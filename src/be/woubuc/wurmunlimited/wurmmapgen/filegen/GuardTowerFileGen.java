package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuardTowerFileGen { // TODO extend FileGen base class
	
	/**
	 * Generates a JSON file containing all guard towers on the server, and writes it to the given file path.
	 * @param filePath The destination file
	 */
	@SuppressWarnings("unchecked")
	public void generateGuardTowerFile(String filePath) throws IOException, SQLException {
		Logger.title("Guard tower data");
		
		List<Integer> towerIds = new ArrayList(Arrays.asList(384, 430, 528, 638, 996));
		if (WurmMapGen.db.getModSupport() != null) {
			Logger.details("Loading modded guard tower IDs");
			towerIds.addAll(loadModdedTowerIds());
		}
		
		Logger.details("Loading guard towers from wurmitems.db");
		
		StringBuilder query = new StringBuilder();
		query.append("select LASTOWNERID, POSX, POSY, QUALITYLEVEL, DAMAGE from ITEMS where (");
		for (int i = 0; i < towerIds.size() - 1; i++) {
			query.append("TEMPLATEID = ? or ");
		}
		query.append("TEMPLATEID = ?) and CREATIONSTATE = 0");
		
		PreparedStatement statement = WurmMapGen.db.getItems().getConnection().prepareStatement(query.toString());
		for (int i = 0; i < towerIds.size(); i++) {
			statement.setInt(i + 1, towerIds.get(i));
		}
		
		ResultSet resultSet = statement.executeQuery();
		
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
			Logger.custom("SKIP", "No guard towers found");
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
		Logger.details("Creating data/guardtowers.json");
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(dataObject.toJSONString());
		writer.close();
		
		Logger.ok("Added " + guardTowers.size() + " entries to guardtowers.json");
	}
	
	/**
	 * Loads modded tower template IDs from the modsupport database and adds them to a list
	 * @return The modded IDs list
	 */
	private static List<Integer> loadModdedTowerIds() throws SQLException {
		List<Integer> list = new ArrayList<>();
		
		Statement statement = WurmMapGen.db.getModSupport().getConnection().createStatement();
		ResultSet resultSet = statement.executeQuery("select ID from IDS where " +
				"NAME = 'org.takino.tower.Jenn-Kellon' or " +
				"NAME = 'org.takino.tower.Mol-Rehan' or " +
				"NAME = 'org.takino.tower.Horde of the Summoned' " +
				";");
		
		while (resultSet.next()) {
			list.add(resultSet.getInt("ID"));
		}
		
		return list;
	}
	
	/**
	 * Describes a guard tower in the database
	 */
	private static class GuardTower {
		
		private final int x;
		private final int y;
		
		private String ownerName;
		private final long ownerID;
		
		private final float ql;
		private final float dmg;
		
		private final int minX;
		private final int maxX;
		private final int minY;
		private final int maxY;
		
		private static final int areaLimit = 50;
		
		/**
		 * Initialises a guard tower
		 * @param  ownerID  ID of the guard tower's owner
		 * @param  x        The x coordinate of the tower
		 * @param  y        The y coordinate of the tower
		 * @param  ql       The item quality level
		 * @param  dmg      The total damage of the tower
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
		
		String getOwnerName() { return ownerName; }
		
		int getX() { return x; }
		int getY() { return y; }
		
		int getMinX() { return minX; }
		int getMaxX() { return maxX; }
		int getMinY() { return minY; }
		int getMaxY() { return maxY; }
		
		float getQl() { return ql; }
		float getDmg() { return dmg; }
		
		/**
		 * Loads the name of the tower's owner from the db
		 */
		private void populateOwnerName() {
			try (PreparedStatement statement = createPopulateOwnerNameStatement();
				 ResultSet result = statement.executeQuery()) {
				
				if (result.next()) {
					ownerName = result.getString("NAME");
				}
				
			} catch(SQLException e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		/**
		 * Creates the prepared statement for the populateOwnerName method
		 * @see GuardTower#populateOwnerName()
		 * @return The statement
		 */
		private PreparedStatement createPopulateOwnerNameStatement() throws SQLException {
			PreparedStatement statement = WurmMapGen.db.getPlayers().getConnection()
					.prepareStatement("select NAME from PLAYERS where WURMID = ?");
			
			statement.setLong(1, ownerID);
			return statement;
		}
	}
}
