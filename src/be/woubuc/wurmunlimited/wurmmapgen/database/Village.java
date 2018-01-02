package be.woubuc.wurmunlimited.wurmmapgen.database;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Village {
	
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
	
	private long tokenID;
	private int tokenX = 0;
	private int tokenY = 0;
	
	public String getVillageName() { return villageName; }
	public String getMayorName() { return mayorName; }
	public String getMotto() { return motto; }
	
	public int getStartX() { return startX; }
	public int getStartY() { return startY; }
	public int getEndX() { return endX; }
	public int getEndY() { return endY; }
	
	public boolean isPermanent() { return permanent; }
	public int getCitizenCount() { return citizenCount; }
	
	public int getTokenX() { return tokenX; }
	public int getTokenY() { return tokenY; }
	
	/**
	 * Initialises a Village
	 * @param villageID The db ID of the village
	 */
	Village(int villageID) {
		this.villageID = villageID;
		
		loadVillageData();
		loadTokenData();
		loadCitizenData();
	}
	
	/**
	 * Loads the village data from the db
	 */
	private void loadVillageData() {
		final String query = "select " +
				"`NAME`, `MAYOR`, `DEVISE`, `STARTX`, `STARTY`, `ENDX`, `ENDY`, `TOKEN`, `PERMANENT` " +
				"from `VILLAGES` where `ID` = ?";
		
		try (PreparedStatement statement = WurmMapGen.db.getZones().prepareStatement(query, villageID);
			 ResultSet resultSet = statement.executeQuery()) {
			
			if (resultSet.next()) {
				villageName = resultSet.getString("NAME");
				mayorName = resultSet.getString("MAYOR");
				motto = resultSet.getString("DEVISE");
				
				startX = resultSet.getInt("STARTX");
				startY = resultSet.getInt("STARTY");
				endX = resultSet.getInt("ENDX");
				endY = resultSet.getInt("ENDY");
				tokenID = resultSet.getLong("TOKEN");
				permanent = resultSet.getBoolean("PERMANENT");
			}
			
		} catch (SQLException e) {
			Logger.error("Could not load village data: " + e.getMessage());
		}
	}
	
	/**
	 * Loads the location of the village token from the db
	 */
	private void loadTokenData() {
		final String query = "select `POSX`, `POSY` from `ITEMS` where `WURMID` = ?";
		
		try (PreparedStatement statement = WurmMapGen.db.getItems().prepareStatement(query, tokenID);
			 ResultSet resultSet = statement.executeQuery()) {
			
			if (resultSet.next()) {
				tokenX = (int) Math.floor(resultSet.getInt("POSX") / 4);
				tokenY = (int) Math.floor(resultSet.getInt("POSY") / 4);
			}
			
		} catch (SQLException e) {
			Logger.error("Could not load village token data: " + e.getMessage());
			return;
		}
		
		// If token location is invalid (outside of deed borders), put it in the center of the deed
		if (tokenX < startX || tokenY < startY || tokenX > endX || tokenY > endY) {
			tokenX = (startX + endX) / 2;
			tokenY = (startY + endY) / 2;
		}
	}
	
	/**
	 * Loads the number of citizens from the db
	 */
	private void loadCitizenData() {
		final String citizenQuery = "select `WURMID` from `CITIZENS` where `VILLAGEID` = ?";
		final String playerQuery = "select `WURMID` from `PLAYERS` where `WURMID` = ?";
		
		// First, get the wurmids from all citizens
		try (PreparedStatement citizenStatement = WurmMapGen.db.getZones().prepareStatement(citizenQuery, villageID);
			 ResultSet citizenResultSet = citizenStatement.executeQuery()) {
			
			while (citizenResultSet.next()) {
				long citizenId = citizenResultSet.getLong("WURMID");
				
				// Then, check if a player with that wurmid exists (to avoid counting guards etc)
				try (PreparedStatement playerStatement = WurmMapGen.db.getPlayers().prepareStatement(playerQuery, citizenId);
					 ResultSet playerResultSet = playerStatement.executeQuery()) {
					
					if (playerResultSet.next()) {
						citizenCount++;
					}
					
				} catch (SQLException e) {
					Logger.error("Could not load citizen player data: " + e.getMessage());
				}
			}
		} catch (SQLException e) {
			Logger.error("Could not load citizen data: " + e.getMessage());
		}
	}
}
