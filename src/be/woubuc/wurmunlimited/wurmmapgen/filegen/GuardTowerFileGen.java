package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GuardTowerFileGen {
	
	/**
	 * Generates a JSON file containing all guard towers on the server, and writes it to the given file path.
	 * @param filePath The destination file
	 */
	@SuppressWarnings("unchecked")
	public void generateGuardTowerFile(String filePath) throws IOException, SQLException {
		System.out.println();
		System.out.println("Guard tower data");
		
		if (WurmMapGen.properties.verbose) System.out.println("      loading guard towers from wurmitems.db");
		Statement statement = WurmMapGen.db.getItems().getConnection().createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM ITEMS WHERE (TEMPLATEID='384' OR TEMPLATEID='430' OR TEMPLATEID='528' OR TEMPLATEID='638' OR TEMPLATEID='996') AND CREATIONSTATE='0';");
		
		ArrayList<GuardTower> guardTowers = new ArrayList<>();
		
		while (resultSet.next()) {
			guardTowers.add(new GuardTower(resultSet.getLong("LASTOWNERID"),
					(int) Math.floor(resultSet.getInt("POSX") / 4),
					(int) Math.floor(resultSet.getInt("POSY") / 4),
					resultSet.getFloat("QUALITYLEVEL"), resultSet.getFloat("DAMAGE")));
		}
		
		resultSet.close();
		statement.close();
		
		if (guardTowers.size() == 0) {
			System.out.println(" SKIP no guard towers found");
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
			towerData.put("ql", f.format(guardTower.getQl()));
			towerData.put("dmg", f.format(guardTower.getDmg()));
			
			data.add(towerData);
		}
		
		dataObject.put("guardtowers", data);
		
		// Write JSON data to file
		if (WurmMapGen.properties.verbose) System.out.println("      creating data/guardtowers.json");
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(dataObject.toJSONString());
		writer.close();
		
		System.out.println("   OK added " + guardTowers.size() + " entries to guardtowers.json");
	}
	
	/**
	 * Describes a guard tower in the db
	 */
	private class GuardTower {
		
		private final int x;
		private final int y;
		
		private final long ownerID;
		private String ownerName;
		
		private final float ql;
		private final float dmg;
		
		private final int minX;
		private final int maxX;
		private final int minY;
		private final int maxY;
		
		private static final int areaLimit = 50;
		
		/**
		 * Initialises a guard tower
		 * @param ownerID ID of the guard tower's owner
		 * @param x       The x coordinate of the tower
		 * @param y       The y coordinate of the tower
		 * @param ql      The item quality level
		 * @param dmg     The total damage of the tower
		 */
		GuardTower(long ownerID, int x, int y, float ql, float dmg) {
			this.ownerID = ownerID;
			
			this.x = x;
			this.y = y;
			
			this.ql = ql;
			this.dmg = dmg;
			
			this.minX = x - areaLimit;
			this.maxX = x + areaLimit;
			this.minY = y - areaLimit;
			this.maxY = y + areaLimit;
			
			populateOwnerName();
		}
		
		String getOwnerName() { return this.ownerName; }
		
		int getX() { return this.x; }
		int getY() { return this.y; }
		
		int getMinX() { return this.minX; }
		int getMaxX() { return this.maxX; }
		int getMinY() { return this.minY; }
		int getMaxY() { return this.maxY; }
		
		float getQl() { return this.ql; }
		float getDmg() { return this.dmg; }
		
		/**
		 * Loads the name of the tower's owner from the db
		 */
		private void populateOwnerName() {
			try (Statement statement = WurmMapGen.db.getPlayers().getConnection().createStatement();
				 ResultSet result = statement.executeQuery("SELECT NAME FROM PLAYERS WHERE WURMID='"+this.ownerID+"';")) {
				
				if (result.next()) {
					this.ownerName = result.getString("NAME");
				}
				
			} catch(SQLException e) {
				System.out.println("[ERROR] " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
