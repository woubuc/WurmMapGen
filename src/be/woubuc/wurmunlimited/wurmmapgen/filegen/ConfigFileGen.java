package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class ConfigFileGen {
	
	/**
	 * Generates a config file
	 */
	@SuppressWarnings("unchecked")
	public static void generateConfigFile() throws IOException {
		Logger.title("JSON Config data");
		
		// Prepare config variables
		int mapSize = WurmMapGen.tileMapGenerator.map.getSize();
		int maxMapSize = mapSize * 8;
		int nativeZoom = 0;
		int maxZoom = 0;
		int minZoom = 0;
		
		int count = 0;
		for (int i = mapSize; i > WurmMapGen.properties.mapTileSize; i++) {
			i = (i/2);
			nativeZoom = count;
			count++;
		}
		
		count = 0;
		for (int i = maxMapSize; i > WurmMapGen.properties.mapTileSize; i++) {
			i = (i/2);
			maxZoom = count;
			count++;
		}
		minZoom = (maxZoom - 5);
		
		// Write config values to file
		JSONObject configObject = new JSONObject();
		JSONObject config = new JSONObject();
		
		config.put("nativeZoom", nativeZoom);
		config.put("mapMinZoom", minZoom);
		config.put("mapMaxZoom", maxZoom);
		config.put("actualMapSize", mapSize);
		config.put("maxMapSize", maxMapSize);
		config.put("mapTileSize", WurmMapGen.properties.mapTileSize);
		
		config.put("markerType", WurmMapGen.properties.markerType);
		
		config.put("showDeeds", WurmMapGen.properties.showDeeds);
		config.put("showGuardTowers", WurmMapGen.properties.showGuardTowers);
		config.put("showStructures", WurmMapGen.properties.showStructures);
		
		configObject.put("config", config);
		
		Logger.details("Creating data/config.json");
		String filePath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data", "config.json").toString();
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(configObject.toJSONString());
		writer.close();
		
		Logger.ok("Wrote config data to config.json");
	}
	
	/**
	 * Generates a config file to be used in the PHP code necessary to connect to the RMI interface
	 */
	public static void generatePhpConfigFile() throws IOException {
		Logger.title("PHP config data");
		
		Logger.details("Creating includes/config.php");
		String filePath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "includes", "config.php").toString();
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(String.format("<?php\n$conf_rmi_host = '%s';\n$conf_rmi_port = '%s';\n?>",
				WurmMapGen.properties.rmiHost.replace("'", "\\'"),
				WurmMapGen.properties.rmiPort.replace("'", "\\'")
		));
		writer.close();
		
		Logger.ok("Wrote config data to config.php");
	}
}
