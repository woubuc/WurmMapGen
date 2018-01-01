package be.woubuc.wurmunlimited.wurmmapgen;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
	
	private WurmDatabaseConnection zones;
	private WurmDatabaseConnection items;
	private WurmDatabaseConnection players;
	
	private WurmDatabaseConnection modSupport = null;
	
	public WurmDatabaseConnection getZones() { return zones; }
	public WurmDatabaseConnection getItems() { return items; }
	public WurmDatabaseConnection getPlayers() { return players; }
	public WurmDatabaseConnection getModSupport() { return modSupport; }
	
	/**
	 * Opens all required database connections
	 */
	public boolean openDatabaseConnections() {
		Logger.title("Open db connections");
		
		// Create connections
		zones = new WurmDatabaseConnection(WurmMapGen.fileManager.db_wurmZones);
		items = new WurmDatabaseConnection(WurmMapGen.fileManager.db_wurmItems);
		players = new WurmDatabaseConnection(WurmMapGen.fileManager.db_wurmPlayers);
		
		if (WurmMapGen.fileManager.db_modSupport != null) {
			modSupport = new WurmDatabaseConnection(WurmMapGen.fileManager.db_modSupport);
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
	 * Describes a Wurm Unlimited SQLite database connection
	 */
	public class WurmDatabaseConnection {
		
		private File file;
		private Connection connection;
		
		/**
		 * Prepares a connection to a Wurm database file
		 * @param  file  The filename
		 */
		WurmDatabaseConnection(File file) {
			this.file = file;
		}
		
		/**
		 * Gets connection status
		 * @return  true if the connection is connected
		 */
		boolean isConnected() {
			if (connection == null) return false;
			
			try {
				return !connection.isClosed();
			} catch (SQLException e) {
				return false;
			}
		}
		
		/**
		 * Gets the underlying connection
		 * @return  The Connection instance
		 */
		public Connection getConnection() {
			return connection;
		}
		
		/**
		 * Connects to the database
		 */
		private void connect() throws IOException, SQLException {
			Logger.details("Connecting to " + file.getName());
			
			if (isConnected()) {
				Logger.warn("Already connected to " + file.getName());
				return;
			}
			
			if (!file.exists()) {
				throw new IOException("File not found: " + file.getName());
			}
			
			connection = DriverManager.getConnection("jdbc:sqlite:" + file);
			Logger.ok("Connection established", true);
		}
		
		/**
		 * Disconnects from the database
		 */
		private void disconnect() throws SQLException {
			Logger.details("Disconnecting from " + file.getName());
			
			if (!isConnected()) {
				Logger.warn("Connection was not open");
				return;
			}
			
			connection.close();
			connection = null;
			Logger.ok("Connection closed", true);
		}
	}
}
