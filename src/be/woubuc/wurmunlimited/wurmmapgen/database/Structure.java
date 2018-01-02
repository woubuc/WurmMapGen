package be.woubuc.wurmunlimited.wurmmapgen.database;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Structure {
	
	private final long structureID;
	
	private String structureName;
	private String ownerName;
	private long ownerID;
	
	private int minX = -1;
	private int maxX = -1;
	private int minY = -1;
	private int maxY = -1;
	
	public String getStructureName() { return structureName; }
	public String getOwnerName() { return ownerName; }
	
	public int getMinX() { return minX; }
	public int getMaxX() { return maxX; }
	public int getMinY() { return minY; }
	public int getMaxY() { return maxY; }
	
	/**
	 * Initialises a structure
	 * @param structureID the db ID of the structure
	 */
	Structure(Long structureID) {
		this.structureID = structureID;
		
		loadStructureData();
		loadOwnerData();
		loadTileData();
	}
	
	/**
	 * Populates the structure instance with data from the db
	 */
	private void loadStructureData() {
		final String query = "select `OWNERID`, `NAME` from `STRUCTURES` where `WURMID` = ?";
		try (PreparedStatement statement = WurmMapGen.db.getZones().prepareStatement(query, structureID);
			 ResultSet resultSet = statement.executeQuery()) {
			
			if (resultSet.next()) {
				ownerID = resultSet.getLong("OWNERID");
				structureName = resultSet.getString("NAME");
			}
			
		} catch(SQLException e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the name of the structure's owner from the db
	 */
	private void loadOwnerData() {
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
	private void loadTileData() {
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
