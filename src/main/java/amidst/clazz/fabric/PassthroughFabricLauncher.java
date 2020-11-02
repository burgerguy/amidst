package amidst.clazz.fabric;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import amidst.logging.AmidstLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.MappingConfiguration;

public class PassthroughFabricLauncher implements FabricLauncher {
	private final FabricLauncher launcher;
	private final Set<URL> urls;
	
	public PassthroughFabricLauncher(FabricLauncher launcher, URL[] providedClassPath, URL gameJarUrl) {
		this.launcher = launcher;
		this.urls = new HashSet<URL>();
		
		for (URL url : providedClassPath) {
			if (!url.sameFile(gameJarUrl)) {
				urls.add(url);
			}
		}
	}
	
	@Override
	public void propose(URL url) {
		if(FabricSetup.DEBUG_LOGGING) AmidstLogger.info("[FabricSetup] Rejected proposition of " + url + " to classpath");
	}
	
	@Override
	public EnvType getEnvironmentType() {
		return launcher.getEnvironmentType();
	}
	
	@Override
	public boolean isClassLoaded(String name) {
		return launcher.isClassLoaded(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		return launcher.getResourceAsStream(name);
	}
	
	@Override
	public ClassLoader getTargetClassLoader() {
		return launcher.getTargetClassLoader();
	}
	
	@Override
	public byte[] getClassByteArray(String name, boolean runTransformers) throws IOException {
		return launcher.getClassByteArray(name, runTransformers);
	}
	
	@Override
	public boolean isDevelopment() {
		return false;
	}
	
	@Override
	public String getEntrypoint() {
		return launcher.getEntrypoint();
	}
	
	@Override
	public String getTargetNamespace() {
		return "official";
	}
	
	@Override
	public Collection<URL> getLoadTimeDependencies() {
		return urls;
	}

	@Override
	public MappingConfiguration getMappingConfiguration() {
		return launcher.getMappingConfiguration();
	}
	
}
