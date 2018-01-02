package be.woubuc.wurmunlimited.wurmmapgen.database;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class GuardTower {
	
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
	
	public String getOwnerName() { return ownerName; }
	
	public int getX() { return x; }
	public int getY() { return y; }
	
	public int getMinX() { return minX; }
	public int getMaxX() { return maxX; }
	public int getMinY() { return minY; }
	public int getMaxY() { return maxY; }
	
	public float getQl() { return ql; }
	public float getDmg() { return dmg; }
	
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
		
		loadOwnerData();
	}
	
	/**
	 * Loads the name of the tower's owner from the db
	 */
	private void loadOwnerData() {
		final String query = "select `NAME` from `PLAYERS` where `WURMID` = ?";
		try (PreparedStatement statement = WurmMapGen.db.getPlayers().prepareStatement(query, ownerID);
			 ResultSet result = statement.executeQuery()) {
			
			if (result.next()) {
				ownerName = result.getString("NAME");
			}
			
		} catch(SQLException e) {
			Logger.error("Could not load tower owner data: " + e.getMessage());
		}
	}
}
