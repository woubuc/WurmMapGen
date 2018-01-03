package be.woubuc.wurmunlimited.wurmmapgen.database;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class GuardTower {
	
	private final int posX;
	private final int posY;
	
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
	
	public int getPosX() { return posX; }
	public int getPosY() { return posY; }
	
	public int getMinX() { return minX; }
	public int getMaxX() { return maxX; }
	public int getMinY() { return minY; }
	public int getMaxY() { return maxY; }
	
	public float getQl() { return ql; }
	public float getDmg() { return dmg; }
	
	/**
	 * Initialises a guard tower
	 * @param  ownerID  ID of the guard tower's owner
	 * @param  posX     The X coordinate of the tower
	 * @param  posY     The Y coordinate of the tower
	 * @param  ql       The item quality level
	 * @param  dmg      The total damage of the tower
	 */
	GuardTower(long ownerID, int posX, int posY, float ql, float dmg) {
		this.ownerID = ownerID;
		
		this.posX = (int) Math.floor(posX / 4);
		this.posY = (int) Math.floor(posY / 4);
		
		this.ql = ql;
		this.dmg = dmg;
		
		this.minX = this.posX - areaLimit;
		this.maxX = this.posX + areaLimit;
		this.minY = this.posY - areaLimit;
		this.maxY = this.posY + areaLimit;
		
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
