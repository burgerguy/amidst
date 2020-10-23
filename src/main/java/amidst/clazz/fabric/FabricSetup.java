package amidst.clazz.fabric;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.zeroturnaround.zip.ZipUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import amidst.logging.AmidstLogger;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import io.github.classgraph.ClassGraph;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.entrypoint.EntrypointTransformer;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import net.fabricmc.loader.game.GameProvider;
import net.fabricmc.loader.game.GameProviders;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.fabricmc.loader.launch.knot.Knot;
import net.fabricmc.loader.util.mappings.MixinIntermediaryDevRemapper;
import net.fabricmc.loader.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.loom.util.accesswidener.AccessWidener;
import net.fabricmc.loom.util.accesswidener.AccessWidenerRemapper;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;

public enum FabricSetup {
	;
	private static final EnvType ENVIRONMENT_TYPE = EnvType.CLIENT;
	private static final boolean DEVELOPMENT = false; // if this is set to true then all normally compiled mods break
	private static final boolean DEBUG_LOGGING = Boolean.getBoolean("amidst.fabric.debug");
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
		Constructor<?> constructor = Class.forName("net.fabricmc.loader.launch.knot.KnotCompatibilityClassLoader").getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		URLClassLoader knotClassLoader = (URLClassLoader) constructor.newInstance(DEVELOPMENT, ENVIRONMENT_TYPE, provider);
		
		setProperties(new HashMap<>());
		
		// DON'T CALL INIT ON THIS!!!
		// We're basically replicating what init does anyway, we just happen
		// to need an instance of a Knot object. If init gets called this will
		// probably crash.
		Knot knot = createKnotInstance(ENVIRONMENT_TYPE, clientJarPath.toFile());
		
		setKnotVars(knot, knotClassLoader, DEVELOPMENT, provider);
		
		// Get all of the classes in the system classloader to check against
		URL[] systemClassPath = getSystemClasspathUrls();
		
		// Merge given classloader into new KnotClassLoader
		try {		    
			for (URL url : ucl.getURLs()) { // given class loader
				String urlString = url.toString().toLowerCase();
				if (!hasMatchingUrl(url, systemClassPath)
				 && !urlString.contains("fabric")
				 && !urlString.contains("mixin")
				 && !urlString.contains("asm")
				 && !urlString.contains("log4j")
				 && !urlString.contains("gson")
				 && !url.sameFile(clientJarPath.toUri().toURL())) {
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
		
		tryRemapAllMods(provider, clientJarPath, knotClassLoader, knot);
		
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
		loader.load();
		loader.freeze();
		
		loader.getAccessWidener().loadFromMods();

		MixinBootstrap.init();
		
		MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
		if (DEBUG_LOGGING) env.setOption(MixinEnvironment.Option.DEBUG_VERBOSE, true);
		
		env.setOption(MixinEnvironment.Option.REFMAP_REMAP, true);
		MixinIntermediaryDevRemapper remapper = new MixinIntermediaryDevRemapper(FabricLauncherBase.getLauncher().getMappingConfiguration().getMappings(), fromNamespace, toNamespace);
		env.getRemappers().add(remapper);
		
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
	
	private static void deobfuscate(String gameId, String gameVersion, Path gameDir, Path jarFile, FabricLauncher launcher) throws Throwable {
		Method m = FabricLauncherBase.class.getDeclaredMethod("deobfuscate", String.class, String.class, Path.class, Path.class, FabricLauncher.class);
		m.setAccessible(true);
		m.invoke(null, gameId, gameVersion, gameDir, jarFile, launcher);
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
	
	private static void loadEntrypoint(GameProvider provider, EnvType envType, ClassLoader loader) throws Throwable {
		String targetClass = provider.getEntrypoint();
		
		if (envType == EnvType.CLIENT && targetClass.contains("Applet")) {
			targetClass = "net.fabricmc.loader.entrypoint.applet.AppletMain";
		}
		
		if(DEBUG_LOGGING) AmidstLogger.debug("Loading GameProvider entrypoint: " + targetClass);
		loader.loadClass(targetClass);
	}
	
	@SuppressWarnings("unused")
	private static void setGameDir(FabricLoader loader, Path newDir) throws Throwable {
		Method m1 = FabricLoader.class.getDeclaredMethod("setGameDir", Path.class);
		m1.setAccessible(true);
		m1.invoke(loader, newDir);
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
	
	private static void tryRemapAllMods(GameProvider provider, Path clientJarPath, URLClassLoader knotClassLoader, Knot knot) throws Throwable {
		boolean firstRemap = true;
		TinyRemapper remapper = null;
		Remapper asmRemapper = null;
		
		Path remappedModsDir = provider.getLaunchDirectory().resolve("mods");
		Files.createDirectories(remappedModsDir);
		Path unmappedModsDir = provider.getLaunchDirectory().resolve("unmappedMods");
		Files.createDirectories(unmappedModsDir);
		
		Iterator<Path> modPathIterator = Files.list(provider.getLaunchDirectory().resolve("unmappedMods")).iterator();
		
		for (Path modPath : (Iterable<Path>)(() -> modPathIterator)) {
			if (Files.isDirectory(modPath)) continue;
			String[] filenameSplits = modPath.getFileName().toString().split("(?=\\.)");
			String extension = filenameSplits[filenameSplits.length - 1];
			if (!extension.equals(".jar")) continue;
			StringBuilder filenameBuilder = new StringBuilder();
			for (int i = 0; i < filenameSplits.length - 1; i++) {
				filenameBuilder.append(filenameSplits[i]);
			}
			filenameBuilder.append("-REMAPPED");
			filenameBuilder.append(extension);
			
			Path newPath = remappedModsDir.resolve(filenameBuilder.toString());

			if (Files.exists(newPath)) continue;
			
			if (firstRemap) {
				AmidstLogger.info("Setting up remapping environment");
				
				// We only need to deobfuscate this into an intermediary jar because it allows us to later remap the AccessWidener.
				if (provider.isObfuscated()) {
					for (Path path : provider.getGameContextJars()) {
						deobfuscate(
							provider.getGameId(),
							provider.getNormalizedGameVersion(),
							Paths.get("."),
							path,
							knot
						);
					}
				}
				
				Path[] knotClassPath = Arrays.stream(knotClassLoader.getURLs()).map(url -> {
					try {
						return Paths.get(url.toURI());
					} catch (URISyntaxException e) {
						return null;
					}
				}).filter(p -> p != null).toArray(i -> new Path[i]);
				
				remapper = TinyRemapper.newRemapper().withMappings(
						TinyRemapperMappingsHelper.create(
								FabricLauncherBase.getLauncher().getMappingConfiguration().getMappings(),
								fromNamespace,
								toNamespace
						)).build();
				remapper.readClassPath(knotClassPath);
				asmRemapper = remapper.getRemapper();
				
				firstRemap = false;
			}
			
			AmidstLogger.info("Remapping mod " + modPath.getFileName());
			
			try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(newPath).build()) {
				remapper.readInputs(modPath);
				remapper.apply(outputConsumer);
				outputConsumer.addNonClassFiles(modPath);
			} catch (Exception e) {
				remapper.finish();
				throw new RuntimeException("Failed to remap " + modPath + " to " + newPath, e);
			}

			String awFileName = null;
			byte[] awBytes = null;
			try (ZipFile newModZip = new ZipFile(newPath.toAbsolutePath().toString())) {
				ZipUtil.unpackEntry(newModZip, "fabric.mod.json");
				JsonObject modJson = new Gson().fromJson(new String(ZipUtil.unpackEntry(newModZip, "fabric.mod.json"), StandardCharsets.UTF_8), JsonObject.class);
				if (modJson.has("accessWidener")) {
					awFileName = modJson.get("accessWidener").getAsString();
				}
				
				if (awFileName != null) {
					AccessWidener accessWidener = new AccessWidener();
					accessWidener.read(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ZipUtil.unpackEntry(newModZip, awFileName)))));
					AccessWidenerRemapper awRemapper = new AccessWidenerRemapper(accessWidener, asmRemapper, toNamespace);
					AccessWidener remapped = awRemapper.remap();
					try (StringWriter writer = new StringWriter()) {
						remapped.write(writer);
						awBytes = writer.toString().getBytes();
					}
				}
				
			} finally {
				remapper.finish();
			}
			
			if (awFileName != null) {
				boolean replaced = ZipUtil.replaceEntry(newPath.toFile(), awFileName, awBytes);
				
				if (!replaced) {
					AmidstLogger.error("Failed to replace access widener file at " + awFileName);
				}
			}
			
			if (!Files.exists(newPath)) {
				throw new RuntimeException("Failed to remap " + modPath + " to " + newPath + " - file missing!");
			}
				
//			if (NestedJars.addNestedJars(project, output)) {
//				AmidstLogger.info("Added nested jar paths to mod json");
//			}
		}
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
