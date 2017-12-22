package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.MapBuilder;
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
		System.out.println();
		System.out.println("Config data");
		
		// Prepare config variables
		int mapSize = MapBuilder.map.getSize();
		int maxMapSize = mapSize * 8;
		int nativeZoom = 0;
		int maxZoom = 0;
		int minZoom = 0;
		
		int count = 0;
		for (int i = mapSize; i > 256; i++) {
			i = (i/2);
			nativeZoom = count;
			count++;
		}
		
		count = 0;
		for (int i = maxMapSize; i > 256; i++) {
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
		
		config.put("markerType", MapBuilder.propertiesManager.markerType);
		
		config.put("showDeeds", MapBuilder.propertiesManager.showDeeds);
		config.put("showGuardTowers", MapBuilder.propertiesManager.showGuardTowers);
		config.put("showStructures", MapBuilder.propertiesManager.showStructures);
		
		configObject.put("config", config);
		
		if (MapBuilder.propertiesManager.verbose) System.out.println("      creating data/config.json");
		String filePath = Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "config.json").toString();
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(configObject.toJSONString());
		writer.close();
		
		System.out.println("   OK wrote config data to config.json");
	}
	
	/**
	 * Generates a config file to be used in the PHP code necessary to connect to the RMI interface
	 */
	public static void generatePhpConfigFile() throws IOException {
		System.out.println();
		System.out.println("PHP config data");
		
		if (MapBuilder.propertiesManager.verbose) System.out.println("      creating includes/config.php");
		String filePath = Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "includes", "config.php").toString();
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(String.format("<?php\n$conf_rmi_host = '%s';\n$conf_rmi_port = '%s';\n?>",
				MapBuilder.propertiesManager.rmiHost.replace("'", "\\'"),
				MapBuilder.propertiesManager.rmiPort.replace("'", "\\'")
		));
		writer.close();
		
		System.out.println("   OK wrote config data to config.php");
	}
}
