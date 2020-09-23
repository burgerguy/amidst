package amidst.mojangapi.minecraftinterface;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import amidst.mojangapi.minecraftinterface.MinecraftInterface.WorldAccessor;
import amidst.mojangapi.world.Dimension;
import amidst.util.FaillibleFunction;

public class ThreadedWorldAccessor implements WorldAccessor {
	private final ThreadLocal<WorldAccessor> rawThreadedAccessor;
	
	public ThreadedWorldAccessor(FaillibleFunction<Void, WorldAccessor, MinecraftInterfaceException> innerAccessorFactory)
			throws MinecraftInterfaceException {
		// this is done so we immediately get an exception thrown if something is wrong with the interface
		AtomicReference<WorldAccessor> initialWorld = new AtomicReference<>(innerAccessorFactory.apply(null));
		
		this.rawThreadedAccessor = ThreadLocal.withInitial(() -> {
			WorldAccessor world = initialWorld.getAndSet(null);
			if (world == null) {
				try {
					return innerAccessorFactory.apply(null);
				} catch (MinecraftInterfaceException e) {
					return null;
				}
			} else {
				return world;
			}
		});
	}

	@Override
	public<T> T getBiomeData(Dimension dimension, int x, int y, int width, int height, boolean useQuarterResolution,
			Function<int[], T> biomeDataMapper) throws MinecraftInterfaceException {
		return rawThreadedAccessor.get().getBiomeData(dimension, x, y, width, height, useQuarterResolution, biomeDataMapper);
	}
}
