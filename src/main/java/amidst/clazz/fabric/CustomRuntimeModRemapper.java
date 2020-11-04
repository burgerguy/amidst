package amidst.clazz.fabric;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.commons.Remapper;

import amidst.logging.AmidstLogger;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerRemapper;
import net.fabricmc.accesswidener.AccessWidenerWriter;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.game.GameProvider;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.FileSystemUtil;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import net.fabricmc.loader.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.tinyremapper.InputTag;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;

public class CustomRuntimeModRemapper {
	
	public static Collection<ModCandidate> remap(String from, String to, Collection<ModCandidate> modCandidates,
			FileSystem fileSystem, GameProvider provider, ClassLoader knotClassLoader, URL[] systemClassPath, URL[] providedClassPath, URL gameJarUrl) {
		
		if(FabricSetup.DEBUG_LOGGING) AmidstLogger.info("[FabricSetup] Setting up remapping environment...");
		
		List<ModCandidate> modsToRemap = modCandidates.stream().filter(ModCandidate::requiresRemap)
				.collect(Collectors.toList());
		
		if (modsToRemap.isEmpty()) {
			return modCandidates;
		}
		
		List<ModCandidate> modsToSkip = modCandidates.stream().filter(mc -> !mc.requiresRemap())
				.collect(Collectors.toList());
		
		FabricLauncher launcher = FabricLauncherBase.getLauncher();
		
		TinyRemapper remapper = TinyRemapper.newRemapper()
				.withMappings(
						TinyRemapperMappingsHelper.create(launcher.getMappingConfiguration().getMappings(), from, to))
				.renameInvalidLocals(false).build();
		
		// We only need to deobfuscate this into an intermediary jar because it allows us to later remap the AccessWidener.
		try {
			if (provider.isObfuscated()) {
				for (Path path : provider.getGameContextJars()) {
					// the PassthroughFabricLauncher makes it so proposed URLs don't get added to the classpath
					remapper.readClassPathAsync(deobfuscate(provider.getGameId(), provider.getNormalizedGameVersion(), Paths.get("."), path, new PassthroughFabricLauncher(launcher, providedClassPath, gameJarUrl)));
				}
			}
		} catch (Throwable t) {
			AmidstLogger.crash(t, "Failed to remap the main Minecraft JAR");
		}
		
		try {
			Field f1 = Class.forName("net.fabricmc.loader.launch.knot.KnotClassLoader").getDeclaredField("urlLoader");
			f1.setAccessible(true);
			URLClassLoader urlLoader = (URLClassLoader) f1.get(knotClassLoader);
			remapper.readClassPathAsync(Arrays.stream(urlLoader.getURLs()).map(url -> {
				try {
					return Paths.get(url.toURI());
				} catch (URISyntaxException e) {
					return null;
				}
			}).filter(p -> p != null).toArray(i -> new Path[i]));
		
			remapper.readClassPathAsync(Arrays.stream(systemClassPath).map(url -> {
				try {
					return Paths.get(url.toURI());
				} catch (URISyntaxException e) {
					return null;
				}
			}).filter(p -> p != null).toArray(i -> new Path[i]));
		} catch (Throwable t) {
			AmidstLogger.crash(t, "Failed to copy URLs from classpath to remapper");
		}
		
		List<ModCandidate> remappedMods = new ArrayList<>();
		
		try {
			if(FabricSetup.DEBUG_LOGGING) AmidstLogger.info("[FabricSetup] Reading mods...");
			Map<ModCandidate, RemapInfo> infoMap = new HashMap<>();
			
			for (ModCandidate mod : modsToRemap) {
				RemapInfo info = new RemapInfo();
				infoMap.put(mod, info);
				
				InputTag tag = remapper.createInputTag();
				info.tag = tag;
				info.inputPath = UrlUtil.asPath(mod.getOriginUrl());
				
				remapper.readInputsAsync(tag, info.inputPath);
			}
			
			if(FabricSetup.DEBUG_LOGGING) AmidstLogger.info("[FabricSetup] Remapping classes...");
			//Done in a 2nd loop as we need to make sure all the inputs are present before remapping
			for (ModCandidate mod : modsToRemap) {
				RemapInfo info = infoMap.get(mod);
				info.outputPath = fileSystem.getPath(UUID.randomUUID() + ".jar");
				OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(info.outputPath).build();
				
				FileSystemUtil.FileSystemDelegate delegate = FileSystemUtil.getJarFileSystem(info.inputPath, false);
				
				if (delegate.get() == null) {
					throw new RuntimeException(
							"Could not open JAR file " + info.inputPath.getFileName() + " for NIO reading!");
				}
				
				Path inputJar = delegate.get().getRootDirectories().iterator().next();
				outputConsumer.addNonClassFiles(inputJar);
				
				info.outputConsumerPath = outputConsumer;
				
				remapper.apply(outputConsumer, info.tag);
			}
			
			if(FabricSetup.DEBUG_LOGGING) AmidstLogger.info("[FabricSetup] Remapping access wideners...");
			//Done in a 3rd loop as this can happen when the remapper is doing its thing.
			for (ModCandidate mod : modsToRemap) {
				RemapInfo info = infoMap.get(mod);
				
				String accessWidener = mod.getInfo().getAccessWidener();
				
				if (accessWidener != null) {
					info.accessWidenerPath = accessWidener;
					
					try (FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(info.inputPath, false)) {
						FileSystem fs = jarFs.get();
						info.accessWidener = remapAccessWidener(Files.readAllBytes(fs.getPath(accessWidener)), remapper.getRemapper(), to);
					}
				}
			}
			
			remapper.finish();
			if(FabricSetup.DEBUG_LOGGING) AmidstLogger.info("[FabricSetup] Finished remapping");
			
			for (ModCandidate mod : modsToRemap) {
				RemapInfo info = infoMap.get(mod);
				
				info.outputConsumerPath.close();
				
				if (info.accessWidenerPath != null) {
					try (FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(info.outputPath,
							false)) {
						FileSystem fs = jarFs.get();
						
						Files.delete(fs.getPath(info.accessWidenerPath));
						Files.write(fs.getPath(info.accessWidenerPath), info.accessWidener);
					}
				}
				
				remappedMods.add(new ModCandidate(mod.getInfo(), UrlUtil.asUrl(info.outputPath), 0, false));
			}
			
		} catch (UrlConversionException | IOException e) {
			remapper.finish();
			throw new RuntimeException("Failed to remap mods", e);
		}
		
		return Stream.concat(remappedMods.stream(), modsToSkip.stream()).collect(Collectors.toList());
	}
	
	private static byte[] remapAccessWidener(byte[] input, Remapper remapper, String to) {
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(input), StandardCharsets.UTF_8))) {
			AccessWidener accessWidener = new AccessWidener();
			new AccessWidenerReader(accessWidener).read(bufferedReader);
			
			AccessWidenerRemapper accessWidenerRemapper = new AccessWidenerRemapper(accessWidener, remapper, to);
			AccessWidener remapped = accessWidenerRemapper.remap();
			
			try (StringWriter writer = new StringWriter()) {
				new AccessWidenerWriter(remapped).write(writer);
				return writer.toString().getBytes(StandardCharsets.UTF_8);
			}
		} catch (IOException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Path deobfuscate(String gameId, String gameVersion, Path gameDir, Path jarFile, FabricLauncher launcher) throws Throwable {
		Method m = FabricLauncherBase.class.getDeclaredMethod("deobfuscate", String.class, String.class, Path.class, Path.class, FabricLauncher.class);
		m.setAccessible(true);
		return (Path) m.invoke(null, gameId, gameVersion, gameDir, jarFile, launcher);
	}
	
	private static class RemapInfo {
		InputTag tag;
		Path inputPath;
		Path outputPath;
		OutputConsumerPath outputConsumerPath;
		String accessWidenerPath;
		byte[] accessWidener;
	}
}
