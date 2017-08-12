package com.imraginbro.wurm.mapgen.filegen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class Village {

	private final int villageID;
	private String villageName;
	private String mayorName;
	private String motto;
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	private boolean permanent;
	private long deedID;
	private long tokenID;
	private int tokenX = 0;
	private int tokenY = 0;
	private int citizenCount = 0;

	public Village(int villageID) {
		this.villageID = villageID;
		populateVillage();
		populateTokenLocation();
		populateCitizenCount();
	}

	private void populateVillage() {
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = MapBuilder.dbhandler.getZonesConnection().createStatement();
			result = statement.executeQuery("SELECT * FROM VILLAGES WHERE ID='"+villageID+"';");
			if (result.next()) {
				setVillageName(result.getString("NAME"));
				setMayorName(result.getString("MAYOR"));
				setMotto(result.getString("DEVISE"));
				setStartX(result.getInt("STARTX"));
				setStartY(result.getInt("STARTY"));
				setEndX(result.getInt("ENDX"));
				setEndY(result.getInt("ENDY"));
				setDeedID(result.getLong("DEEDID"));
				setTokenID(result.getLong("TOKEN"));
				setPermanent(result.getBoolean("PERMANENT"));
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

	private void populateTokenLocation() {
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = MapBuilder.dbhandler.getItemsConnection().createStatement();
			result = statement.executeQuery("SELECT POSX, POSY FROM ITEMS WHERE WURMID='"+tokenID+"';");
			if (result.next()) {
				setTokenX((int) Math.floor(result.getInt("POSX")/4));
				setTokenY((int) Math.floor(result.getInt("POSY")/4));
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
		if (getTokenX() < getStartX() || getTokenY() < getStartY() || getTokenX() > getEndX() || getTokenY() > getEndY()) {
			setTokenX((getStartX() + getEndX())/2);
			setTokenY((getStartY() + getEndY())/2);
		}
	}

	private void populateCitizenCount() {
		Statement statement = null;
		Statement statement_players = null;
		ResultSet result = null;
		ResultSet result_players = null;
		try {
			statement = MapBuilder.dbhandler.getZonesConnection().createStatement();
			result = statement.executeQuery("SELECT WURMID FROM CITIZENS WHERE VILLAGEID='"+villageID+"';");
			while (result.next()) {
				final long tempID = result.getLong("WURMID");
				statement_players = MapBuilder.dbhandler.getPlayersConnection().createStatement();
				result_players = statement_players.executeQuery("Select WURMID FROM PLAYERS WHERE WURMID='"+tempID+"';");
				if (result_players.next()) {
					this.citizenCount++;
				}
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
			if (result_players != null) {
				try {
					result_players.close();
				} catch (SQLException e) { }
			}
			if (statement_players != null) {
				try {
					statement_players.close();
				} catch (SQLException e) { }
			}
		}
		if (getTokenX() < getStartX() || getTokenY() < getStartY() || getTokenX() > getEndX() || getTokenY() > getEndY()) {
			setTokenX((getStartX() + getEndX())/2);
			setTokenY((getStartY() + getEndY())/2);
		}
	}

	public String getVillageName() {
		return villageName;
	}

	private void setVillageName(String villageName) {
		this.villageName = villageName;
	}

	public String getMayorName() {
		return mayorName;
	}

	private void setMayorName(String mayorName) {
		this.mayorName = mayorName;
	}

	public String getMotto() {
		return motto;
	}

	private void setMotto(String motto) {
		this.motto = motto;
	}

	public int getStartX() {
		return startX;
	}

	private void setStartX(int startX) {
		this.startX = startX;
	}

	public int getStartY() {
		return startY;
	}

	private void setStartY(int startY) {
		this.startY = startY;
	}

	public int getEndX() {
		return endX;
	}

	private void setEndX(int endX) {
		this.endX = endX;
	}

	public int getEndY() {
		return endY;
	}

	private void setEndY(int endY) {
		this.endY = endY;
	}

	public boolean isPermanent() {
		return permanent;
	}

	private void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public long getDeedID() {
		return deedID;
	}

	private void setDeedID(long deedID) {
		this.deedID = deedID;
	}

	public long getTokenID() {
		return tokenID;
	}

	private void setTokenID(long tokenID) {
		this.tokenID = tokenID;
	}

	public int getTokenX() {
		return tokenX;
	}

	private void setTokenX(int tokenX) {
		this.tokenX = tokenX;
	}

	public int getTokenY() {
		return tokenY;
	}

	private void setTokenY(int tokenY) {
		this.tokenY = tokenY;
	}

	public int getCitizenCount() {
		return citizenCount;
	}

}
