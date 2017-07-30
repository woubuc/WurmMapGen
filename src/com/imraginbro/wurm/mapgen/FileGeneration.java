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
		generateConfigFile(MapGen.saveLocation);
	}

	public static void generateDeedsFile(File saveLocation, File db_wurmZones) throws IOException, SQLException {
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
