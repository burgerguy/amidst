package amidst.gui.menu;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import amidst.Application;
import amidst.gui.MainWindow;
import amidst.gui.SkinLoader;
import amidst.gui.UpdatePrompt;
import amidst.gui.worldsurroundings.WorldSurroundings;
import amidst.logging.Log;
import amidst.mojangapi.MojangApi;
import amidst.mojangapi.world.CoordinatesInWorld;
import amidst.mojangapi.world.WorldType;
import amidst.mojangapi.world.icon.WorldIcon;
import amidst.preferences.BiomeColorProfile;
import amidst.preferences.BiomeColorProfileSelection;

public class MenuActions {
	private static final String ABOUT_MESSAGE = "Advanced Minecraft Interfacing and Data/Structure Tracking (AMIDST)\n"
			+ "By Skidoodle (amidst.project@gmail.com)";

	private final Application application;
	private final MojangApi mojangApi;
	private final MainWindow mainWindow;
	private final AtomicReference<WorldSurroundings> worldSurroundings;
	private final SkinLoader skinLoader;
	private final UpdatePrompt updatePrompt;
	private final BiomeColorProfileSelection biomeColorProfileSelection;

	public MenuActions(Application application, MojangApi mojangApi,
			MainWindow mainWindow,
			AtomicReference<WorldSurroundings> worldSurroundings,
			SkinLoader skinLoader, UpdatePrompt updatePrompt,
			BiomeColorProfileSelection biomeColorProfileSelection) {
		this.application = application;
		this.mojangApi = mojangApi;
		this.mainWindow = mainWindow;
		this.worldSurroundings = worldSurroundings;
		this.skinLoader = skinLoader;
		this.updatePrompt = updatePrompt;
		this.biomeColorProfileSelection = biomeColorProfileSelection;
	}

	public void savePlayerLocations() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.savePlayerLocations();
		}
	}

	public void reloadPlayerLocations() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.reloadPlayerLocations(skinLoader);
		}
	}

	public void switchProfile() {
		application.displayVersionSelectWindow();
	}

	public void exit() {
		application.exitGracefully();
	}

	public void newFromSeed() {
		String seed = mainWindow.askForSeed();
		if (seed != null) {
			if (seed.isEmpty()) {
				newFromRandom();
			} else {
				WorldType worldType = mainWindow.askForWorldType();
				if (worldType != null) {
					try {
						mainWindow.setWorld(mojangApi.createWorldFromSeed(seed,
								worldType));
					} catch (IllegalStateException e) {
						mainWindow.displayException(e);
					}
				}
			}
		}
	}

	public void newFromRandom() {
		WorldType worldType = mainWindow.askForWorldType();
		if (worldType != null) {
			try {
				mainWindow.setWorld(mojangApi.createRandomWorld(worldType));
			} catch (IllegalStateException e) {
				mainWindow.displayException(e);
			}
		}
	}

	public void newFromFileOrFolder() {
		File file = mainWindow.askForMinecraftWorldFile();
		if (file != null) {
			try {
				mainWindow.setWorld(mojangApi.createWorldFromFile(file));
			} catch (Exception e) {
				mainWindow.displayException(e);
			}
		}
	}

	public void findStronghold() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			WorldIcon stronghold = mainWindow.askForOptions("Go to",
					"Select Stronghold:",
					worldSurroundings.getStrongholdWorldIcons());
			if (stronghold != null) {
				worldSurroundings.centerOn(stronghold.getCoordinates());
			}
		}
	}

	public void gotoCoordinate() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			CoordinatesInWorld coordinates = mainWindow.askForCoordinates();
			if (coordinates != null) {
				worldSurroundings.centerOn(coordinates);
			} else {
				mainWindow.displayMessage("You entered an invalid location.");
				Log.w("Invalid location entered, ignoring.");
			}
		}
	}

	public void gotoPlayer() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			List<WorldIcon> playerWorldIcons = worldSurroundings
					.getPlayerWorldIcons();
			if (!playerWorldIcons.isEmpty()) {
				WorldIcon player = mainWindow.askForOptions("Go to",
						"Select player:", playerWorldIcons);
				if (player != null) {
					worldSurroundings.centerOn(player.getCoordinates());
				}
			} else {
				mainWindow
						.displayMessage("There are no players in this world.");
			}
		}
	}

	public void capture() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			BufferedImage image = worldSurroundings.createCaptureImage();
			File file = mainWindow.askForScreenshotSaveFile();
			if (file != null) {
				saveImageToFile(image, file);
			}
			image.flush();
		}
	}

	public void copySeedToClipboard() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			String seed = "" + worldSurroundings.getSeed();
			StringSelection selection = new StringSelection(seed);
			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(selection, selection);
		}
	}

	public void selectBiomeColorProfile(BiomeColorProfile profile) {
		biomeColorProfileSelection.setProfile(profile);
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.reloadBiomeLayer();
		}
	}

	public void checkForUpdates() {
		updatePrompt.check(mainWindow);
	}

	public void viewLicense() {
		application.displayLicenseWindow();
	}

	public void about() {
		mainWindow.displayMessage(ABOUT_MESSAGE);
	}

	public void adjustZoom(int notches) {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.adjustZoom(notches);
		}
	}

	private void saveImageToFile(BufferedImage image, File file) {
		try {
			ImageIO.write(image, "png", appendPNGFileExtensionIfNecessary(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File appendPNGFileExtensionIfNecessary(File file) {
		String filename = file.toString();
		if (!filename.toLowerCase().endsWith(".png")) {
			filename += ".png";
		}
		return new File(filename);
	}

	public void howCanIMoveAPlayer() {
		mainWindow
				.displayMessage("If you load the world from a minecraft save folder, you can change the player locations.\n"
						+ "1. Scroll the map to and right-click on the new player location.\n"
						+ "2. Select the player you want to move to the new location.\n"
						+ "3. Enter the new player height (y-coordinate).\n"
						+ "4. Save player locations.\n\n"
						+ "WARNING: This will change the contents of the save folder, so there is a chance that the world gets corrupted.\n"
						+ "We try to minimize the risk by creating a backup of the changed file, before it is changed.\n"
						+ "If the backup fails, we will not write the changes.\n"
						+ "You can find the backup files in a sub folder of the world, named 'amidst_backup'.\n"
						+ "Especially, make sure to not have the world loaded in minecraft during this process.");
	}
}
