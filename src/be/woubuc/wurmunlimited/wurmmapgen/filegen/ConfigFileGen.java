package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import org.json.simple.JSONObject;

public final class ConfigFileGen extends FileGen {
	
	// Set config.json filename
	public ConfigFileGen() {
		setFileName("config.json");
	}
	
	/**
	 * Generates the config file data
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected String generateData() {
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
		
		return configObject.toJSONString();
	}
}
