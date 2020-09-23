package amidst.mojangapi.mocking;

import java.util.Set;
import java.util.function.Function;

import amidst.documentation.ThreadSafe;
import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.Dimension;
import amidst.mojangapi.world.WorldOptions;

@ThreadSafe
public class RequestStoringMinecraftInterface implements MinecraftInterface {
	private final MinecraftInterface realMinecraftInterface;
	private final BiomeDataJsonBuilder builder;

	public RequestStoringMinecraftInterface(MinecraftInterface realMinecraftInterface, BiomeDataJsonBuilder builder) {
		this.realMinecraftInterface = realMinecraftInterface;
		this.builder = builder;
	}

	private void store(Dimension dimension, int x, int y, int width, int height, boolean useQuarterResolution, int[] biomeData) {
		builder.store(dimension, x, y, width, height, useQuarterResolution, biomeData);
	}
	
	@Override
	public MinecraftInterface.WorldAccessHelper createAccessHelper(WorldOptions worldOptions) throws MinecraftInterfaceException {
		return new WorldAccessHelper(realMinecraftInterface.createAccessHelper(worldOptions));
	}

	@Override
	public RecognisedVersion getRecognisedVersion() {
		return realMinecraftInterface.getRecognisedVersion();
	}
	
	private class WorldAccessHelper implements MinecraftInterface.WorldAccessHelper {
		private final MinecraftInterface.WorldAccessHelper realAccessHelper;

		private WorldAccessHelper(MinecraftInterface.WorldAccessHelper realAccessHelper) {
			this.realAccessHelper = realAccessHelper;
		}
		
		@Override
		public Set<Dimension> supportedDimensions() {
			return realAccessHelper.supportedDimensions();
		}
		
		@Override
		public MinecraftInterface.WorldAccessor createWorldAccessor() throws MinecraftInterfaceException {
			return new WorldAccessor(realAccessHelper.createWorldAccessor());
		}
	}

	private class WorldAccessor implements MinecraftInterface.WorldAccessor {
		private final MinecraftInterface.WorldAccessor realWorldAccessor;

		private WorldAccessor(MinecraftInterface.WorldAccessor realWorldAccessor) {
			this.realWorldAccessor = realWorldAccessor;
		}

		@Override
		public<T> T getBiomeData(Dimension dimension, int x, int y, int width, int height,
				boolean useQuarterResolution, Function<int[], T> biomeDataMapper) throws MinecraftInterfaceException {
			return realWorldAccessor.getBiomeData(dimension, x, y, width, height, useQuarterResolution, biomeData -> {
				store(dimension, x, y, width, height, useQuarterResolution, biomeData);
				return biomeDataMapper.apply(biomeData);
			});
		}
	}
}
