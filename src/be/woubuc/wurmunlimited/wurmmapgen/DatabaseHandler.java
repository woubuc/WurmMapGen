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
	
	public WurmDatabaseConnection getZones() { return zones; }
	public WurmDatabaseConnection getItems() { return items; }
	public WurmDatabaseConnection getPlayers() { return players; }
	
	/**
	 * Opens all required database connections
	 */
	public boolean openDatabaseConnections() {
		System.out.println();
		System.out.println("Open db connections");
		
		// Create connections
		zones = new WurmDatabaseConnection(WurmMapGen.fileManager.db_wurmZones);
		items = new WurmDatabaseConnection(WurmMapGen.fileManager.db_wurmItems);
		players = new WurmDatabaseConnection(WurmMapGen.fileManager.db_wurmPlayers);
		
		// Connect to the db files
		try {
			zones.connect();
			items.connect();
			players.connect();
		} catch (Exception e) {
			System.err.println("ERROR Could not connect: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Closes the open database connections
	 */
	public boolean closeDatabaseConnections() {
		System.out.println();
		System.out.println("Close db connections");
		
		try {
			zones.disconnect();
			items.disconnect();
			players.disconnect();
		} catch (Exception e) {
			System.err.println("ERROR Could not close connection: " + e.getMessage());
			return false;
		}
		
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
			if (WurmMapGen.properties.verbose) System.out.println("      Connecting to " + file.getName());
			
			if (isConnected()) {
				System.out.println(" WARN Already connected to " + file.getName());
				return;
			}
			
			if (!file.exists()) {
				throw new IOException("File not found: " + file.getName());
			}
			
			connection = DriverManager.getConnection("jdbc:sqlite:" + file);
			if (WurmMapGen.properties.verbose) System.out.println("   OK Connection established");
		}
		
		/**
		 * Disconnects from the database
		 */
		private void disconnect() throws SQLException {
			if (WurmMapGen.properties.verbose) System.out.println("      Disconnecting from " + file.getName());
			
			if (!isConnected()) {
				System.out.println(" WARN Connection was not open");
				return;
			}
			
			connection.close();
			connection = null;
		}
	}
}
