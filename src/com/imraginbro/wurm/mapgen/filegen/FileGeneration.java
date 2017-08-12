package com.imraginbro.wurm.mapgen.filegen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

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

		bw.append("function getStructures() {" + newLine
				+ "\tvar structureBorders = [];" + newLine);
		int count = 0;
		while (resultSet.next()) {
			long structureID = resultSet.getLong("WURMID");
			Structure structure = new Structure(structureID);
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

		resultSet.close();
		statement.close();

		bw.append("\treturn structureBorders;" + newLine + "}");
		bw.close();

		System.out.println("Added " + count + " structures to structures.js...");
	}

	public void generateGuardTowersFile() throws IOException, SQLException {
		if (!MapBuilder.propertiesManager.showGuardTowers || !MapBuilder.dbhandler.checkItemsConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.out.println("Skipping guardtowers.js generation.");
			return;
		}

		System.out.println("Writing guardtowers.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "guardtowers.js", false));

		StringBuilder GTBordersString = new StringBuilder();
		StringBuilder GTMarkersString = new StringBuilder();

		System.out.println("Loading guard towers from wurmitems.db...");
		Statement statement = MapBuilder.dbhandler.getItemsConnection().createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT * FROM ITEMS WHERE "
				+ "(TEMPLATEID='384' OR TEMPLATEID='430' OR TEMPLATEID='528' OR TEMPLATEID='638' OR TEMPLATEID='996') AND CREATIONSTATE='0';"); 

		ArrayList<GuardTower> guardTowers = new ArrayList<GuardTower>();

		while (resultSet.next()) {
			guardTowers.add(new GuardTower(resultSet.getLong("LASTOWNERID"), 
					(int) Math.floor(resultSet.getInt("POSX")/4), 
					(int) Math.floor(resultSet.getInt("POSY")/4), 
					resultSet.getFloat("QUALITYLEVEL"), resultSet.getFloat("DAMAGE")));
		}

		resultSet.close();
		statement.close();

		GTBordersString.append("function getGuardTowerBorders() {" + newLine
				+ "\tvar guardTowerBorders = [];" + newLine);
		GTMarkersString.append("function getGuardTowers() {" + newLine
				+ "\tvar guardTower = [];" + newLine);

		final DecimalFormat f = new DecimalFormat("0.00");
		for (int i = 0; i < guardTowers.size(); i++) {
			final GuardTower gt = guardTowers.get(i);
			GTBordersString.append("\tguardTowerBorders.push(L.polygon(["
					+ "xy("+(gt.getMinX())+","+(gt.getMinY())+"),"
					+ "xy("+(gt.getMaxX()+1)+","+(gt.getMinY())+"),"
					+ "xy("+(gt.getMaxX()+1)+","+(gt.getMaxY()+1)+"),"
					+ "xy("+(gt.getMinX())+","+(gt.getMaxY()+1)+")]"
					+ ", {color:'red',fillOpacity:0.1,weight:1}));" + newLine);
			GTMarkersString.append("\tguardTower.push(L.marker("
					+ "xy("+(gt.getX()+0.5)+","+(gt.getY()+0.5)+"),"
					+ "{icon: guardTowerIcon})"
					+ ".bindPopup(\"<div align='center'><b>Guard Tower</b><br>"
					+ "<i>Created by " + gt.getOwnerName() + "</i></div><br>"
					+ "<b>QL:</b> " + f.format(gt.getQL()) + "<br>"
					+ "<b>DMG:</b> " + f.format(gt.getDMG()) + "\"));" + newLine);
		}

		GTBordersString.append("\treturn guardTowerBorders;" + newLine + "}" + newLine + newLine);
		GTMarkersString.append("\treturn guardTower;" + newLine + "}");

		bw.append(GTBordersString);
		bw.append(GTMarkersString);
		bw.close();

		System.out.println("Added " + guardTowers.size() + " guard towers to guardtowers.js...");
	}

	public void generateDeedsFile() throws IOException, SQLException {
		if (!MapBuilder.propertiesManager.showDeeds || !MapBuilder.dbhandler.checkZonesConnection() || !MapBuilder.dbhandler.checkItemsConnection() || !MapBuilder.dbhandler.checkPlayersConnection()) {
			System.out.println("Skipping deeds.js generation.");
			return;
		}

		System.out.println("Writing deeds.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "deeds.js", false));

		System.out.println("Loading deeds from wurmzones.db...");

		Statement statement = MapBuilder.dbhandler.getZonesConnection().createStatement();  
		ResultSet resultSet = statement.executeQuery("SELECT ID FROM VILLAGES WHERE DISBANDED=0;"); 
		
		ArrayList<Village> villages = new ArrayList<Village>();
		
		while (resultSet.next()) {
			villages.add(new Village(resultSet.getInt("ID")));
		}
		
		resultSet.close();
		statement.close();
		
		StringBuilder mainDeedString = new StringBuilder();
		StringBuilder deedBordersString = new StringBuilder();
		StringBuilder deedMarkersString = new StringBuilder();
		
		mainDeedString.append("function setViewOnMainDeed(map) {" + newLine);
		deedBordersString.append("function deedBorders() {" + newLine
				+ "\tvar deedBorders = [];" + newLine);
		deedMarkersString.append("function deedMarkers() {" + newLine
				+ "\tvar deedMarkers = [];" + newLine);
		
		int count = 0;
		boolean setMainDeed = false;
		for (int i = 0; i < villages.size(); i++) {
			final Village vi = villages.get(i);

			deedBordersString.append("\tdeedBorders.push(L.polygon(["
					+ "xy("+vi.getStartX()+","+vi.getStartY()+"),"
					+ "xy("+(vi.getEndX()+1)+","+vi.getStartY()+"),"
					+ "xy("+(vi.getEndX()+1)+","+(vi.getEndY()+1)+"),"
					+ "xy("+vi.getStartX()+","+(vi.getEndY()+1)+")]");
			if (vi.isPermanent()) {
				deedBordersString.append(", {color:'orange',fillOpacity:0,weight:1})");
				if (!setMainDeed) {
					mainDeedString.append("\tmap.setView(xy("+vi.getTokenX()+","+vi.getTokenY()+"), config.mapMaxZoom-1)" + newLine);
					setMainDeed = true;
				}
			} else {
				deedBordersString.append(", {color:'white',fillOpacity:0,weight:1})");
			}
			deedBordersString.append(".bindPopup(\"" + vi.getVillageName() + "\"));" + newLine);

			String firstLetter = vi.getVillageName().substring(0, 1).toLowerCase();
			deedMarkersString.append("\tdeedMarkers.push(L.marker("
					+ "xy("+(vi.getTokenX()+0.5)+","+(vi.getTokenY()+0.5)+"),");
			if (vi.isPermanent()) {
				deedMarkersString.append("{icon: mainIcon})");
			} else {
				deedMarkersString.append("{icon: letter_"+firstLetter+"Icon})");
			}
			deedMarkersString.append(".bindPopup(\"<div align='center'><b>"+vi.getVillageName()+"</b><br>"
					+ "<i>" + vi.getMotto() + "</i></div><br>"
					+ "<b>Mayor:</b> " + vi.getMayorName() + "<br>"
					+ "<b>Citizens:</b> " + vi.getCitizenCount() + "\"));" + newLine);
			
			count++;
		}

		mainDeedString.append("}" + newLine + newLine);
		deedBordersString.append("\treturn deedBorders;" + newLine + "}" + newLine + newLine);
		deedMarkersString.append("\treturn deedMarkers;" + newLine + "}");

		bw.append(mainDeedString);
		bw.append(deedBordersString);
		bw.append(deedMarkersString);
		bw.close();

		System.out.println("Added "+count+" deeds to deeds.js...");
	}

	public void generateConfigFile() throws IOException {
		System.out.println("Writing config.js file...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "includes" + separator + "config.js", false));
		bw.append("function Config() {}" + newLine
				+ "var config = new Config();" + newLine
				+ "config.nativeZoom = "+html_nativeZoom+";" + newLine
				+ "config.mapMinZoom = "+html_mapMinZoom+";" + newLine
				+ "config.mapMaxZoom = "+html_mapMaxZoom+";" + newLine
				+ "config.actualMapSize = "+html_actualMapSize+";" + newLine
				+ "config.maxMapSize = "+html_maxMapSize+";" + newLine
				+ newLine
				+ "var xyMulitiplier = (config.actualMapSize / 256);" + newLine
				+ newLine
				+ "var yx = L.latLng;" + newLine
				+ "var xy = function(x, y) {" + newLine
				+ "\treturn yx(-(y / xyMulitiplier), (x / xyMulitiplier));" + newLine
				+ "};");
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
		html_mapMinZoom = (html_mapMaxZoom - 5);
	}

}
