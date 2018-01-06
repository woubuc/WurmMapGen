package be.woubuc.wurmunlimited.wurmmapgen.database;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHandler {
	
	private DatabaseConnection zones;
	private DatabaseConnection items;
	private DatabaseConnection players;
	
	private DatabaseConnection modSupport = null;
	
	public DatabaseConnection getZones() { return zones; }
	public DatabaseConnection getItems() { return items; }
	public DatabaseConnection getPlayers() { return players; }
	
	public DatabaseConnection getModSupport() { return modSupport; }
	
	// The template IDs of all portal objects
	private static Integer[] portalIds = {
			603, 604, 605, 637, 606, 607, 732, 733, 855, // Vanilla Wurm portal items
			4002, 4003, 4004, 4010, 4011, 4015 // New portals mod
	};
	
	// The fixed template IDs of all tower objects
	private static Integer[] towerIds = {
			384, 430, 528, 638, 996 // Vanilla Wurm towers
	};
	
	// The template names of all modded towers using Ago's item template creator
	private static String[] towerTemplateNames = {
			"org.takino.tower.Jenn-Kellon",
			"org.takino.tower.Mol-Rehan",
			"org.takino.tower.Horde of the Summoned"
	};
	
	
	/**
	 * Opens all required database connections
	 */
	public boolean openDatabaseConnections() {
		Logger.title("Open db connections");
		
		// Create connections
		zones = new DatabaseConnection(WurmMapGen.fileManager.db_wurmZones);
		items = new DatabaseConnection(WurmMapGen.fileManager.db_wurmItems);
		players = new DatabaseConnection(WurmMapGen.fileManager.db_wurmPlayers);
		
		if (WurmMapGen.fileManager.db_modSupport != null) {
			modSupport = new DatabaseConnection(WurmMapGen.fileManager.db_modSupport);
		}
		
		// Connect to the db files
		try {
			zones.connect();
			items.connect();
			players.connect();
			if (modSupport != null) modSupport.connect();
		} catch (Exception e) {
			Logger.error("Could not connect: " + e.getMessage());
			return false;
		}
		
		Logger.ok("Database connections opened");
		return true;
	}
	
	/**
	 * Closes the open database connections
	 */
	public boolean closeDatabaseConnections() {
		Logger.title("Close db connections");
		
		try {
			zones.disconnect();
			items.disconnect();
			players.disconnect();
			if (modSupport != null) modSupport.disconnect();
		} catch (Exception e) {
			Logger.error("Could not close connection: " + e.getMessage());
			return false;
		}
		
		Logger.ok("Database connections closed");
		return true;
	}
	
	/**
	 * Loads modded tower template IDs from the modsupport database and adds them to a list
	 * @param  templateNames  The template names
	 * @return  The modded IDs list
	 */
	private ArrayList<Integer> loadModdedTemplateIds(String[] templateNames) {
		Logger.details("Loading modded template IDs from modsupport.db");
		
		ArrayList<Integer> ids = new ArrayList<>();
		if (modSupport == null) {
			Logger.warn("Mod support database not loaded");
			return ids;
		}
		
		StringBuilder query = new StringBuilder();
		query.append("select `ID`, `NAME` from `IDS` where ");
		for (int i = 1; i < templateNames.length; i++) query.append("`NAME` = ? or ");
		query.append("`NAME` = ?;");
		
		try (PreparedStatement statement = modSupport.prepareStatement(query.toString(), (Object[]) templateNames);
			 ResultSet resultSet = statement.executeQuery()) {
			
			while (resultSet.next()){
				final int id = resultSet.getInt("ID");
				ids.add(id);
				
				Logger.details("-> " + resultSet.getString("NAME") + " (" + id + ")");
			}
		} catch (SQLException e) {
			Logger.error("Could not load modded items: " + e.getMessage());
		}
		
		Logger.ok("Loaded " + ids.size() + " mod templates", true);
		return ids;
	}
	
	/**
	 * Gets a list of all guard towers
	 * @return  The guard towers
	 */
	public ArrayList<GuardTower> getGuardTowers() {
		
		// Get modded tower IDs
		List<Integer> towerIds = new ArrayList<>(Arrays.asList(DatabaseHandler.towerIds));
		towerIds.addAll(loadModdedTemplateIds(towerTemplateNames));
		
		Logger.details("Loading guard tower items from wurmitems.db");
		Logger.details("-> Tower IDs: " + towerIds.toString());
		
		// Build query based on the number of tower IDs
		StringBuilder query = new StringBuilder();
		query.append("select `LASTOWNERID`, `POSX`, `POSY`, `QUALITYLEVEL`, `DAMAGE` from `ITEMS` where (");
		for (int i = 1; i < towerIds.size(); i++) query.append("`TEMPLATEID` = ? or ");
		query.append("`TEMPLATEID` = ?) and `CREATIONSTATE` = 0;");
		
		ArrayList<GuardTower> guardTowers = new ArrayList<>();
		try (PreparedStatement statement = items.prepareStatement(query.toString(), (Object[]) towerIds.toArray());
			 ResultSet resultSet = statement.executeQuery()) {
			
			while (resultSet.next()) {
				guardTowers.add(new GuardTower(
						resultSet.getLong("LASTOWNERID"),
						resultSet.getInt("POSX"),
						resultSet.getInt("POSY"),
						resultSet.getFloat("QUALITYLEVEL"),
						resultSet.getFloat("DAMAGE")
				));
			}
			
			Logger.ok("Loaded data for " + guardTowers.size() + " guard towers", true);
		} catch (SQLException e) {
			Logger.error("Could not get guard towers list: " + e.getMessage());
		}
		return guardTowers;
	}
	
	/**
	 * Gets a list of all portals
	 * @return  The portals
	 */
	public ArrayList<Portal> getPortals() {
		Logger.details("Loading portal items from wurmitems.db");
		Logger.details("-> Portal IDs: " + Arrays.toString(portalIds));
		
		// Build query based on the number of portal IDs
		StringBuilder query = new StringBuilder();
		query.append("select `NAME`, `POSX`, `POSY` from `ITEMS` where (");
		for (int i = 1; i < portalIds.length; i++) query.append("`TEMPLATEID` = ? or ");
		query.append("`TEMPLATEID` = ?) and `CREATIONSTATE` = 0;");
		
		ArrayList<Portal> portals = new ArrayList<>();
		try (PreparedStatement statement = items.prepareStatement(query.toString(), (Object[]) portalIds);
			 ResultSet resultSet = statement.executeQuery()) {
			
			while (resultSet.next()) {
				portals.add(new Portal(
						resultSet.getString("NAME"),
						resultSet.getInt("POSX"),
						resultSet.getInt("POSY")
				));
			}
			
			Logger.ok("Loaded data for " + portals.size() + " portals", true);
		} catch (SQLException e) {
			Logger.error("Could not get portals list: " + e.getMessage());
		}
		return portals;
	}
	
	/**
	 * Gets a list of all structures
	 * @return  The structures
	 */
	public ArrayList<Structure> getStructures() {
		Logger.details("Loading structures from wurmzones.db");
		final String query = "select `WURMID` from `STRUCTURES` where `FINISHED` = 1;";
		
		ArrayList<Structure> structures = new ArrayList<>();
		try (PreparedStatement statement = zones.prepareStatement(query);
			 ResultSet resultSet = statement.executeQuery()) {
			
			while (resultSet.next()) structures.add(new Structure(resultSet.getLong("WURMID")));
			Logger.ok("Loaded data for " + structures.size() + " structures", true);
		} catch (SQLException e) {
			Logger.error("Could not get structures list: " + e.getMessage());
		}
		return structures;
	}
	
	/**
	 * Gets a list of all villages
	 * @return  The villages
	 */
	public ArrayList<Village> getVillages() {
		Logger.details("Loading villages from wurmzones.db");
		final String query = "select `ID` from `VILLAGES` where `DISBANDED` = 0;";
		
		ArrayList<Village> villages = new ArrayList<>();
		try (PreparedStatement statement = zones.prepareStatement(query);
			 ResultSet resultSet = statement.executeQuery()) {
			
			while (resultSet.next()) villages.add(new Village(resultSet.getInt("ID")));
			Logger.ok("Loaded data for " + villages.size() + " villages", true);
		} catch (SQLException e) {
			Logger.error("Could not get village list: " + e.getMessage());
		}
		return villages;
	}
}
