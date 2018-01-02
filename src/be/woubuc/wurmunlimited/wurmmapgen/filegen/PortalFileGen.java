package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import be.woubuc.wurmunlimited.wurmmapgen.database.Portal;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.file.Paths;
import java.util.ArrayList;

public final class PortalFileGen extends FileGen {
	
	// Set portals.json filename
	public PortalFileGen() {
		setFilePath(Paths.get(WurmMapGen.dataPath, "portals.json"));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String generateData() {
		Logger.title("Portal data");
		
		// Load portals
		ArrayList<Portal> portals = WurmMapGen.db.getPortals();
		
		if (portals.size() == 0) {
			Logger.custom("SKIP", "No portals found");
			return null;
		}
		// Prepare JSON objects
		JSONObject dataObject = new JSONObject();
		JSONArray data = new JSONArray();
		
		JSONObject portalData;
		for (final Portal portal : portals) {
			portalData = new JSONObject();
			
			portalData.put("name", portal.getName());
			portalData.put("x", portal.getPosX());
			portalData.put("y", portal.getPosY());
			
			data.add(portalData);
		}
		
		dataObject.put("portals", data);
		
		return dataObject.toJSONString();
	}
}
