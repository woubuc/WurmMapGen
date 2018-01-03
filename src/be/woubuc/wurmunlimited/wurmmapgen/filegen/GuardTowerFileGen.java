package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import be.woubuc.wurmunlimited.wurmmapgen.database.GuardTower;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;

public final class GuardTowerFileGen extends FileGen {
	
	private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	
	public GuardTowerFileGen() {
		setFilePath(Paths.get(WurmMapGen.dataPath, "guardtowers.json"));
	}
	
	/**
	 * Generates a JSON file containing all guard towers on the server, and writes it to the given file path.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected String generateData() {
		Logger.title("Guard tower data");
		
		ArrayList<GuardTower> guardTowers = WurmMapGen.db.getGuardTowers();
		
		if (guardTowers.size() == 0) {
			Logger.custom("SKIP", "No guard towers found");
			return null;
		}
		
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
			
			towerData.put("x", guardTower.getPosX());
			towerData.put("y", guardTower.getPosY());
			
			towerData.put("creator", guardTower.getOwnerName());
			towerData.put("ql", decimalFormat.format(guardTower.getQl()));
			towerData.put("dmg", decimalFormat.format(guardTower.getDmg()));
			
			data.add(towerData);
		}
		
		dataObject.put("guardtowers", data);
		
		return dataObject.toJSONString();
	}
}
