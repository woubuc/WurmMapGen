package com.imraginbro.wurm.mapgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.IntStream;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;

public class MapGen {

	final static String newLine = System.lineSeparator();
	final static String separator = java.io.File.separator;

	static File wurmMapLocation = null;
	static File saveLocation = null;

	static File map_topLayer = null;
	static File db_wurmZones = null;
	static File db_wurmItems = null;

	static File[] fileBackupArray = new File[3];

	//vars for map gen
	final static boolean gen_map_shading = true;
	final static boolean gen_map_avoid_shading_paths = false;
	final static boolean gen_map_water = true;
	final static boolean gen_map_bridges = true;

	public static void main(String[] args) throws Exception {
		
		wurmMapLocation = new File(args[0]);
		saveLocation = new File(args[1]);
		
		if (wurmMapLocation == null || saveLocation == null) {
			return;
		}
		
		map_topLayer = new File(wurmMapLocation.getAbsolutePath() + separator + "top_layer.map");
		db_wurmZones = new File(wurmMapLocation.getAbsolutePath() + separator + "sqlite" + separator + "wurmzones.db");
		db_wurmItems = new File(wurmMapLocation.getAbsolutePath() + separator + "sqlite" + separator + "wurmitems.db");
		
		fileBackupArray[0] = map_topLayer;
		fileBackupArray[1] = db_wurmZones;
		fileBackupArray[2] = db_wurmItems;
		
		final long startTime = System.currentTimeMillis();
		
		FileManagement.makeTempCopies(fileBackupArray, saveLocation);
		FileManagement.relocateFileVars();
		
		System.out.println("Loading top_layer.map file...");
		MeshIO map = MeshIO.open(map_topLayer.getAbsolutePath());
		
		FileManagement.extractRescources("/resources/required.zip", saveLocation);
		FileGeneration.generateFiles(map);
		
		genImages(map);
		map.close();
		
		System.out.println("Removing temporary files...");
		FileManagement.deleteDir(new File(saveLocation.getAbsolutePath() + separator + "tmp"));
		
		final long endTime = System.currentTimeMillis();
		final long totalTime = (endTime - startTime)/1000;
		System.out.println("Finished with map generation! " + totalTime + " seconds.");
	}

	public static void genImages(MeshIO map) throws IllegalArgumentException, ImagingOpException, IOException, SQLException {
		System.out.println("Generating image for map");
		BufferedImage original = genMap(map, 1);
		genTileMap(original);
		original.flush();
	}

	public static BufferedImage genMap(MeshIO map, int PIXEL_SIZE) throws SQLException {

		int MAP_SIZE = map.getSize();
		int IMAGE_SIZE = PIXEL_SIZE * MAP_SIZE;

		BufferedImage mapImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g_mapImage = (Graphics2D) mapImage.getGraphics();

		for (int y = 0; y < MAP_SIZE; y++) {
			if (y % 32 == 0) {
				int percent = (int)((float)y / (float)MAP_SIZE * 100.0f);
				System.out.print("Percent completed: " + percent + "%\r");
			}
			for (int x = 0; x < MAP_SIZE; x++) {
				int newX = x * PIXEL_SIZE;
				int newY = y * PIXEL_SIZE;
				int tileEncoded = map.getTile(x, y);
				byte tileType = Tiles.decodeType(tileEncoded);
				short tileHeight = Tiles.decodeHeight(tileEncoded);
				Tile thisTile = Tiles.getTile(tileType);
				Color tileColor = thisTile.getColor();
				g_mapImage.setColor(tileColor);
				g_mapImage.fillRect(newX, newY, PIXEL_SIZE, PIXEL_SIZE);
				if (gen_map_shading) {
					boolean checkPath = false;
					if (gen_map_avoid_shading_paths) {
						final int[] path_tile_types = {
								Tiles.TILE_TYPE_COBBLESTONE, Tiles.TILE_TYPE_COBBLESTONE_ROUND,
								Tiles.TILE_TYPE_MARBLE_BRICKS, Tiles.TILE_TYPE_MARBLE_SLABS,
								Tiles.TILE_TYPE_SANDSTONE_BRICKS, Tiles.TILE_TYPE_SANDSTONE_SLABS,
								Tiles.TILE_TYPE_SLATE_BRICKS, Tiles.TILE_TYPE_SLATE_SLABS,
								Tiles.TILE_TYPE_STONE_SLABS};
						checkPath = IntStream.of(path_tile_types).anyMatch(n -> n == tileType);
					}
					if (!checkPath) {
						if (x > 0 && y > 0) {
							int lastTileEncoded = map.getTile(x - 1, y - 1);
							short lastTileHeight = Tiles.decodeHeight(lastTileEncoded);
							int calc = lastTileHeight - tileHeight;
							if (calc > 0) {
								int alpha = (int) Math.round(Math.pow(calc, 0.95));
								if (alpha > 255) {
									alpha = 255;
								}
								g_mapImage.setColor(new Color(0,0,0,alpha));
								g_mapImage.fillRect(newX - 1, newY - 1, PIXEL_SIZE, PIXEL_SIZE);
							} else {
								calc = -calc;
								int alpha = (int) Math.round(Math.pow(calc, 0.8));
								if (alpha > 255) {
									alpha = 255;
								}
								g_mapImage.setColor(new Color(255,255,255,alpha));
								g_mapImage.fillRect(newX - 1, newY - 1, PIXEL_SIZE, PIXEL_SIZE);
							}
						}
					}
				}
				if (gen_map_water && tileHeight < 0) {
					g_mapImage.setColor(new Color(20,80,180,210));
					g_mapImage.fillRect(newX, newY, PIXEL_SIZE, PIXEL_SIZE);
				}
			}
		}
		System.out.println("Image generation complete");
		drawBridges(g_mapImage, PIXEL_SIZE);
		g_mapImage.dispose();
		return mapImage;
	}

	public static void drawBridges(Graphics2D g, int PIXEL_SIZE) throws SQLException {
		if (gen_map_bridges) {
			System.out.println("Loading bridges from wurmzones.db...");
			Tile thisTile = Tiles.getTile(9);
			Color tileColor = thisTile.getColor();
			g.setColor(tileColor);
			Connection connection = DriverManager.getConnection("jdbc:sqlite:"+db_wurmZones);
			Statement statement = connection.createStatement();  
			ResultSet resultSet = statement.executeQuery("SELECT * FROM BRIDGEPARTS;"); 
			int count = 0;
			System.out.println("Drawing bridges...");
			while (resultSet.next()) {
				int tileX = resultSet.getInt("TILEX") * PIXEL_SIZE;
				int tileY = resultSet.getInt("TILEY") * PIXEL_SIZE;
				g.fillRect(tileX, tileY, PIXEL_SIZE, PIXEL_SIZE);
				count++;
			}
			connection.close();
			System.out.println("Finished drawing " + count + " bridge tiles");
		}
	}

	public static void genTileMap(BufferedImage img) throws IOException {
		System.out.println("Creating tile map from image...");
		int subImg = img.getWidth() / 256;
		for (int x = 0; x < subImg; x++) {
			for (int y = 0; y < subImg; y++) {
				new File(saveLocation.getAbsolutePath() + separator + "images").mkdirs();
				BufferedImage newImg = img.getSubimage(x * 256, y * 256, 256, 256);
				FileManagement.saveToFile(newImg, new File(saveLocation.getAbsolutePath() + separator + "images" + separator + x + "-" + y + ".png"));
				newImg.flush();
			}
		}
		img.flush();
	}

}
