package com.imraginbro.wurm.mapgen.filegen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class GuardTower {

	private final int X;
	private final int Y;
	private String ownerName;
	private final long ownerID;
	private final float QL;
	private final float DMG;
	private final int minX;
	private final int maxX;
	private final int minY;
	private final int maxY;
	
	private final int areaLimit = 50;
	
	public GuardTower(long ownerID, int X, int Y, float QL, float DMG) {
		this.X = X;
		this.Y = Y;
		this.ownerID = ownerID;
		this.QL = QL;
		this.DMG = DMG;
		this.minX = X - this.areaLimit;
		this.maxX = X + this.areaLimit;
		this.minY = Y - this.areaLimit;
		this.maxY = Y + this.areaLimit;
		populateOwnerName();
	}
	
	public int getX() {
		return this.X;
	}
	
	public int getY() {
		return this.Y;
	}
	
	public int getMinX() {
		return this.minX;
	}
	
	public int getMaxX() {
		return this.maxX;
	}
	
	public int getMinY() {
		return this.minY;
	}
	
	public int getMaxY() {
		return this.maxY;
	}
	
	public String getOwnerName() {
		return this.ownerName;
	}

	public long getOwnerID() {
		return this.ownerID;
	}

	public float getQL() {
		return this.QL;
	}

	public float getDMG() {
		return this.DMG;
	}
	
	private void populateOwnerName() {
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = MapBuilder.dbhandler.getPlayersConnection().createStatement();
			result = statement.executeQuery("SELECT NAME FROM PLAYERS WHERE WURMID='"+this.ownerID+"';");
			if (result.next()) {
				this.ownerName = result.getString("NAME");
			}
		} catch(SQLException e) {
			System.out.println("[ERROR] " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) { }
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) { }
			}
		}
	}
	
}