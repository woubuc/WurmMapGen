package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import be.woubuc.wurmunlimited.wurmmapgen.database.Structure;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.file.Paths;
import java.util.ArrayList;

public final class StructureFileGen extends FileGen {
	
	public StructureFileGen() {
		setFilePath(Paths.get(WurmMapGen.dataPath, "structures.json"));
	}
	/**
	 * Generates a JSON file containing all structures on the server, and writes it to the given file path.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected String generateData() {
		Logger.title("Structure data");
		
		// Load data
		ArrayList<Structure> structures = WurmMapGen.db.getStructures();
		
		if (structures.size() == 0) {
			Logger.custom("SKIP", "No structures found");
			return null;
		}
		
		// Prepare JSON objects
		JSONObject dataObject = new JSONObject();
		JSONArray data = new JSONArray();
		
		JSONObject structureData;
		JSONArray structureBorders;
		for (Structure structure : structures) {
			structureData = new JSONObject();
			structureBorders = new JSONArray();
			
			// Add borders to JSON data
			structureBorders.add(structure.getMinX());
			structureBorders.add(structure.getMinY());
			structureBorders.add(structure.getMaxX());
			structureBorders.add(structure.getMaxY());
			structureData.put("borders", structureBorders);
			
			// Add creator to JSON data
			structureData.put("name", structure.getStructureName());
			structureData.put("creator", structure.getOwnerName());
			data.add(structureData);
		}
		
		dataObject.put("structures", data);
		
		return dataObject.toJSONString();
	}
}
