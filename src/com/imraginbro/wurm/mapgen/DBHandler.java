package com.imraginbro.wurm.mapgen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class DBHandler {

	private Connection wurmZonesConnection = null;
	private Connection wurmItemsConnection = null;
	private Connection wurmPlayersConnection = null;
	
	public void load() {
		this.openConnections();
	}
	
	public Connection getZonesConnection() {
		return this.wurmZonesConnection;
	}
	
	public Connection getItemsConnection() {
		return this.wurmItemsConnection;
	}
	
	public Connection getPlayersConnection() {
		return this.wurmPlayersConnection;
	}
	
	public boolean checkZonesConnection() {
		if (this.wurmZonesConnection != null) {
			return true;
		}
		return false;
	}
	
	public boolean checkItemsConnection() {
		if (this.wurmItemsConnection != null) {
			return true;
		}
		return false;
	}
	
	public boolean checkPlayersConnection() {
		if (this.wurmPlayersConnection != null) {
			return true;
		}
		return false;
	}

	public void openConnections() {
		try {
			if (MapBuilder.fileManager.db_wurmZones.exists() && !this.checkZonesConnection()) {
				this.wurmZonesConnection = DriverManager.getConnection("jdbc:sqlite:" + MapBuilder.fileManager.db_wurmZones);
			}
		} catch (SQLException e) {
			System.out.println("[ERROR] connecting wurmzones.db - " + e.getMessage());
		}
		try {
			if (MapBuilder.fileManager.db_wurmItems.exists() && !this.checkItemsConnection()) {
				this.wurmItemsConnection = DriverManager.getConnection("jdbc:sqlite:" + MapBuilder.fileManager.db_wurmItems);
			}
		} catch (SQLException e) {
			System.out.println("[ERROR] connecting wurmitems.db - " + e.getMessage());
		}
		try {
			if (MapBuilder.fileManager.db_wurmPlayers.exists() && !this.checkPlayersConnection()) {
				this.wurmPlayersConnection = DriverManager.getConnection("jdbc:sqlite:" + MapBuilder.fileManager.db_wurmPlayers);
			}
		} catch (SQLException e) {
			System.out.println("[ERROR] connecting wurmplayers.db - " + e.getMessage());
		}
	}
	
	public void closeConnections() {
		if (this.wurmZonesConnection != null) {
			try {
				this.wurmZonesConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.wurmZonesConnection = null;
		}
		if (this.wurmItemsConnection != null) {
			try {
				this.wurmItemsConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.wurmItemsConnection = null;
		}
		if (this.wurmPlayersConnection != null) {
			try {
				this.wurmPlayersConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.wurmPlayersConnection = null;
		}
	}

}
