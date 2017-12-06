package com.imraginbro.wurm.mapgen.filegen;

import com.imraginbro.wurm.mapgen.MapBuilder;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class ConfigFileGen {
	
	/**
	 * Generates a config file
	 * @param filePath The destination file
	 */
	@SuppressWarnings("unchecked")
	public static void generateConfigFile(String filePath) throws IOException {
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
		
		configObject.put("config", config);
		
		if (MapBuilder.propertiesManager.verbose) System.out.println("      creating data/config.json");
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(configObject.toJSONString());
		writer.close();
		
		System.out.println("   OK wrote config data to config.json");
	}
}
