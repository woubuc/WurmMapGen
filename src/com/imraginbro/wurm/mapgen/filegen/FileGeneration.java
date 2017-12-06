package com.imraginbro.wurm.mapgen.filegen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.imraginbro.wurm.mapgen.MapBuilder;
import com.wurmonline.mesh.MeshIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FileGeneration {

	private final static String newLine = System.lineSeparator();
	private final static String separator = java.io.File.separator;

	private int html_nativeZoom = 0;
	private int html_mapMinZoom = 0;
	private int html_mapMaxZoom = 0;
	private int html_actualMapSize = 0;
	private int html_maxMapSize = 0;

	public void generateFiles() throws IOException, SQLException {
		setHTMLvars(MapBuilder.map);
		generateDeedsFile();
		generateGuardTowersFile();
		generateStructuresFile();
		generateConfigFile();
	}
	
	/**
	 * Generates the structures JSON data and writes it to data/structures.json
	 */
	@SuppressWarnings("unchecked")
	private void generateStructuresFile() throws IOException, SQLException {
		System.out.println();
		System.out.println("Structure data");
		
		// Check if we should be loading structures at all
		if (!MapBuilder.propertiesManager.showStructures) {
			System.out.println("  SKIP show structures is disabled");
			return;
		}
		
		// Check if we're connected to the necessary databases
		if (!MapBuilder.dbhandler.checkZonesConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.err.println("  WARN could not connect to one or more databases");
			return;
		}
		
		// Prepare JSON container object
		JSONObject dataObject = new JSONObject();
		JSONArray data = new JSONArray();

		if (MapBuilder.propertiesManager.verbose) System.out.println("       loading structures from wurmzones.db");
		Statement statement = MapBuilder.dbhandler.getZonesConnection().createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT WURMID FROM STRUCTURES WHERE FINISHED='1';");
		
		// Keep track of the number of structures loaded
		int count = 0;
		
		// Prepare structure JSON objects
		JSONObject structureData;
		JSONArray structureBorders;
		
		while (resultSet.next()) {
			count++;
			
			// Create JSON objects that will contain structure data
			structureData = new JSONObject();
			structureBorders = new JSONArray();
			
			long structureID = resultSet.getLong("WURMID");
			Structure structure = new Structure(structureID);
			
			// Add borders to JSON data
			structureBorders.add(structure.getMinX());
			structureBorders.add(structure.getMinY());
			structureBorders.add(structure.getMaxX());
			structureBorders.add(structure.getMaxY());
			structureData.put("borders", structureBorders);
			
			// Add creator to JSON data
			structureData.put("creator", structure.getOwnerName());
			data.add(structureData);
		}

		resultSet.close();
		statement.close();
		
		if (count == 0) {
			System.out.println("  SKIP no structures found");
			return;
		}
		
		dataObject.put("structures", data);
		
		// Write JSON data to file
		if (MapBuilder.propertiesManager.verbose) System.out.println("       writing data/structures.json");
		FileWriter writer = new FileWriter(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "structures.json").toString(), false);
		writer.write(dataObject.toJSONString());
		writer.close();

		System.out.println("    OK added " + count + " entries to structures.json");
	}
	
	/**
	 * Generates the guard tower JSON data and writes it to data/guardtowers.json
	 */
	@SuppressWarnings("unchecked")
	private void generateGuardTowersFile() throws IOException, SQLException {
		System.out.println();
		System.out.println("Guard tower data");
		
		// Check if guard towers aren't disabled
		if (!MapBuilder.propertiesManager.showGuardTowers) {
			System.out.println("  SKIP show guard towers is disabled");
			return;
		}
		
		// Check if we're connected to the necessary databases
		if (!MapBuilder.dbhandler.checkItemsConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.err.println("  WARN could not connect to one or more databases");
			return;
		}
		
		if (MapBuilder.propertiesManager.verbose) System.out.println("       loading guard towers from wurmitems.db");
		Statement statement = MapBuilder.dbhandler.getItemsConnection().createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM ITEMS WHERE "
				+ "(TEMPLATEID='384' OR TEMPLATEID='430' OR TEMPLATEID='528' OR TEMPLATEID='638' OR TEMPLATEID='996') AND CREATIONSTATE='0';");

		ArrayList<GuardTower> guardTowers = new ArrayList<>();

		while (resultSet.next()) {
			guardTowers.add(new GuardTower(resultSet.getLong("LASTOWNERID"),
					(int) Math.floor(resultSet.getInt("POSX")/4),
					(int) Math.floor(resultSet.getInt("POSY")/4),
					resultSet.getFloat("QUALITYLEVEL"), resultSet.getFloat("DAMAGE")));
		}

		resultSet.close();
		statement.close();
		
		if (guardTowers.size() == 0) {
			System.out.println("  SKIP no guard towers found");
			return;
		}

		final DecimalFormat f = new DecimalFormat("0.00");
		
		// Prepare JSON objects
		JSONObject dataObject = new JSONObject();
		JSONArray data = new JSONArray();
		
		JSONObject towerData;
		JSONArray towerBorders;
		
		for (final GuardTower guardTower : guardTowers) {
			towerData = new JSONObject();
			towerBorders = new JSONArray();
			
			towerBorders.add(guardTower.getMinX());
			towerBorders.add(guardTower.getMinY());
			towerBorders.add(guardTower.getMaxX());
			towerBorders.add(guardTower.getMaxY());
			towerData.put("borders", towerBorders);
			
			towerData.put("x", guardTower.getX());
			towerData.put("y", guardTower.getY());
			
			towerData.put("creator", guardTower.getOwnerName());
			towerData.put("ql", f.format(guardTower.getQL()));
			towerData.put("dmg", f.format(guardTower.getDMG()));
			
			data.add(towerData);
		}
		
		dataObject.put("guardtowers", data);
		
		// Write JSON data to file
		if (MapBuilder.propertiesManager.verbose) System.out.println("       creating data/guardtowers.json");
		FileWriter writer = new FileWriter(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "guardtowers.json").toString(), false);
		writer.write(dataObject.toJSONString());
		writer.close();

		System.out.println("    OK added " + guardTowers.size() + " entries to guardtowers.json");
	}
	
	/**
	 * Generates deeds JSON data and writes it to data/deeds.json
	 */
	@SuppressWarnings("unchecked")
	private void generateDeedsFile() throws IOException, SQLException {
		System.out.println();
		System.out.println("Deeds data");
		
		// Check if guard towers aren't disabled
		if (!MapBuilder.propertiesManager.showDeeds) {
			System.out.println("  SKIP show deeds is disabled");
			return;
		}
		
		// Check if we're connected to the necessary databases
		if (!MapBuilder.dbhandler.checkZonesConnection() || !MapBuilder.dbhandler.checkItemsConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.err.println("  WARN could not connect to one or more databases");
			return;
		}
		
		if (MapBuilder.propertiesManager.verbose) System.out.println("       loading deeds from wurmzones.db");
		Statement statement = MapBuilder.dbhandler.getZonesConnection().createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT ID FROM VILLAGES WHERE DISBANDED=0;");
		
		ArrayList<Village> villages = new ArrayList<Village>();
		
		while (resultSet.next()) {
			villages.add(new Village(resultSet.getInt("ID")));
		}
		
		resultSet.close();
		statement.close();
		
		if (villages.size() == 0) {
			System.out.println("  SKIP no deeds found");
			return;
		}
		
		final DecimalFormat f = new DecimalFormat("0.00");
		
		// Prepare JSON objects
		JSONObject dataObject = new JSONObject();
		JSONArray data = new JSONArray();
		
		JSONObject deedData;
		JSONArray deedBorders;
		
		for (final Village village : villages) {
			deedData = new JSONObject();
			deedBorders = new JSONArray();
			
			deedBorders.add(village.getStartX());
			deedBorders.add(village.getStartY());
			deedBorders.add(village.getEndX());
			deedBorders.add(village.getEndY());
			deedData.put("borders", deedBorders);
			
			deedData.put("name", village.getVillageName());
			deedData.put("motto", village.getMotto());
			deedData.put("permanent", village.isPermanent());
			
			deedData.put("x", village.getTokenX() + 0.5);
			deedData.put("y", village.getTokenY() + 0.5);
			
			deedData.put("mayor", village.getMayorName());
			deedData.put("citizens", village.getCitizenCount());
			
			data.add(deedData);
		}
		
		dataObject.put("deeds", data);
		
		// Write JSON data to file
		if (MapBuilder.propertiesManager.verbose) System.out.println("       creating data/deeds.json");
		FileWriter writer = new FileWriter(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "deeds.json").toString(), false);
		writer.write(dataObject.toJSONString());
		writer.close();
		
		System.out.println("    OK added " + villages.size() + " entries to deeds.json");
	}

	public void generateConfigFile() throws IOException {
		System.out.println();
		System.out.println("Config data");
		
		JSONObject configObject = new JSONObject();
		JSONObject config = new JSONObject();
		
		config.put("nativeZoom", html_nativeZoom);
		config.put("mapMinZoom", html_mapMinZoom);
		config.put("mapMaxZoom", html_mapMaxZoom);
		config.put("actualMapSize", html_actualMapSize);
		config.put("maxMapSize", html_maxMapSize);
		
		configObject.put("config", config);
		
		if (MapBuilder.propertiesManager.verbose) System.out.println("       creating data/config.json");
		FileWriter writer = new FileWriter(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "config.json").toString(), false);
		writer.write(configObject.toJSONString());
		writer.close();
		
		System.out.println("    OK wrote config data to config.json");
	}

	public void setHTMLvars(MeshIO map) {
		System.out.println();
		System.out.println("Generating config.js variables...");
		html_actualMapSize = map.getSize();
		html_maxMapSize = html_actualMapSize * 8;
		int count = 0;
		for (int i = html_actualMapSize; i > 256; i++) {
			i = (i/2);
			html_nativeZoom = count;
			count++;
		}
		count = 0;
		for (int i = html_maxMapSize; i > 256; i++) {
			i = (i/2);
			html_mapMaxZoom = count;
			count++;
		}
		html_mapMinZoom = (html_mapMaxZoom - 5);
	}

}
