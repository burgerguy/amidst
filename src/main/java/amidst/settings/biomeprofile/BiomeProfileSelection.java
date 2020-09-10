package amidst.settings.biomeprofile;

import java.util.concurrent.ConcurrentHashMap;

import amidst.documentation.ThreadSafe;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.world.biome.BiomeColor;
import amidst.mojangapi.world.biome.UnknownBiomeIdException;

@ThreadSafe
public class BiomeProfileSelection {
	private ConcurrentHashMap<Integer, BiomeColor> biomeColors;

	public BiomeProfileSelection(BiomeProfile biomeProfile) {
		set(biomeProfile);
	}

	public BiomeColor getBiomeColorOrUnknown(int index) {
		try {
			return getBiomeColor(index);
		} catch (UnknownBiomeIdException e) {
			// we display an error before filling the spot with a random color
			AmidstLogger.error("Unknown biome index " + index + ", using a random color.");
			BiomeColor newColor = BiomeColor.random();
			biomeColors.put(index, newColor);
			return newColor;
		}
	}

	public BiomeColor getBiomeColor(int index) throws UnknownBiomeIdException {
		BiomeColor color = biomeColors.get(index);
		if(color != null) {
			return color;
		} else {
			throw new UnknownBiomeIdException("unsupported biome index detected: " + index);
		}
	}

	public void set(BiomeProfile biomeProfile) {
		this.biomeColors = biomeProfile.createBiomeColorMap();
		AmidstLogger.info("Biome profile activated: " + biomeProfile.getName());
	}
}
