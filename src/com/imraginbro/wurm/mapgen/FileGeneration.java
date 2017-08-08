package com.imraginbro.wurm.mapgen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import com.wurmonline.mesh.MeshIO;

public class FileGeneration {

	final static String newLine = System.lineSeparator();
	final static String separator = java.io.File.separator;

	static int html_nativeZoom = 0;
	static int html_mapMinZoom = 0;
	static int html_mapMaxZoom = 0;
	static int html_actualMapSize = 0;
	static int html_maxMapSize = 0;

	public static void generateFiles(MeshIO map) throws IOException, SQLException {
		setHTMLvars(map);
		generateDeedsFile(MapGen.saveLocation, MapGen.db_wurmZones);
		generateGuardTowersFile(MapGen.saveLocation, MapGen.db_wurmItems, MapGen.db_wurmPlayers);
		generateStructuresFile(MapGen.saveLocation, MapGen.db_wurmZones, MapGen.db_wurmPlayers);
		generateConfigFile(MapGen.saveLocation);
	}
	
	public static void generateStructuresFile(File saveLocation, File db_wurmZones, File db_wurmPlayers) throws IOException, SQLException {
		if (!MapGen.showStructures) {
			return;
		}
		if (!db_wurmZones.exists()) {
			System.out.println("[ERROR] Could not find wurmzones.db. Skipping structures.js file generation.");
			return;
		}
		if (!db_wurmPlayers.exists()) {
			System.out.println("[ERROR] Could not find wurmplayers.db. Skipping structures.js file generation.");
			return;
		}
		System.out.println("Writing structures.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(saveLocation.getAbsolutePath() + separator + "structures.js", false));
		String structBordersString = "";
		System.out.println("Loading structures from wurmzones.db...");
		Connection zonesDBcon = DriverManager.getConnection("jdbc:sqlite:"+db_wurmZones);
		Connection playersDBcon = DriverManager.getConnection("jdbc:sqlite:"+db_wurmPlayers);
		Statement statement = zonesDBcon.createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT * FROM STRUCTURES WHERE FINISHED='1';"); 

		structBordersString += ("function getStructures() {" + newLine);
		structBordersString += ("\tvar structureBorders = [];" + newLine);

		int count = 0;
		while (resultSet.next()) {
			int minX = -1;
			int maxX = -1;
			int minY = -1;
			int maxY = -1;
			long structureID = resultSet.getLong("WURMID");
			
			Statement structStatement = zonesDBcon.createStatement(); 
			ResultSet structRes = structStatement.executeQuery("SELECT TILEX, TILEY FROM BUILDTILES WHERE STRUCTUREID='"+structureID+"';"); 
			if (structRes.next()) {
				minX = structRes.getInt("TILEX");
				maxX = structRes.getInt("TILEX");
				minY = structRes.getInt("TILEY");
				maxY = structRes.getInt("TILEY");
				while (structRes.next()) {
					final int nX = structRes.getInt("TILEX");
					final int nY = structRes.getInt("TILEY");
					if (nX < minX) {
						minX = nX;
					}
					if (nX > maxX) {
						maxX = nX;
					}
					if (nY < minY) {
						minY = nY;
					}
					if (nY > maxY) {
						maxY = nY;
					}
				}
			}
			
			maxX++;
			maxY++;
			
			structStatement.close();
			
			String name = resultSet.getString("NAME");
			long ownerID = resultSet.getLong("OWNERID");
			Statement nameStatement = playersDBcon.createStatement();  
			ResultSet nameRes = nameStatement.executeQuery("SELECT NAME FROM PLAYERS WHERE WURMID='"+ownerID+"';"); 
			String pname = "";
			if (nameRes.next()) {
				pname = nameRes.getString("NAME");
			}
			nameStatement.close();
			structBordersString += ("\tstructureBorders.push(L.polygon([");
			structBordersString += ("xy("+minX+","+minY+"),");
			structBordersString += ("xy("+maxX+","+minY+"),");
			structBordersString += ("xy("+maxX+","+maxY+"),");
			structBordersString += ("xy("+minX+","+maxY+")]");
			structBordersString += (", {color:'blue',fillOpacity:0.1,weight:1})");
			structBordersString += (".bindPopup(\"<div align='center'><b>" + name + "</b><br><i>Created by " + pname + "</i></div>\"));" + newLine);

			count++;
		}
		System.out.println("Added "+count+" structures to structures.js...");
		structBordersString += ("\treturn structureBorders;" + newLine);
		structBordersString += ("}" + newLine + newLine);
		bw.append(structBordersString);
		zonesDBcon.close();
		playersDBcon.close();
		bw.close();
	}

	public static void generateGuardTowersFile(File saveLocation, File db_wurmItems, File db_wurmPlayers) throws IOException, SQLException {
		if (!MapGen.showGuardTowers) {
			return;
		}
		if (!db_wurmItems.exists()) {
			System.out.println("[ERROR] Could not find wurmitems.db. Skipping guardtowers.js file generation.");
			return;
		}
		if (!db_wurmPlayers.exists()) {
			System.out.println("[ERROR] Could not find wurmplayers.db. Skipping guardtowers.js file generation.");
			return;
		}
		System.out.println("Writing guardtowers.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(saveLocation.getAbsolutePath() + separator + "guardtowers.js", false));
		String deedBordersString = "";
		String deedMarkersString = "";
		System.out.println("Loading guard towers from wurmitems.db...");
		Connection itemsDBcon = DriverManager.getConnection("jdbc:sqlite:"+db_wurmItems);
		Connection playersDBcon = DriverManager.getConnection("jdbc:sqlite:"+db_wurmPlayers);
		Statement statement = itemsDBcon.createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT * FROM ITEMS WHERE (TEMPLATEID='384' OR TEMPLATEID='430' OR TEMPLATEID='528' OR TEMPLATEID='638' OR TEMPLATEID='996') AND CREATIONSTATE='0';"); 

		deedBordersString += ("function getGuardTowerBorders() {" + newLine);
		deedBordersString += ("\tvar guardTowerBorders = [];" + newLine);

		deedMarkersString += ("function getGuardTowers() {" + newLine);
		deedMarkersString += ("\tvar guardTower = [];" + newLine);

		int count = 0;
		while (resultSet.next()) {
			int x = (int) Math.floor(resultSet.getInt("POSX")/4);
			int y = (int) Math.floor(resultSet.getInt("POSY")/4);
			float ql = resultSet.getFloat("QUALITYLEVEL");
			float dmg = resultSet.getFloat("DAMAGE");
			long ownerID = resultSet.getLong("LASTOWNERID");
			Statement nameStatement = playersDBcon.createStatement();  
			ResultSet nameRes = nameStatement.executeQuery("SELECT NAME FROM PLAYERS WHERE WURMID='"+ownerID+"';"); 
			String pname = "";
			if (nameRes.next()) {
				pname = nameRes.getString("NAME");
			}
			nameStatement.close();
			deedBordersString += ("\tguardTowerBorders.push(L.polygon([");
			deedBordersString += ("xy("+(x-50)+","+(y-50)+"),");
			deedBordersString += ("xy("+(x+51)+","+(y-50)+"),");
			deedBordersString += ("xy("+(x+51)+","+(y+51)+"),");
			deedBordersString += ("xy("+(x-50)+","+(y+51)+")]");
			deedBordersString += (", {color:'red',fillOpacity:0.1,weight:1}));" + newLine);

			deedMarkersString += ("\tguardTower.push(L.marker(");
			deedMarkersString += ("xy("+(x+0.5)+","+(y+0.5)+"),");
			deedMarkersString += ("{icon: guardTowerIcon})");
			DecimalFormat f = new DecimalFormat("0.00");
			deedMarkersString += (".bindPopup(\"<div align='center'><b>Guard Tower</b><br><i>Created by " + pname + "</i></div><br><b>QL:</b> " + f.format(ql) + "<br><b>DMG:</b> " + f.format(dmg) + "\"));" + newLine);

			count++;
		}
		System.out.println("Added "+count+" guard towers to guardtowers.js...");
		deedBordersString += ("\treturn guardTowerBorders;" + newLine);
		deedBordersString += ("}" + newLine + newLine);
		deedMarkersString += ("\treturn guardTower;" + newLine);
		deedMarkersString += ("}");
		bw.append(deedBordersString);
		bw.append(deedMarkersString);
		itemsDBcon.close();
		playersDBcon.close();
		bw.close();
	}

	public static void generateDeedsFile(File saveLocation, File db_wurmZones) throws IOException, SQLException {
		if (!MapGen.showDeeds) {
			return;
		}
		if (!db_wurmZones.exists()) {
			System.out.println("[ERROR] Could not find wurmzones.db. Skipping deeds.js file generation.");
			return;
		}
		System.out.println("Writing deeds.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(saveLocation.getAbsolutePath() + separator + "deeds.js", false));
		String deedBordersString = "";
		String deedMarkersString = "";
		String mainDeedString = "";
		System.out.println("Loading deeds from wurmzones.db...");
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+db_wurmZones);
		Statement statement = connection.createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT * FROM VILLAGES WHERE DISBANDED=0;"); 

		mainDeedString += ("function setViewOnMainDeed(map) {" + newLine);

		deedBordersString += ("function deedBorders() {" + newLine);
		deedBordersString += ("\tvar deedBorders = [];" + newLine);

		deedMarkersString += ("function deedMarkers() {" + newLine);
		deedMarkersString += ("\tvar deedMarkers = [];" + newLine);

		double mainX = 0;
		double mainY = 0;

		int count = 0;
		while (resultSet.next()) {
			int sx = resultSet.getInt("STARTX");
			int sy = resultSet.getInt("STARTY");
			int ex = resultSet.getInt("ENDX");
			int ey = resultSet.getInt("ENDY");
			double x = (sx + ex + 1) / 2;
			double y = (sy + ey + 1) / 2;
			deedBordersString += ("\tdeedBorders.push(L.polygon([");
			deedBordersString += ("xy("+sx+","+sy+"),");
			deedBordersString += ("xy("+(ex+1)+","+sy+"),");
			deedBordersString += ("xy("+(ex+1)+","+(ey+1)+"),");
			deedBordersString += ("xy("+sx+","+(ey+1)+")]");
			boolean perm = resultSet.getBoolean("PERMANENT");
			if (perm) {
				deedBordersString += (", {color:'orange',fillOpacity:0,weight:1})");
				if (mainX == 0 && mainY == 0) {
					mainX = x;
					mainY = y;
					mainDeedString += ("\tmap.setView(xy("+mainX+","+mainY+"), config.mapMaxZoom-1)" + newLine);
				}
			} else {
				deedBordersString += (", {color:'white',fillOpacity:0,weight:1})");
			}
			deedBordersString += (".bindPopup(\"" + resultSet.getString("NAME") + "\"));" + newLine);

			String firstLetter = resultSet.getString("NAME").substring(0, 1).toLowerCase();
			deedMarkersString += ("\tdeedMarkers.push(L.marker(");
			deedMarkersString += ("xy("+(x+0.5)+","+(y+0.5)+"),");
			if (perm) {
				deedMarkersString += ("{icon: mainIcon})");
			} else {
				deedMarkersString += ("{icon: letter_"+firstLetter+"Icon})");
			}
			deedMarkersString += (".bindPopup(\""+resultSet.getString("NAME")+"\"));" + newLine);
			count++;
		}
		System.out.println("Added "+count+" deeds to deeds.js...");
		deedBordersString += ("\treturn deedBorders;" + newLine);
		deedBordersString += ("}" + newLine + newLine);
		deedMarkersString += ("\treturn deedMarkers;" + newLine);
		deedMarkersString += ("}");
		mainDeedString += ("}" + newLine + newLine);
		bw.append(mainDeedString);
		bw.append(deedBordersString);
		bw.append(deedMarkersString);
		connection.close();
		bw.close();
	}

	public static void generateConfigFile(File saveLocation) throws IOException {
		System.out.println("Writing config.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(saveLocation.getAbsolutePath() + separator + "config.js", false));
		bw.append("function Config() {}" + newLine);
		bw.append("var config = new Config();" + newLine);
		bw.append("config.nativeZoom = "+html_nativeZoom+";" + newLine);
		bw.append("config.mapMinZoom = "+html_mapMinZoom+";" + newLine);
		bw.append("config.mapMaxZoom = "+html_mapMaxZoom+";" + newLine);
		bw.append("config.actualMapSize = "+html_actualMapSize+";" + newLine);
		bw.append("config.maxMapSize = "+html_maxMapSize+";" + newLine);
		bw.append("" + newLine);
		bw.append("var xyMulitiplier = (config.actualMapSize / 256);" + newLine);
		bw.append("" + newLine);
		bw.append("var yx = L.latLng;" + newLine);
		bw.append("var xy = function(x, y) {" + newLine);
		bw.append("\treturn yx(-(y / xyMulitiplier), (x / xyMulitiplier));" + newLine);
		bw.append("};");
		bw.close();
	}

	public static void setHTMLvars(MeshIO map) {
		System.out.println("Generating config.js variables...");
		html_actualMapSize = map.getSize();
		html_maxMapSize = html_actualMapSize * 8;
		int count = 0;
		for (int i = html_actualMapSize; i > 256; i++) {
			i = (i/2);
			html_nativeZoom = count;
			count++;
		}
		count = 0;
		for (int i = html_maxMapSize; i > 256; i++) {
			i = (i/2);
			html_mapMaxZoom = count;
			count++;
		}
		html_mapMinZoom = 1;
	}

}
