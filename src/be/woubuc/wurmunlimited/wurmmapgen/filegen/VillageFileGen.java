package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class VillageFileGen {
	
	/**
	 * Generates a JSON file containing all villages on the server, and writes it to the given file path.
	 */
	@SuppressWarnings("unchecked")
	public void generateVillageFile() throws IOException, SQLException {
		System.out.println();
		System.out.println("Deeds data");
		
		// Load list of villages
		if (WurmMapGen.properties.verbose) System.out.println("      Loading villages from wurmzones.db");
		Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT ID FROM VILLAGES WHERE DISBANDED=0;");
		
		ArrayList<Village> villages = new ArrayList<>();
		
		while (resultSet.next()) {
			villages.add(new Village(resultSet.getInt("ID")));
		}
		
		resultSet.close();
		statement.close();
		
		// Stop right here if there are no villages on the server
		if (villages.size() == 0) {
			System.out.println(" SKIP No villages found");
			return;
		}
		
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
		
		dataObject.put("villages", data);
		
		// Write JSON data to file
		if (WurmMapGen.properties.verbose) System.out.println("      Creating data/villages.json");
		String filePath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data", "villages.json").toString();
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(dataObject.toJSONString());
		writer.close();
		
		System.out.println("   OK Added " + villages.size() + " entries to villages.json");
	}
	
	/**
	 * Describes a single village entry in the db
	 */
	private class Village {
		
		private final int villageID;
		
		private String villageName;
		private String mayorName;
		private String motto;
		
		private int startX;
		private int startY;
		private int endX;
		private int endY;
		
		private boolean permanent;
		private int citizenCount = 0;
		
		private long deedID;
		private long tokenID;
		private int tokenX = 0;
		private int tokenY = 0;
		
		/**
		 * Initialises a Village
		 * @param villageID The db ID of the village
		 */
		Village(int villageID) {
			this.villageID = villageID;
			
			populateVillage();
			populateTokenLocation();
			populateCitizenCount();
		}
		
		String getVillageName() { return villageName; }
		String getMayorName() { return mayorName; }
		String getMotto() { return motto; }
		
		int getStartX() { return startX; }
		int getStartY() { return startY; }
		int getEndX() { return endX; }
		int getEndY() { return endY; }
		
		boolean isPermanent() { return permanent; }
		int getCitizenCount() { return citizenCount; }
		
		int getTokenX() { return tokenX; }
		int getTokenY() { return tokenY; }
		
		/**
		 * Loads the village data from the db
		 */
		private void populateVillage() {
			try (Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
				 ResultSet result = statement.executeQuery("SELECT * FROM VILLAGES WHERE ID='" + villageID + "';")) {
				
				if (result.next()) {
					villageName = result.getString("NAME");
					mayorName = result.getString("MAYOR");
					motto = result.getString("DEVISE");
					
					startX = result.getInt("STARTX");
					startY = result.getInt("STARTY");
					endX = result.getInt("ENDX");
					endY = result.getInt("ENDY");
					deedID = result.getLong("DEEDID");
					tokenID = result.getLong("TOKEN");
					permanent = result.getBoolean("PERMANENT");
				}
				
			} catch (SQLException e) {
				System.out.println("ERROR " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		/**
		 * Loads the location of the village token from the db
		 */
		private void populateTokenLocation() {
			try (Statement statement = WurmMapGen.db.getItems().getConnection().createStatement();
				 ResultSet result = statement.executeQuery("SELECT POSX, POSY FROM ITEMS WHERE WURMID='" + tokenID + "';")) {
				
				if (result.next()) {
					tokenX = (int) Math.floor(result.getInt("POSX") / 4);
					tokenY = (int) Math.floor(result.getInt("POSY") / 4);
				}
				
			} catch (SQLException e) {
				System.out.println("[ERROR] " + e.getMessage());
				e.printStackTrace();
			}
			
			if (tokenX < startX || tokenY < startY || tokenX > endX || tokenY > endY) {
				tokenX = (startX + endX) / 2;
				tokenY = (startY + endY) / 2;
			}
		}
		
		/**
		 * Loads the number of citizens from the db
		 */
		private void populateCitizenCount() {
			try (Statement statement = WurmMapGen.db.getZones().getConnection().createStatement();
				 ResultSet result = statement.executeQuery("SELECT WURMID FROM CITIZENS WHERE VILLAGEID='" + villageID + "';")) {
				
				while (result.next()) {
					long tempID = result.getLong("WURMID");
					
					try (Statement statementPlayers = WurmMapGen.db.getPlayers().getConnection().createStatement();
						 ResultSet resultPlayers = statementPlayers.executeQuery("Select WURMID FROM PLAYERS WHERE WURMID='" + tempID + "';")) {
						
						if (resultPlayers.next()) {
							this.citizenCount++;
						}
					} catch (SQLException e) {
						System.out.println("[ERROR] " + e.getMessage());
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				System.out.println("[ERROR] " + e.getMessage());
				e.printStackTrace();
			}
			
			if (tokenX < startX || tokenY < startY || tokenX > endX || tokenY > endY) {
				tokenX = (startX + endX) / 2;
				tokenY = (startY + endY) / 2;
			}
		}
	}
}
