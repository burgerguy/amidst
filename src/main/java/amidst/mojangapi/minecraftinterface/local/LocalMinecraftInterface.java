package amidst.mojangapi.minecraftinterface.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.LongFunction;

import amidst.clazz.symbolic.SymbolicClass;
import amidst.clazz.symbolic.SymbolicObject;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.WorldType;

public class LocalMinecraftInterface implements MinecraftInterface {
	
	private boolean isInitialized = false;
	private final RecognisedVersion recognisedVersion;
	
	private final SymbolicClass registryClass;
	private final SymbolicClass worldTypeClass;
	private final SymbolicClass genSettingsClass;
	private final SymbolicClass levelDataClass;
	private final SymbolicClass layersClass;
	private final SymbolicClass lazyAreaClass;
	private final SymbolicClass lazyAreaContextClass;
	private final SymbolicClass noiseBiomeSourceClass;
	private final SymbolicClass foccbzClass; // stands for fuzzyOffsetConstantColumnBiomeZoomerClass
	private final SymbolicClass mappedRegistryClass;
	private final SymbolicClass pixelTransformerClass;
	private final SymbolicClass utilClass;
	
	/**
	 * A PixelTransformer instance for the current world that is given to
	 * each thread, giving direct access to the quarter-scale biome data.
	 */
	private ThreadLocal<Object> threadedPixelTransformer;
	
	/**
	 * An instance of fuzzyOffsetConstantColumnBiomeZoomer that we use to
	 * get the full resolution biome data.
	 */
	private Object fuzzyOffsetConstantColumnBiomeZoomer;
	
	/**
	 * An instance of NoiseBiomeSource that we create and provide quarter
	 * resolution biome data to. This is used by minecraft to create the
	 * full resolution biome data.
	 */
	private Object noiseBiomeSource;
	
	/**
	 * A method used to retrieve the full resolution biome data.
	 * We create a SymbolicMethod for it so we dont lose performance
	 * searching the SymbolicClass for it every time it's called.
	 */
	private Method getFullResBiomeMethod;
	
	/**
	 * A method used to retrieve the quarter resolution biome data.
	 * We create a SymbolicMethod for it so we dont lose performance
	 * searching the SymbolicClass for it every time it's called.
	 */
	private Method getQuarterResBiomeMethod;
	
	/**
	 * A method used to retrieve biome ids for the full resolution
	 * biome data. We create a SymbolicMethod for it so we dont lose
	 * performance searching the SymbolicClass for it every time it's
	 * called.
	 */
	private Method getIdFromBiomeMethod;
	
	/**
	 * A method used for converting back to ints from Biomes after
	 * minecraft has generated the full resolution biome data.
	 */
	private Method getBiomeFromIdMethod;
	
	/**
	 * The registry for converting between ints and biomes.
	 */
	private Object biomeRegistry;
	
	/**
	 * The seed used by the BiomeZoomer during interpolation. It is
	 * derived from the world seed.
	 */
	private long seedForBiomeZoomer;
	
	/**
	 * An array used to return biome data. It's a ThreadLocal so
	 * different threads don't try to access the same array at the same
	 * time.
	 */
	private ThreadLocal<int[]> dataArray = ThreadLocal.withInitial(() -> new int[256]);
	
	public LocalMinecraftInterface(Map<String, SymbolicClass> symbolicClassMap, RecognisedVersion recognisedVersion) {
		this.recognisedVersion = recognisedVersion;
		this.registryClass = symbolicClassMap.get(SymbolicNames.CLASS_REGISTRY);
		this.worldTypeClass = symbolicClassMap.get(SymbolicNames.CLASS_WORLD_TYPE);
		this.genSettingsClass = symbolicClassMap.get(SymbolicNames.CLASS_GEN_SETTINGS);
		this.levelDataClass = symbolicClassMap.get(SymbolicNames.CLASS_LEVEL_DATA);
		this.layersClass = symbolicClassMap.get(SymbolicNames.CLASS_LAYERS);
		this.lazyAreaClass = symbolicClassMap.get(SymbolicNames.CLASS_LAZY_AREA);
		this.lazyAreaContextClass = symbolicClassMap.get(SymbolicNames.CLASS_LAZY_AREA_CONTEXT);
		this.foccbzClass = symbolicClassMap.get(SymbolicNames.CLASS_FUZZY_OFFSET_CONSTANT_COLUMN_BIOME_ZOOMER);
		this.noiseBiomeSourceClass = symbolicClassMap.get(SymbolicNames.CLASS_NOISE_BIOME_SOURCE);
		this.mappedRegistryClass = symbolicClassMap.get(SymbolicNames.CLASS_MAPPED_REGISTRY);
		this.pixelTransformerClass = symbolicClassMap.get(SymbolicNames.CLASS_PIXEL_TRANSFORMER);
		this.utilClass = symbolicClassMap.get(SymbolicNames.CLASS_UTIL);
	}

	@Override
	public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution)
			throws MinecraftInterfaceException {
		if (!isInitialized || fuzzyOffsetConstantColumnBiomeZoomer == null) {
			throw new MinecraftInterfaceException("no world was created");
		}
		
		int[] data = ensureArrayCapacity(width * height);
		
		try {
			
			/**
			 * We break the region in 16x16 chunks, to get better performance out
			 * of the LazyArea used by the game. This gives a ~2x improvement.
			 */
			int chunkSize = 16;
			for (int x0 = 0; x0 < width; x0 += chunkSize) {
				int w = Math.min(chunkSize, width - x0);

				for (int y0 = 0; y0 < height; y0 += chunkSize) {
					int h = Math.min(chunkSize, height - y0);

					for (int i = 0; i < w; i++) {
						for (int j = 0; j < h; j++) {
							int trueIdx = (x0 + i) + (y0 + j) * width;
							data[trueIdx] = getBiomeIdAt(x + x0 + i, y + y0 + j, useQuarterResolution);
						}
					}
				}
			}
		} catch (IllegalAccessException
				|IllegalArgumentException
				| InvocationTargetException e) {
			throw new MinecraftInterfaceException("unable to get biome data", e);
		}
		
		return data;
	}

	private int getBiomeIdAt(int x, int y, boolean useQuarterResolution)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(useQuarterResolution) {
			return (int) getQuarterResBiomeMethod.invoke(threadedPixelTransformer.get(), x, y);
		} else {
			return getIdFromBiome((Object) getFullResBiomeMethod.invoke(fuzzyOffsetConstantColumnBiomeZoomer, seedForBiomeZoomer, x, 0, y, noiseBiomeSource));
		}
	}

	public int getBiomeIdAt(int x, int y) throws MinecraftInterfaceException {
		try {
			return getBiomeIdAt(x, y, false);
		} catch (Throwable e) {
			throw new MinecraftInterfaceException("unable to get biome data", e);
		}
	}

	@Override
	public synchronized void createWorld(long seed, WorldType worldType, String generatorOptions)
			throws MinecraftInterfaceException {
		initializeIfNeeded();

		try {
			seedForBiomeZoomer = (long) levelDataClass.callStaticMethod(SymbolicNames.METHOD_LEVEL_DATA_MAP_SEED, seed);
			threadedPixelTransformer = ThreadLocal.withInitial(() -> {
				try {
					return createPixelTransformerObject(seed, worldType);
				} catch (Exception e) {
					throw new RuntimeException("unable to create pixel transformer", e);
				}
			});

		} catch(IllegalArgumentException
				| IllegalAccessException
				| InvocationTargetException e) {
			throw new MinecraftInterfaceException("unable to create world", e);
		}
		
		isInitialized = true;
	}

	private void initializeIfNeeded() throws MinecraftInterfaceException {
		if (isInitialized) {
			return;
		}
		try {
			fuzzyOffsetConstantColumnBiomeZoomer = foccbzClass.getClazz().getEnumConstants()[0];
			getFullResBiomeMethod = foccbzClass.getMethod(SymbolicNames.METHOD_FUZZY_OFFSET_CONSTANT_COLUMN_BIOME_ZOOMER_GET_BIOME).getRawMethod();
			getQuarterResBiomeMethod = pixelTransformerClass.getMethod(SymbolicNames.METHOD_PIXEL_TRANSFORMER_APPLY).getRawMethod();
			getIdFromBiomeMethod = mappedRegistryClass.getMethod(SymbolicNames.METHOD_MAPPED_REGISTRY_GET_ID).getRawMethod();
			getBiomeFromIdMethod = mappedRegistryClass.getMethod(SymbolicNames.METHOD_MAPPED_REGISTRY_BY_ID).getRawMethod();
			noiseBiomeSource = createNoiseBiomeSource();
			biomeRegistry = registryClass.getField(SymbolicNames.FIELD_REGISTRY_BIOME).getRawField().get(null);
			
			try {
				((ExecutorService) utilClass.getStaticFieldValue(SymbolicNames.FIELD_UTIL_SERVER_EXECUTOR)).shutdownNow();
			} catch (NullPointerException e) {
				AmidstLogger.warn("Unable to shut down Server-Worker threads");
			}
			
		} catch(IllegalArgumentException | IllegalAccessException e) {
			throw new MinecraftInterfaceException("unable to initialize the MinecraftInterface", e);
		}
		
	}

	private Object createNoiseBiomeSource() {
		Class<?> nbsInterface = noiseBiomeSourceClass.getClazz();
		
		return Proxy.newProxyInstance(nbsInterface.getClassLoader(), new Class<?>[]{nbsInterface}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return getBiomeFromId(getBiomeIdAt((int) args[0], (int) args[2], true));
			}
		});
	}

	private Object createPixelTransformerObject(long seed, WorldType worldType)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Object worldTypeObj = worldTypeClass.getField(worldType.getSymbolicFieldName()).getRawField().get(null);
		
		Object genSettingsObj = genSettingsClass.getConstructor(SymbolicNames.CONSTRUCTOR_GEN_SETTINGS).getRawConstructor().newInstance();
		
		SymbolicObject areaFactoryObj = (SymbolicObject) layersClass.callStaticMethod(
				SymbolicNames.METHOD_LAYERS_GET_DEFAULT_LAYER,
				worldTypeObj,
				genSettingsObj,
				(LongFunction<?>)l -> {
					try {
						return lazyAreaContextClass.callConstructor(
								SymbolicNames.CONSTRUCTOR_LAZY_AREA_CONTEXT, 25, seed, l
								).getObject();
					} catch (InstantiationException
							| IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException e) {
						throw new RuntimeException("unable to create lazy area context", e);
					}
				}
				);

		Object lazyAreaObj = areaFactoryObj.callMethod(SymbolicNames.METHOD_AREA_FACTORY_MAKE);
		
		return lazyAreaClass.getField(SymbolicNames.FIELD_LAZY_AREA_PIXEL_TRANSFORMER).getRawField().get(lazyAreaObj);
	}

	@Override
	public RecognisedVersion getRecognisedVersion() {
		return recognisedVersion;
	}
	
	private int[] ensureArrayCapacity(int length) throws MinecraftInterfaceException {
		if (length > 1073741824) {
			throw new MinecraftInterfaceException("Biome data array size exceeds maximum limit");
		} else {
			int[] currentArray = dataArray.get();
			int cur = currentArray.length;
			if (length <= cur)
				return currentArray;

			while (cur < length)
				cur *= 2;
			
			currentArray = new int[cur];
			dataArray.set(currentArray);
			return currentArray;
		}
	}
	
	private int getIdFromBiome(Object biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (int) getIdFromBiomeMethod.invoke(biomeRegistry, biome);
	}
	
	private Object getBiomeFromId(int id)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getBiomeFromIdMethod.invoke(biomeRegistry, id);
	}
}
