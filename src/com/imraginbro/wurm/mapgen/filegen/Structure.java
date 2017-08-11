package com.imraginbro.wurm.mapgen.filegen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class Structure {
	
	private String structureName;
	private long structureID;
	private String ownerName;
	private long ownerID;
	private int minX = -1;
	private int maxX = -1;
	private int minY = -1;
	private int maxY = -1;

	public Structure(Long structureID) {
		this.structureID = structureID;
		populateStructure();
		populateOwnerName();
		generateMinMax();
	}
	
	public String getStructureName() {
		return this.structureName;
	}
	
	public long getStructureID() {
		return this.structureID;
	}
	
	public String getOwnerName() {
		return this.ownerName;
	}
	
	public long getOwnerID() {
		return this.ownerID;
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
	
	private void populateStructure() {
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = MapBuilder.dbhandler.getZonesConnection().createStatement();
			result = statement.executeQuery("SELECT * FROM STRUCTURES WHERE WURMID='"+this.structureID+"';");
			if (result.next()) {
				this.ownerID = result.getLong("OWNERID");
				this.structureName = result.getString("NAME");
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
	
	private void generateMinMax() {
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = MapBuilder.dbhandler.getZonesConnection().createStatement();
			result = statement.executeQuery("SELECT TILEX, TILEY FROM BUILDTILES WHERE STRUCTUREID='"+this.structureID+"';"); 
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
