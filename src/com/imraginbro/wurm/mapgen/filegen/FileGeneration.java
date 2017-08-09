package com.imraginbro.wurm.mapgen.filegen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import com.imraginbro.wurm.mapgen.MapBuilder;
import com.wurmonline.mesh.MeshIO;

public class FileGeneration {

	final static String newLine = System.lineSeparator();
	final static String separator = java.io.File.separator;

	private int html_nativeZoom = 0;
	private int html_mapMinZoom = 0;
	private int html_mapMaxZoom = 0;
	private int html_actualMapSize = 0;
	private int html_maxMapSize = 0;

	public void generateFiles() throws IOException, SQLException {
		setHTMLvars(MapBuilder.map);
		generateDeedsFile();
		generateGuardTowersFile();
		generateStructuresFile();
		generateConfigFile();
	}

	public void generateStructuresFile() throws IOException, SQLException {
		if (!MapBuilder.propertiesManager.showStructures || !MapBuilder.dbhandler.checkZonesConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.out.println("Skipping structures.js generation.");
			return;
		}

		System.out.println("Writing structures.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "structures.js", false));
		
		System.out.println("Loading structures from wurmzones.db...");
		
		Statement statement = MapBuilder.dbhandler.getZonesConnection().createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT WURMID FROM STRUCTURES WHERE FINISHED='1';"); 
		
		bw.append("function getStructures() {" + newLine);
		bw.append("\tvar structureBorders = [];" + newLine);
		int count = 0;
		while (resultSet.next()) {
			long structureID = resultSet.getLong("WURMID");
			Structure structure = new Structure(MapBuilder.dbhandler, structureID);
			bw.append("\tstructureBorders.push(L.polygon(["
					+ "xy("+structure.getMinX()+","+structure.getMinY()+"),"
					+ "xy("+structure.getMaxX()+","+structure.getMinY()+"),"
					+ "xy("+structure.getMaxX()+","+structure.getMaxY()+"),"
					+ "xy("+structure.getMinX()+","+structure.getMaxY()+")]"
					+ ", {color:'blue',fillOpacity:0.1,weight:1})"
					+ ".bindPopup(\"<div align='center'><b>" + structure.getStructureName() + "</b><br>"
					+ "<i>Created by " + structure.getOwnerName() + "</i></div>\"));" + newLine);
			count++;
		}
		System.out.println("Added " + count + " structures to structures.js...");
		bw.append("\treturn structureBorders;" + newLine);
		bw.append("}");
		bw.close();
	}

	public void generateGuardTowersFile() throws IOException, SQLException {
		if (!MapBuilder.propertiesManager.showGuardTowers || !MapBuilder.dbhandler.checkItemsConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.out.println("Skipping guardtowers.js generation.");
			return;
		}

		System.out.println("Writing guardtowers.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "guardtowers.js", false));
		StringBuilder deedBordersString = new StringBuilder();
		StringBuilder deedMarkersString = new StringBuilder();
		System.out.println("Loading guard towers from wurmitems.db...");
		Statement statement = MapBuilder.dbhandler.getItemsConnection().createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT * FROM ITEMS WHERE (TEMPLATEID='384' OR TEMPLATEID='430' OR TEMPLATEID='528' OR TEMPLATEID='638' OR TEMPLATEID='996') AND CREATIONSTATE='0';"); 

		deedBordersString.append("function getGuardTowerBorders() {" + newLine);
		deedBordersString.append("\tvar guardTowerBorders = [];" + newLine);

		deedMarkersString.append("function getGuardTowers() {" + newLine);
		deedMarkersString.append("\tvar guardTower = [];" + newLine);

		int count = 0;
		while (resultSet.next()) {
			
			
			int x = (int) Math.floor(resultSet.getInt("POSX")/4);
			int y = (int) Math.floor(resultSet.getInt("POSY")/4);
			float ql = resultSet.getFloat("QUALITYLEVEL");
			float dmg = resultSet.getFloat("DAMAGE");
			long ownerID = resultSet.getLong("LASTOWNERID");
			Statement nameStatement = MapBuilder.dbhandler.getPlayersConnection().createStatement();  
			ResultSet nameRes = nameStatement.executeQuery("SELECT NAME FROM PLAYERS WHERE WURMID='" + ownerID + "';"); 
			String pname = "";
			if (nameRes.next()) {
				pname = nameRes.getString("NAME");
			}
			nameStatement.close();
			
			
			deedBordersString.append("\tguardTowerBorders.push(L.polygon([");
			deedBordersString.append("xy("+(x-50)+","+(y-50)+"),");
			deedBordersString.append("xy("+(x+51)+","+(y-50)+"),");
			deedBordersString.append("xy("+(x+51)+","+(y+51)+"),");
			deedBordersString.append("xy("+(x-50)+","+(y+51)+")]");
			deedBordersString.append(", {color:'red',fillOpacity:0.1,weight:1}));" + newLine);

			deedMarkersString.append("\tguardTower.push(L.marker(");
			deedMarkersString.append("xy("+(x+0.5)+","+(y+0.5)+"),");
			deedMarkersString.append("{icon: guardTowerIcon})");
			DecimalFormat f = new DecimalFormat("0.00");
			deedMarkersString.append(".bindPopup(\"<div align='center'><b>Guard Tower</b><br><i>Created by " + pname + "</i></div><br><b>QL:</b> " + f.format(ql) + "<br><b>DMG:</b> " + f.format(dmg) + "\"));" + newLine);

			count++;
		}
		System.out.println("Added " + count + " guard towers to guardtowers.js...");
		
		deedBordersString.append("\treturn guardTowerBorders;" + newLine);
		deedBordersString.append("}" + newLine + newLine);
		
		deedMarkersString.append("\treturn guardTower;" + newLine);
		deedMarkersString.append("}");
		
		bw.append(deedBordersString);
		bw.append(deedMarkersString);
		
		bw.close();
	}

	public void generateDeedsFile() throws IOException, SQLException {
		if (!MapBuilder.propertiesManager.showDeeds || !MapBuilder.dbhandler.checkZonesConnection()) {
			System.out.println("Skipping deeds.js generation.");
			return;
		}
		System.out.println("Writing deeds.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "deeds.js", false));
		String deedBordersString = "";
		String deedMarkersString = "";
		String mainDeedString = "";
		System.out.println("Loading deeds from wurmzones.db...");

		Statement statement = MapBuilder.dbhandler.getZonesConnection().createStatement();  
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
		
		bw.close();
	}

	public void generateConfigFile() throws IOException {
		System.out.println("Writing config.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "config.js", false));
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

	public void setHTMLvars(MeshIO map) {
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
