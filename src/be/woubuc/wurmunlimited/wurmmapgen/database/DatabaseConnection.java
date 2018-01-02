package be.woubuc.wurmunlimited.wurmmapgen.database;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
	
	private File file;
	private Connection connection;
	
	public Connection getConnection() { return connection; }
	
	/**
	 * Prepares a connection to a Wurm database file
	 * @param  file  The filename
	 */
	DatabaseConnection(File file) {
		this.file = file;
	}
	
	/**
	 * Gets connection status
	 * @return  true if the connection is connected
	 */
	private boolean isConnected() {
		if (connection == null) return false;
		
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * Connects to the database
	 */
	void connect() throws IOException, SQLException {
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
	void disconnect() throws SQLException {
		Logger.details("Disconnecting from " + file.getName());
		
		if (!isConnected()) {
			Logger.warn("Connection was not open");
			return;
		}
		
		connection.close();
		connection = null;
		Logger.ok("Connection closed", true);
	}
	
	/**
	 * Creates a prepared statement with given properties
	 * @param  sql   The SQL query
	 * @param  args  The query values
	 * @return  The prepared statement
	 */
	protected PreparedStatement prepareStatement(String sql, Object... args) throws SQLException {
		if (!isConnected()) return null;
		PreparedStatement statement = connection.prepareStatement(sql);
		
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof String) statement.setString(i + 1, (String) args[i]);
				else if (args[i] instanceof Integer) statement.setInt(i + 1, (int) args[i]);
				else if (args[i] instanceof Long) statement.setLong(i + 1, (long) args[i]);
				else if (args[i] instanceof Boolean) statement.setBoolean(i + 1, (boolean) args[i]);
				else if (WurmMapGen.debug)
					throw new RuntimeException("Unknown type " + (args[i].getClass().getName()) + " in prepared statement");
			}
		}
		
		return statement;
	}
}
