package amidst.mojangapi.world.oracle.end;

import java.util.ArrayList;
import java.util.List;

import amidst.documentation.ThreadSafe;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.mojangapi.world.coordinates.Resolution;
import amidst.mojangapi.world.oracle.SimplexNoise;
import amidst.mojangapi.world.versionfeatures.DefaultBiomes;

import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;

@ThreadSafe
public class EndIslandOracle {
	public static EndIslandOracle from(long seed) {
		return new EndIslandOracle(createNoiseFunction(seed), seed);
	}

	/**
	 * Returns the noise function using the current seed.
	 */
	private static SimplexNoise createNoiseFunction(long seed) {
		JRand random = new JRand(seed);
		// Mimics the side-effects to the random number generator caused by Minecraft.
		// Past 1.13, it just skips the random 17292 times.
		random.advance(17292);
		return new SimplexNoise(random);
	}

	/**
	 * Minecraft checks 12 chunks either side of a chunk when assessing island
	 * influence.
	 */
	private static final int SURROUNDING_CHUNKS = 12;

	/**
	 * When cast to double, -0.9 will become -0.8999999761581421, which is why
	 * you might see that value in Minecraft's .jar
	 */
	private static final float ISLAND_DENSITY_THRESHOLD = -0.9f;

	/**
	 * The distance from (0;0) at which islands start to generated
	 */
	private static final int OUTER_LANDS_DISTANCE_IN_CHUNKS = 64;

	private final SimplexNoise noiseFunction;
	private final long seed;

	public EndIslandOracle(SimplexNoise noiseFunction, long seed) {
		this.noiseFunction = noiseFunction;
		this.seed = seed;
	}
	
	public static int getBiomeAtBlock(long x, long y, List<LargeEndIsland> largeIslands) {		
		if (x * x + y * y <= 1048576L) {
			return DefaultBiomes.theEnd;
		} else {
			float influence = getInfluenceAtBlock(x, y, largeIslands);
			if (influence > 40.0F) {
				return DefaultBiomes.theEndHigh;
			} else if (influence >= 0.0F) {
				return DefaultBiomes.theEndMedium;
			} else {
				return influence < -20.0F ? DefaultBiomes.theEndLow : DefaultBiomes.theEndBarren;
			}
		}
	}
	
	public static float getInfluenceAtBlock(long x, long y, List<LargeEndIsland> largeIslands) {
		float highestInfluence = -100.0f;
		
		for (LargeEndIsland island : largeIslands) {
			if (island instanceof LargeEndIsland) {
				float tempInfluence = ((LargeEndIsland) island).influenceAtBlock(x, y);
				if (tempInfluence > highestInfluence) {
					highestInfluence = tempInfluence;
				}
			}
		}
		return highestInfluence;
	}
	
	@Deprecated
	private int getBiomeAtBlock(long x, long y) {
		if (x * x + y * y <= 1048576L) {
			return DefaultBiomes.theEnd;
		} else {
			float influence = getInfluenceAtBlock(x, y);
			if (influence > 40.0F) {
				return DefaultBiomes.theEndHigh;
			} else if (influence >= 0.0F) {
				return DefaultBiomes.theEndMedium;
			} else {
				return influence < -20.0F ? DefaultBiomes.theEndLow : DefaultBiomes.theEndBarren;
			}
		}
	}
	
	@Deprecated
	private float getInfluenceAtBlock(long x, long y) {
		float highestInfluence = -100.0f;
		
		for (LargeEndIsland island : getLargeIslandsAt(new CoordinatesInWorld(x, y))) {
			if (island instanceof LargeEndIsland) {
				float tempInfluence = ((LargeEndIsland) island).influenceAtBlock(x, y);
				if (tempInfluence > highestInfluence) {
					highestInfluence = tempInfluence;
				}
			}
		}
		return highestInfluence;
	}

	public EndIslandList getAt(CoordinatesInWorld corner) {
		int steps = Resolution.CHUNK.getStepsPerFragment();
		return findSurroundingIslands(
				corner.getXAs(Resolution.CHUNK),
				corner.getYAs(Resolution.CHUNK),
				steps,
				steps);
	}
	
	/**
	 * Returns a list of all islands that might be touching a chunk-area.
	 */
	private EndIslandList findSurroundingIslands(
			long chunkX,
			long chunkY,
			int chunksPerFragmentX,
			int chunksPerFragmentY) {
		List<LargeEndIsland> largeEndIslands = findSurroundingLargeIslands(chunkX, chunkY, chunksPerFragmentX, chunksPerFragmentY);
		List<SmallEndIsland> smallEndIslands = findSurroundingSmallIslands(chunkX, chunkY, chunksPerFragmentX, chunksPerFragmentY, largeEndIslands);
		return new EndIslandList(smallEndIslands, largeEndIslands);
	}
	
	public List<LargeEndIsland> getLargeIslandsAt(CoordinatesInWorld corner) {
		int steps = Resolution.CHUNK.getStepsPerFragment();
		return findSurroundingLargeIslands(
				corner.getXAs(Resolution.CHUNK),
				corner.getYAs(Resolution.CHUNK),
				steps,
				steps);
	}

	/**
	 * Returns a list of all large islands that might be touching a chunk-area.
	 */
	private List<LargeEndIsland> findSurroundingLargeIslands(
			long chunkX,
			long chunkY,
			int chunksPerFragmentX,
			int chunksPerFragmentY) {
		List<LargeEndIsland> result = new ArrayList<>();
		for (int y = -SURROUNDING_CHUNKS; y <= chunksPerFragmentY + SURROUNDING_CHUNKS; y++) {
			for (int x = -SURROUNDING_CHUNKS; x <= chunksPerFragmentX + SURROUNDING_CHUNKS; x++) {
				LargeEndIsland island = tryCreateLargeEndIsland(chunkX + x, chunkY + y);
				if (island != null) {
					result.add(island);
				}
			}
		}
		return result;
	}

	/**
	 * Returns a LargeEndIsland if one has 'grown out' from the chunk, otherwise
	 * null
	 */
	private LargeEndIsland tryCreateLargeEndIsland(long chunkX, long chunkY) {

		if (chunkX == 0 && chunkY == 0) {
			return createMainEndIsland(chunkX, chunkY);
		} else if (!isInRange(chunkX, chunkY, OUTER_LANDS_DISTANCE_IN_CHUNKS)) {
			return tryCreateLargeEndIslandInOuterLands(chunkX, chunkY);
		} else {
			return null;
		}
	}

	/**
	 * The main island grows from the origin, with a hard-coded erosion factor
	 * of 8
	 */
	private LargeEndIsland createMainEndIsland(long chunkX, long chunkY) {
		return new LargeEndIsland(chunkX, chunkY, 8.0f);
	}

	/**
	 * The chunk is in the outer-islands band
	 */
	private LargeEndIsland tryCreateLargeEndIslandInOuterLands(long chunkX, long chunkY) {
		if (noiseFunction.noise(chunkX, chunkY) < ISLAND_DENSITY_THRESHOLD) {
			return new LargeEndIsland(chunkX, chunkY, getErosionFactor(chunkX, chunkY));
		} else {
			return null;
		}
	}

	/**
	 * An island (or part of an island) grows out from this chunk, with an
	 * erosion factor between 9 and 21 (i.e. they will be smaller than the main
	 * island).
	 */
	private int getErosionFactor(long chunkX, long chunkY) {
	    // Convert coordinates to long to guard against overflow
		return (int) ((Math.abs(chunkX) * 3439 + Math.abs(chunkY) * 147) % 13 + 9);
	}

    /**
     * Is the point (x, y) inside the disk of radius d centered at the origin?
     */
    private boolean isInRange(long x, long y, int d) {
        // Guard against overflow
        if (x < -d || x > d || y < -d || y > d)
            return false;
        return x * x + y * y <= d * d;
    }
    
	public List<SmallEndIsland> getSmallIslandsAt(CoordinatesInWorld corner) {
		int steps = Resolution.CHUNK.getStepsPerFragment();
		return findSurroundingSmallIslands(
				corner.getXAs(Resolution.CHUNK),
				corner.getYAs(Resolution.CHUNK),
				steps,
				steps);
	}
    
	@Deprecated
	private List<SmallEndIsland> findSurroundingSmallIslands(
			long chunkX,
			long chunkY,
			int chunksPerFragmentX,
			int chunksPerFragmentY) {
		List<SmallEndIsland> result = new ArrayList<>();
		for (int y = 0; y <= chunksPerFragmentY; y++) {
			for (int x = 0; x <= chunksPerFragmentX; x++) {
				addSmallIslandsInChunk(chunkX + x, chunkY + y, result);
			}
		}
		return result;
	}

	@Deprecated
    private void addSmallIslandsInChunk(long chunkX, long chunkY, List<SmallEndIsland> islands) {
		long blockX = chunkX << 4;
		long blockY = chunkY << 4;
		if (getBiomeAtBlock(blockX, blockY) == DefaultBiomes.theEndLow) {
			ChunkRand rand = new ChunkRand();
			rand.setDecoratorSeed(seed, (int) blockX, (int) blockY, 0, 3, MCVersion.v1_13);
			
			if (rand.nextInt(14) == 0) {
				long resultX = blockX + rand.nextInt(16);
				int resultH = 55 + rand.nextInt(16);
				long rexultY = blockY + rand.nextInt(16);
				islands.add(new SmallEndIsland(resultX, rexultY, resultH, 4));
				if (rand.nextInt(4) == 0) {
					resultX = blockX + rand.nextInt(16);
					resultH = 55 + rand.nextInt(16);
					rexultY = blockY + rand.nextInt(16);
					islands.add(new SmallEndIsland(resultX, rexultY, resultH, 4));
				}
			}
		}
	}
	
	private List<SmallEndIsland> findSurroundingSmallIslands(
			long chunkX,
			long chunkY,
			int chunksPerFragmentX,
			int chunksPerFragmentY,
			List<LargeEndIsland> largeIslands) {
		List<SmallEndIsland> result = new ArrayList<>();
		for (int y = 0; y <= chunksPerFragmentY; y++) {
			for (int x = 0; x <= chunksPerFragmentX; x++) {
				addSmallIslandsInChunk(chunkX + x, chunkY + y, result, largeIslands);
			}
		}
		return result;
	}
    
    private void addSmallIslandsInChunk(long chunkX, long chunkY, List<SmallEndIsland> smallIslands, List<LargeEndIsland> largeIslands) {
		long blockX = chunkX << 4;
		long blockY = chunkY << 4;
		if (getBiomeAtBlock(blockX, blockY, largeIslands) == DefaultBiomes.theEndLow) {
			ChunkRand rand = new ChunkRand();
			rand.setDecoratorSeed(seed, (int) blockX, (int) blockY, 0, 0, MCVersion.v1_13);
			
			if (rand.nextInt(14) == 0) {
				long resultX = blockX + rand.nextInt(16);
				int resultH = 55 + rand.nextInt(16);
				long rexultY = blockY + rand.nextInt(16);
				smallIslands.add(new SmallEndIsland(resultX, rexultY, resultH, 4));
				if (rand.nextInt(4) == 0) {
					resultX = blockX + rand.nextInt(16);
					resultH = 55 + rand.nextInt(16);
					rexultY = blockY + rand.nextInt(16);
					smallIslands.add(new SmallEndIsland(resultX, rexultY, resultH, 4));
				}
			}
		}
	}
    
}
