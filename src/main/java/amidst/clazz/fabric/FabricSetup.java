package amidst.clazz.fabric;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import amidst.logging.AmidstLogger;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import io.github.classgraph.ClassGraph;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.discovery.ClasspathModCandidateFinder;
import net.fabricmc.loader.discovery.DirectoryModCandidateFinder;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.discovery.ModResolutionException;
import net.fabricmc.loader.discovery.ModResolver;
import net.fabricmc.loader.entrypoint.EntrypointTransformer;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import net.fabricmc.loader.game.GameProvider;
import net.fabricmc.loader.game.GameProviders;
import net.fabricmc.loader.gui.FabricGuiEntry;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.fabricmc.loader.launch.knot.Knot;
import net.fabricmc.loader.util.mappings.MixinIntermediaryDevRemapper;

public enum FabricSetup {
	;
	private static final EnvType ENVIRONMENT_TYPE = EnvType.CLIENT;
	private static final boolean DEVELOPMENT = false; // if this is set to true then all normally compiled mods break
	private static final boolean DEBUG_LOGGING = Boolean.getBoolean("amidst.fabric.debug");
	private static final boolean RUNTIME_REMAPPING = true;
	private static final boolean DEBUG_DUMPING = false;
	private static final String fromNamespace = "intermediary";
	private static final String toNamespace = "official";
	
	public static ClassLoader initAndGetClassLoader(URLClassLoader ucl, Path clientJarPath) throws Throwable {
		
		if (DEBUG_LOGGING) Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.DEBUG);
		
		List<GameProvider> providers = GameProviders.create();
		GameProvider provider = null;
		
		for (GameProvider p : providers) {
			if (p.locateGame(ENVIRONMENT_TYPE, ucl)) {
				provider = p;
				break;
			}
		}
		
		if (provider != null) {
			AmidstLogger.info("[FabricSetup] Loading for game " + provider.getGameName() + " " + provider.getRawGameVersion());
		} else {
			AmidstLogger.error("[FabricSetup] Could not find valid game provider!");
			for (GameProvider p : providers) {
				AmidstLogger.error("- " + p.getGameName()+ " " + p.getRawGameVersion());
			}
			throw new RuntimeException("Could not find valid game provider!");
		}
		
		provider.acceptArguments("--gameDir", ".fabric" + File.separator + "environments" + File.separator + provider.getRawGameVersion());
		
		// Reflect classloader instance
		// we need a url class loader so we can later obtain its classpath
		Constructor<?> constructor = Class.forName("net.fabricmc.loader.launch.knot.KnotClassLoader").getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		ClassLoader knotClassLoader = (ClassLoader) constructor.newInstance(DEVELOPMENT, ENVIRONMENT_TYPE, provider);
		
		setProperties(new HashMap<>());
		
		// DON'T CALL INIT ON THIS!!!
		// We're basically replicating what init does anyway, we just happen
		// to need an instance of a Knot object. If init gets called this will
		// probably crash.
		Knot knot = createKnotInstance(ENVIRONMENT_TYPE, clientJarPath.toFile());
		
		setKnotVars(knot, knotClassLoader, DEVELOPMENT, provider);
		
		// Get all of the classes in the system classloader to check against
		URL[] systemClassPath = getSystemClasspathUrls();
		
		URL gameJarUrl = clientJarPath.toUri().toURL();
		
		// Merge given classloader into new KnotClassLoader
		URL[] providedClassPath = ucl.getURLs();
		try {		    
			for (URL url : providedClassPath) { // given class loader
				String urlString = url.toString().toLowerCase();
				if (!hasMatchingUrl(url, systemClassPath)
				 && !urlString.contains("fabric")
				 && !urlString.contains("mixin")
				 && !urlString.contains("asm")
				 && !urlString.contains("log4j")
				 && !urlString.contains("gson")
				 && !url.sameFile(gameJarUrl)) {
					knot.propose(url);
				} else {
					if (DEBUG_LOGGING) AmidstLogger.debug("Rejected URL: " + url);
				}
			}
			
		} catch (Throwable e) {
			AmidstLogger.error("Unable to add URLs to classpath");
		}
		
		boolean mergedMappings = RecognisedVersion.isNewerOrEqualTo(RecognisedVersion.fromName(provider.getRawGameVersion()), RecognisedVersion._20w10a);
		if (tryAddYarnToClasspath(provider, knot, mergedMappings ? "-mergedv2.jar" : ".jar")) {
			AmidstLogger.info("Finished setting up yarn mappings");
		} else {
			throw new UnsupportedOperationException("Minecraft version incompatible with Fabric");
		}
		
		// FABRIC DOC: Locate entrypoints before switching class loaders
		// Read documentation at the setContextClassLoader() for more info as to why
		// this isn't exactly true.
		
		// Because we're always going to be manually invoking the entrypoints anyway,
		// it makes sense to disable the modification of classes as it removes the
		// possibility of faliure in that aspect. This may break some mods that require
		// janky entrypoints.
		
		fakeInitializeEntrypointTransformer(provider.getEntrypointTransformer());
//		provider.getEntrypointTransformer().locateEntrypoints(knot);
		
		// This doesn't actually switch the classloader, this only does something if
		// getContextClassLoader() gets called on the same thread somewhere else.
		Thread.currentThread().setContextClassLoader(knotClassLoader);
		
		@SuppressWarnings("deprecation")
		FabricLoader loader = FabricLoader.INSTANCE;
		setMappingResolverNamespace(loader, toNamespace);
		loader.setGameProvider(provider);
//		loader.load();
		setupLoader(loader, provider, knotClassLoader, systemClassPath, providedClassPath, gameJarUrl);
		loader.freeze();
		
		loader.getAccessWidener().loadFromMods();

		MixinBootstrap.init();
		
		MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
		if (DEBUG_LOGGING) env.setOption(MixinEnvironment.Option.DEBUG_VERBOSE, true);
		
		env.setOption(MixinEnvironment.Option.REFMAP_REMAP, true);
		MixinIntermediaryDevRemapper remapper = new MixinIntermediaryDevRemapper(FabricLauncherBase.getLauncher().getMappingConfiguration().getMappings(), fromNamespace, toNamespace);
		env.getRemappers().add(remapper);
//		env.setOption(MixinEnvironment.Option.REFMAP_REMAP_ALLOW_PERMISSIVE, true);
		
		if (DEBUG_DUMPING) {
			env.setOption(MixinEnvironment.Option.DEBUG_EXPORT, true);
			env.setOption(MixinEnvironment.Option.DUMP_TARGET_ON_FAILURE, true);
		}
		
		FabricMixinBootstrap.init(ENVIRONMENT_TYPE, loader);
		finishMixinBootstrapping();
		
		initializeTransformers(knotClassLoader);
		
		if (DEBUG_LOGGING) listAllEntrypoints(loader);
		
		EntrypointUtils.invoke("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
		
		loadEntrypoint(provider, ENVIRONMENT_TYPE, knotClassLoader); // This is done to imitate what fabric loader would be doing at
																 // this stage. After invoking the preLaunch entrypoint, it loads
																 // the main Minecraft class and invokes the main, where the main
																 // entrypoint would have been invoked naturally.
		
//		EntrypointUtils.invoke("main", ModInitializer.class, ModInitializer::onInitialize);
//		EntrypointUtils.invoke("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);
//		EntrypointUtils.invoke("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
		
		return knotClassLoader;
	}
	
	private static void setProperties(Map<String, Object> properties) throws Throwable {
		Method m = FabricLauncherBase.class.getDeclaredMethod("setProperties", Map.class);
		m.setAccessible(true);
		m.invoke(null, properties);
	}
	
	private static void initializeTransformers(ClassLoader classLoader) throws Throwable {
		Method m1 = classLoader.getClass().getDeclaredMethod("getDelegate");
		m1.setAccessible(true);
		Object delegate = m1.invoke(classLoader);
		
		Method m2 = delegate.getClass().getDeclaredMethod("initializeTransformers");
		m2.setAccessible(true);
		m2.invoke(delegate);
	}
	
	private static Knot createKnotInstance(EnvType type, File gameJarFile) throws Throwable {
		Constructor<?> constructor = Knot.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return (Knot) constructor.newInstance(type, gameJarFile);
	}
	
	private static void finishMixinBootstrapping() throws Throwable{
		Method m = FabricLauncherBase.class.getDeclaredMethod("finishMixinBootstrapping");
		m.setAccessible(true);
		m.invoke(null);
	}
	
	private static void setKnotVars(Knot knot, ClassLoader classLoader, boolean isDevelopment, GameProvider provider) throws Throwable {
		Field f1 = Knot.class.getDeclaredField("classLoader");
		Field f2 = Knot.class.getDeclaredField("isDevelopment");
		Field f3 = Knot.class.getDeclaredField("provider");
		
		f1.setAccessible(true);
		f2.setAccessible(true);
		f3.setAccessible(true);
		
		f1.set(knot, classLoader);
		f2.setBoolean(knot, isDevelopment);
		f3.set(knot, provider);
	}
	
	private static void setupLoader(FabricLoader loader, GameProvider provider, ClassLoader knotClassLoader,
			URL[] systemClassPath, URL[] providedClassPath, URL gameJarUrl) throws Throwable {
		try {			
			ModResolver resolver = new ModResolver();
			resolver.addCandidateFinder(new ClasspathModCandidateFinder());
			resolver.addCandidateFinder(new DirectoryModCandidateFinder(loader.getModsDir(), RUNTIME_REMAPPING));
			Map<String, ModCandidate> candidateMap = resolver.resolve(loader);

			String modText;
			switch (candidateMap.values().size()) {
				case 0:
					modText = "Loading {} mods";
					break;
				case 1:
					modText = "Loading {} mod: {}";
					break;
				default:
					modText = "Loading {} mods: {}";
					break;
			}

			AmidstLogger.info("[FabricSetup] " + modText, candidateMap.values().size(), candidateMap.values().stream()
				.map(info -> String.format("%s@%s", info.getInfo().getId(), info.getInfo().getVersion().getFriendlyString()))
				.collect(Collectors.joining(", ")));
			
			Method addModMethod = FabricLoader.class.getDeclaredMethod("addMod", ModCandidate.class);
			addModMethod.setAccessible(true);
			
			for (ModCandidate candidate : CustomRuntimeModRemapper.remap(fromNamespace, toNamespace, candidateMap.values(), ModResolver.getInMemoryFs(), provider, knotClassLoader, systemClassPath, providedClassPath, gameJarUrl)) {
				// The biome api is excluded because it always fails at appendNetherBiomes after 1.16.2.
				if (!candidate.getInfo().getId().equals("fabric-biome-api-v1")) {
					addModMethod.invoke(loader, candidate);
				}
			}
			
		} catch (ModResolutionException exception) {
			FabricGuiEntry.displayCriticalError(exception, true);
		}
	}
	
	private static void loadEntrypoint(GameProvider provider, EnvType envType, ClassLoader loader) throws Throwable {
		String targetClass = provider.getEntrypoint();
		
		if (envType == EnvType.CLIENT && targetClass.contains("Applet")) {
			targetClass = "net.fabricmc.loader.entrypoint.applet.AppletMain";
		}
		
		if(DEBUG_LOGGING) AmidstLogger.debug("Loading GameProvider entrypoint: " + targetClass);
		loader.loadClass(targetClass);
	}
	
	// We use ClassGraph instead of just converting the system classloader to a
	// URLClassLoader and calling getUrls because that doesn't work past Java 8.
	private static URL[] getSystemClasspathUrls() {
		return new ClassGraph().getClasspathURIs().stream().map(uri -> {
			try {
				return uri.toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}).toArray(size -> new URL[size]);
	}
	
	private static boolean hasMatchingUrl(URL url, URL[] array) throws Throwable {
		for (URL classpathUrl : array) {
			if (classpathUrl.sameFile(url)) {
				return true;
			}
		}
		return false;
	}
	
	private static void listAllEntrypoints(FabricLoader loader) throws Throwable {
		Field f1 = FabricLoader.class.getDeclaredField("entrypointStorage");
		f1.setAccessible(true);
		Object entrypointStorage = f1.get(loader);
		
		Class<?> esClass = Class.forName("net.fabricmc.loader.EntrypointStorage");
		Field f2 = esClass.getDeclaredField("entryMap");
		f2.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		Map<String, List<?>> entryMap = (Map<String, List<?>>) f2.get(entrypointStorage);
		
		for(String entrypoint : entryMap.keySet()) {
			AmidstLogger.debug("Entrypoint " + entrypoint + ":");
			for (Object entry : entryMap.get(entrypoint)) {
				AmidstLogger.debug("	Entry " + entry);
			}
		}
	}
	
	private static void fakeInitializeEntrypointTransformer(EntrypointTransformer transformer) throws Throwable {
		Field f1 = EntrypointTransformer.class.getDeclaredField("entrypointsLocated");
		f1.setAccessible(true);
		f1.set(transformer, true);
		
		Field f2 = EntrypointTransformer.class.getDeclaredField("patchedClasses");
		f2.setAccessible(true);
		f2.set(transformer, new HashMap<>());
	}
	
	private static boolean tryAddYarnToClasspath(GameProvider provider, Knot knot, String fileEnding) throws Throwable {
		Path mappingsDir = Paths.get(".fabric" + File.separatorChar + "mappings");
		String rawGameVersion = provider.getRawGameVersion();
		
		int buildVer = 0;
		Path mappingsFile = null;
		
		if(Files.exists(mappingsDir)) {
			for (Path file : (Iterable<Path>) Files.list(mappingsDir)::iterator) {
				String fileName = file.getFileName().toString();
				String fileNameStart = "yarn-" + rawGameVersion + "+build.";
				if (!Files.isDirectory(file) && fileName.contains(fileNameStart) && fileName.contains(fileEnding)) {
					int possibleBuildVer = Integer.parseInt(fileName.substring(fileNameStart.length(), fileName.indexOf(fileEnding)));
					if (possibleBuildVer > buildVer) {
						buildVer = possibleBuildVer;
						mappingsFile = file;
					}
				}
			}
		}
		
		if (mappingsFile != null) {
			if (DEBUG_LOGGING) AmidstLogger.debug("Local yarn mappings found at " + mappingsFile.toAbsolutePath().toString());
			
		} else {
			if (DEBUG_LOGGING) AmidstLogger.debug("No local yarn mappings found. Downloading...");
			Files.createDirectories(mappingsDir);
			
			if (DEBUG_LOGGING) AmidstLogger.debug("Reading mappings build versions for " + rawGameVersion + "...");
			try (InputStreamReader versionsReader = new InputStreamReader(new URL("https://maven.fabricmc.net/net/fabricmc/yarn/versions.json").openStream())) {
				JsonObject versionsRoot = new Gson().fromJson(versionsReader, JsonObject.class);
				JsonArray buildsArray = versionsRoot.get(rawGameVersion).getAsJsonArray();
				if(buildsArray != null) {
					buildVer = buildsArray.get(buildsArray.size() - 1).getAsInt();
				} else {
					return false;
				}
			}
			
			mappingsFile = mappingsDir.resolve("yarn-" + rawGameVersion + "+build." + buildVer + fileEnding);
			if (DEBUG_LOGGING) AmidstLogger.debug("Downloading yarn mappings " + rawGameVersion + " build " + buildVer + "...");
			
			try (ReadableByteChannel readableByteChannel = Channels.newChannel(
					new URL("https://maven.fabricmc.net/net/fabricmc/yarn/" + rawGameVersion + "%2Bbuild." + buildVer
							+ "/yarn-" + rawGameVersion + "%2Bbuild." + buildVer + fileEnding).openStream())) {
				try (FileOutputStream fileOutputStream = new FileOutputStream(mappingsFile.toAbsolutePath().toString())) {
					fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
				}
			}
			
			if (DEBUG_LOGGING) AmidstLogger.debug("Yarn mappings saved to " + mappingsFile.toAbsolutePath().toString());
		}
		
		URL mappingsURL = mappingsFile.toUri().toURL();
		
		ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
		try {
			// Try using Java 8 method for adding to system classloader
			URLClassLoader urlSystemLoader = (URLClassLoader) systemLoader;
			Method m1 = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			m1.setAccessible(true);
			m1.invoke(urlSystemLoader, mappingsURL);
		} catch (Throwable t) {
			// If that didn't work, use different method for newer Java versions
			Class<?> systemLoaderClass = Class.forName("jdk.internal.loader.ClassLoaders$AppClassLoader");
			Method m1 = systemLoaderClass.getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
			m1.setAccessible(true);
			m1.invoke(ClassLoader.getSystemClassLoader(), mappingsFile.toAbsolutePath().toString());
		}
		
		// Add to knot class loader
		knot.propose(mappingsURL);
		
		return true;
	}
	
	private static void setMappingResolverNamespace(FabricLoader loader, String newNamespace) throws Throwable {
		Field f1 = Class.forName("net.fabricmc.loader.FabricMappingResolver").getDeclaredField("targetNamespace");
		f1.setAccessible(true);
		f1.set(loader.getMappingResolver(), newNamespace);
	}
	
}
