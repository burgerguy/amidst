package amidst.mojangapi.minecraftinterface;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import amidst.documentation.ThreadSafe;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.world.Dimension;
import amidst.mojangapi.world.WorldOptions;

@ThreadSafe
public class LoggingMinecraftInterface implements MinecraftInterface {
	private final MinecraftInterface inner;

	public LoggingMinecraftInterface(MinecraftInterface minecraftInterface) {
		this.inner = minecraftInterface;
	}

	@Override
	public MinecraftInterface.WorldAccessHelper createAccessHelper(WorldOptions worldOptions) throws MinecraftInterfaceException {
		MinecraftInterface.WorldAccessHelper config = new WorldAccessHelper(worldOptions);
		
		StringBuilder sb = new StringBuilder("Supported dimensions for world configuration: ");
		boolean firstDim = true;
		for(Dimension dimension : config.supportedDimensions()) {
			if(firstDim) {
				firstDim = false;
			} else {
				sb.append(", ");
			}
			
			sb.append(dimension.getDisplayName());
		}
		AmidstLogger.info(sb.toString());
		
		return config;
	}

	@Override
	public RecognisedVersion getRecognisedVersion() {
		return inner.getRecognisedVersion();
	}

	private class WorldAccessHelper implements MinecraftInterface.WorldAccessHelper {
		private final MinecraftInterface.WorldAccessHelper innerAccessHelper;
		private final WorldOptions worldOptions;
		
		// This is used so we don't log the message every time a thread creates a new WorldAccessor.
		private final AtomicBoolean shouldLogAccessor = new AtomicBoolean(true);
		
		private WorldAccessHelper(WorldOptions worldOptions) throws MinecraftInterfaceException {
			this.innerAccessHelper = inner.createAccessHelper(worldOptions);
			this.worldOptions = worldOptions;
		}

		@Override
		public Set<Dimension> supportedDimensions() {
			return innerAccessHelper.supportedDimensions();
		}

		@Override
		public WorldAccessor createWorldAccessor() throws MinecraftInterfaceException {
			if(shouldLogAccessor.getAndSet(false)) {
				AmidstLogger.info("Creating world with seed '{}' and type '{}'", worldOptions.getWorldSeed().getLong(), worldOptions.getWorldType().getName());
				AmidstLogger.info("Using the following generator options: {}", worldOptions.getGeneratorOptions());
			}
			
			return innerAccessHelper.createWorldAccessor();
		}
	}
}
