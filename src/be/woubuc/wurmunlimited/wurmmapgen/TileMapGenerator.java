package be.woubuc.wurmunlimited.wurmmapgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;

import static be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen.properties;

public class TileMapGenerator {

	private final String separator = File.separator;
	private int threadCounter = 0;
	
	private final int tileSize = 256;
	
	public MeshIO map;
	
	/**
	 * Opens the MeshIO connection required for the map builder
	 */
	public void openMap() throws IOException {
		System.out.println("\nOpening Wurm MeshIO connection");
		map = MeshIO.open(WurmMapGen.fileManager.map_topLayer.getAbsolutePath());
		if (WurmMapGen.properties.verbose) System.out.println("   OK Connection opened");
	}
	
	/**
	 * Closes the MeshIO connection
	 */
	public void closeMap() throws IOException {
		if (WurmMapGen.properties.verbose) System.out.println("\nClose Wurm MeshIO connection");
		map.close();
		if (WurmMapGen.properties.verbose) System.out.println("   OK Connection closed");
	}
	
	/**
	 * Generates the map tile images
	 */
	public void generateMapTiles() {
		System.out.println("\nMap generation");
		final long startTime = System.currentTimeMillis();
		
		final int tileCount = (getMapSize() / tileSize);
		final int totalProcesses = (tileCount * tileCount);
		ExecutorService executor = Executors.newFixedThreadPool(properties.mapGeneratorThreads);
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
			System.out.print("      Generating map tiles " + percent + "%\r");
			try {
				synchronized (obj) {
					obj.wait(100);
				}
			} catch (InterruptedException ex) { }
		}
		
		System.out.println("   OK Map tiles generated in " + (System.currentTimeMillis() - startTime) + "ms");
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
		final BufferedImage imageTile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D imageTileGraphics = imageTile.createGraphics();
		for (int x = 0; x < tileSize; x++) {
			for (int y = 0; y < tileSize; y++) {
				final int tileX = (imageTileX * tileSize) + x;
				final int tileY = (imageTileY * tileSize) + y;
				int tileEncoded = map.getTile(tileX, tileY);
				byte tileType = Tiles.decodeType(tileEncoded);
				short tileHeight = Tiles.decodeHeight(tileEncoded);
				Tile thisTile = Tiles.getTile(tileType);
				Color tileColor = thisTile.getColor();
				imageTileGraphics.setColor(tileColor);
				imageTileGraphics.fillRect(x, y, 1, 1);
				if (properties.mapGenerateShading) {
					boolean checkPath = false;
					if (!properties.mapShadePaths) {
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
				if (properties.mapGenerateWater && tileHeight < 0) {
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
		new File(properties.saveLocation.getAbsolutePath() + separator + "images").mkdirs();
		try {
			WurmMapGen.fileManager.saveToFile(imageTile, new File(properties.saveLocation.getAbsolutePath() + separator + "images" + separator + imageTileX + "-" + imageTileY + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageTileGraphics.dispose();
		imageTile.flush();
	}

	private void drawBridges(BufferedImage imageTile, int imageTileX, int imageTileY) throws SQLException {
		if (properties.mapGenerateBridges) {
			if (WurmMapGen.db.getZones().isConnected()) {
				final int minX = (imageTileX * tileSize);
				final int minY = (imageTileY * tileSize);
				final int maxX = minX + tileSize;
				final int maxY = minY + tileSize;
				Graphics2D imageTileGraphics = imageTile.createGraphics();
				Tile thisTile = Tiles.getTile(9);
				Color tileColor = thisTile.getColor();
				imageTileGraphics.setColor(tileColor);
				Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT TILEX, TILEY FROM BRIDGEPARTS WHERE TILEX >= "+minX+" AND TILEY >= "+minY+" AND TILEX < "+maxX+" AND TILEY < "+maxY+";");
				while (resultSet.next()) {
					int tileX = resultSet.getInt("TILEX");
					int tileY = resultSet.getInt("TILEY");
					imageTileGraphics.fillRect((tileX - minX), (tileY - minY), 1, 1);
				}
				resultSet.close();
				statement.close();
				imageTileGraphics.dispose();
			}
		}
	}

}
