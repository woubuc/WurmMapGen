package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StructureFileGen { // TODO extend FileGen base class
	
	/**
	 * Generates a JSON file containing all structures on the server, and writes it to the given file path.
	 * @param filePath The destination file
	 */
	@SuppressWarnings("unchecked")
	public void generateStructureFile(String filePath) throws IOException, SQLException {
		Logger.title("Structure data");
		
		// Prepare JSON container object
		JSONObject dataObject = new JSONObject();
		JSONArray data = new JSONArray();
		
		Logger.details("Loading structures from wurmzones.db");
		Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
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
			structureData.put("name", structure.getStructureName());
			structureData.put("creator", structure.getOwnerName());
			data.add(structureData);
		}
		
		resultSet.close();
		statement.close();
		
		if (count == 0) {
			Logger.custom("SKIP", "No structures found");
			return;
		}
		
		dataObject.put("structures", data);
		
		// Write JSON data to file
		Logger.details("Writing data/structures.json");
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(dataObject.toJSONString());
		writer.close();
		
		Logger.ok("Added " + count + " entries to structures.json");
	}
	
	/**
	 * Describes a single structure entry in the db
	 */
	private class Structure {
		
		private final long structureID;
		
		private String structureName;
		private String ownerName;
		private long ownerID;
		
		private int minX = -1;
		private int maxX = -1;
		private int minY = -1;
		private int maxY = -1;
		
		/**
		 * Initialises a structure
		 * @param structureID the db ID of the structure
		 */
		Structure(Long structureID) {
			this.structureID = structureID;
			
			populateStructure();
			populateOwnerName();
			generateMinMax();
		}
		
		String getStructureName() { return this.structureName; }
		String getOwnerName() { return this.ownerName; }
		
		int getMinX() { return this.minX; }
		int getMaxX() { return this.maxX; }
		int getMinY() { return this.minY; }
		int getMaxY() { return this.maxY; }
		
		/**
		 * Populates the structure instance with data from the db
		 */
		private void populateStructure() {
			try (Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
				 ResultSet result = statement.executeQuery("SELECT * FROM STRUCTURES WHERE WURMID='"+this.structureID+"';")) {
				
				if (result.next()) {
					this.ownerID = result.getLong("OWNERID");
					this.structureName = result.getString("NAME");
				}
				
			} catch(SQLException e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		/**
		 * Gets the name of the structure's owner from the db
		 */
		private void populateOwnerName() {
			try (Statement statement = WurmMapGen.db.getPlayers().getConnection().createStatement();
				 ResultSet resultSet = statement.executeQuery("SELECT NAME FROM PLAYERS WHERE WURMID='"+this.ownerID+"';")) {
				
				if (resultSet.next()) {
					this.ownerName = resultSet.getString("NAME");
				}
			} catch(SQLException e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		/**
		 * Calculates the minimum and maximum coordinates for the structure based on the coordinates of its tiles
		 */
		private void generateMinMax() {
			try (Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
				 ResultSet result = statement.executeQuery("SELECT TILEX, TILEY FROM BUILDTILES WHERE STRUCTUREID='" + this.structureID + "';")) {
				
				if (result.next()) {
					minX = result.getInt("TILEX");
					maxX = result.getInt("TILEX");
					minY = result.getInt("TILEY");
					maxY = result.getInt("TILEY");
					
					while (result.next()) {
						final int nX = result.getInt("TILEX");
						final int nY = result.getInt("TILEY");
						
						if (nX < minX) {
							minX = nX;
						} else if (nX > maxX) {
							maxX = nX;
						}
						
						if (nY < minY) {
							minY = nY;
						} else if (nY > maxY) {
							maxY = nY;
						}
					}
					
					maxX++;
					maxY++;
				}
				
			} catch (SQLException e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
