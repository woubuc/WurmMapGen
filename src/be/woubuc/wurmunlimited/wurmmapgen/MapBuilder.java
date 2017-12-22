package be.woubuc.wurmunlimited.wurmmapgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import be.woubuc.wurmunlimited.wurmmapgen.filegen.FileGen;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;

public class MapBuilder {

	private final String separator = File.separator;
	private int threadCounter = 0;
	private int bridgeTileCount = 0;
	
	public final static PropertiesManager propertiesManager = new PropertiesManager();
	public final static FileManager fileManager = new FileManager();
	public final static DBHandler dbhandler = new DBHandler();
	
	private final static TemplateHandler templateHandler = new TemplateHandler();
	private final static FileGen fileGenerator = new FileGen();

	public static MeshIO map;
	
	/**
	 * Initialises the map builder
	 * @param  propertiesFilePath  Path to the properties file that should be used
	 */
	public MapBuilder(Path propertiesFilePath) throws Exception {
		if (!propertiesManager.load(propertiesFilePath)) {
			throw new Exception("Could not load properties");
		}
	}
	
	/**
	 * Builds the map
	 */
	public void buildMap() throws Exception {
		fileManager.load();
		fileManager.makeTempCopies();
		fileManager.relocateFileVars();
		
		System.out.println("\nWurm MeshIO operations");
		map = MeshIO.open(fileManager.map_topLayer.getAbsolutePath());
		
		dbhandler.load();
		
		System.out.println("\nMap generation");
		start();
		
		templateHandler.copyAssets();
		templateHandler.render();
		
		fileGenerator.generateFiles();
		dbhandler.closeConnections();
		
		map.close();
		System.out.println("\nRemove temp files");
		fileManager.deleteDir(new File(propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp"));
		System.out.println("   OK Temporary files removed");
	}

	private void start() {
		final int tileCount = (getMapSize() / 256);
		final int totalProcesses = (tileCount * tileCount);
		ExecutorService executor = Executors.newFixedThreadPool(propertiesManager.mapGeneratorThreads);
		for (int x = 0; x < tileCount; x++) {
			for (int y = 0; y < tileCount; y++) {
				threadCounter++;
				Runnable mtt = new MapTileThreader(x, y);
				executor.execute(mtt);
			}
		}
		executor.shutdown();
		Object obj = new Object();
		while (!executor.isTerminated()) {
			int percent = (int)((float)(totalProcesses - threadCounter) / (float)(totalProcesses) * 100.0f);
			System.out.print("      Completion percent: " + percent + "%\r");
			try {
				synchronized (obj) {
					obj.wait(100);
				}
			} catch (InterruptedException ex) { }
		}
		System.out.println("      Completion percent: 100%");
		if (propertiesManager.verbose) { System.out.println("      Found " + bridgeTileCount + " bridge tiles to draw"); }
		System.out.println("   OK Map generation complete!");
	}

	private class MapTileThreader implements Runnable {

		private final int x;
		private final int y;

		private MapTileThreader(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void run() {
			generateImageTile(x, y);
			threadCounter--;
		}

	}

	private int getMapSize() {
		return map.getSize();
	}

	private void generateImageTile(final int imageTileX, final int imageTileY) {
		final BufferedImage imageTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		Graphics2D imageTileGraphics = imageTile.createGraphics();
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++) {
				final int tileX = (imageTileX * 256) + x;
				final int tileY = (imageTileY * 256) + y;
				int tileEncoded = map.getTile(tileX, tileY);
				byte tileType = Tiles.decodeType(tileEncoded);
				short tileHeight = Tiles.decodeHeight(tileEncoded);
				Tile thisTile = Tiles.getTile(tileType);
				Color tileColor = thisTile.getColor();
				imageTileGraphics.setColor(tileColor);
				imageTileGraphics.fillRect(x, y, 1, 1);
				if (propertiesManager.mapGenerateShading) {
					boolean checkPath = false;
					if (!propertiesManager.mapShadePaths) {
						final int[] path_tile_types = {
								Tiles.TILE_TYPE_COBBLESTONE, Tiles.TILE_TYPE_COBBLESTONE_ROUND,
								Tiles.TILE_TYPE_MARBLE_BRICKS, Tiles.TILE_TYPE_MARBLE_SLABS,
								Tiles.TILE_TYPE_SANDSTONE_BRICKS, Tiles.TILE_TYPE_SANDSTONE_SLABS,
								Tiles.TILE_TYPE_SLATE_BRICKS, Tiles.TILE_TYPE_SLATE_SLABS,
								Tiles.TILE_TYPE_STONE_SLABS};
						checkPath = IntStream.of(path_tile_types).anyMatch(n -> n == tileType);
					}
					if (!checkPath) {
						if (tileX < (getMapSize()-1) && tileY < (getMapSize()-1)) {
							int lastTileEncoded = map.getTile(tileX + 1, tileY + 1);
							short nextTileHeight = Tiles.decodeHeight(lastTileEncoded);
							int calc = tileHeight - nextTileHeight;
							if (calc > 0) {
								int alpha = (int) Math.round(Math.pow(calc, 0.95));
								if (alpha > 255) {
									alpha = 255;
								}
								imageTileGraphics.setColor(new Color(0,0,0,alpha));
								imageTileGraphics.fillRect(x, y, 1, 1);
							} else {
								calc = -calc;
								int alpha = (int) Math.round(Math.pow(calc, 0.8));
								if (alpha > 255) {
									alpha = 255;
								}
								imageTileGraphics.setColor(new Color(255,255,255,alpha));
								imageTileGraphics.fillRect(x, y, 1, 1);
							}
						}
					}
				}
				if (propertiesManager.mapGenerateWater && tileHeight < 0) {
					imageTileGraphics.setColor(new Color(20,80,180,210));
					imageTileGraphics.fillRect(x, y, 1, 1);
				}
			}
		}
		try {
			drawBridges(imageTile, imageTileX, imageTileY);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		new File(propertiesManager.saveLocation.getAbsolutePath() + separator + "images").mkdirs();
		try {
			fileManager.saveToFile(imageTile, new File(propertiesManager.saveLocation.getAbsolutePath() + separator + "images" + separator + imageTileX + "-" + imageTileY + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageTileGraphics.dispose();
		imageTile.flush();
	}

	private void drawBridges(BufferedImage imageTile, int imageTileX, int imageTileY) throws SQLException {
		if (propertiesManager.mapGenerateBridges) {
			if (dbhandler.checkZonesConnection()) {
				final int minX = (imageTileX * 256);
				final int minY = (imageTileY * 256);
				final int maxX = minX + 256;
				final int maxY = minY + 256;
				Graphics2D imageTileGraphics = imageTile.createGraphics();
				Tile thisTile = Tiles.getTile(9);
				Color tileColor = thisTile.getColor();
				imageTileGraphics.setColor(tileColor);
				Statement statement = dbhandler.getZonesConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT TILEX, TILEY FROM BRIDGEPARTS WHERE TILEX >= "+minX+" AND TILEY >= "+minY+" AND TILEX < "+maxX+" AND TILEY < "+maxY+";");
				while (resultSet.next()) {
					int tileX = resultSet.getInt("TILEX");
					int tileY = resultSet.getInt("TILEY");
					imageTileGraphics.fillRect((tileX - minX), (tileY - minY), 1, 1);
					bridgeTileCount++;
				}
				resultSet.close();
				statement.close();
				imageTileGraphics.dispose();
			}
		}
	}

}
