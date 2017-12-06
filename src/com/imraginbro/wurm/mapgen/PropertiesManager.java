package com.imraginbro.wurm.mapgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class PropertiesManager {
	
	private final String propertiesFile = "WurmMapGen.properties";
	
	public Boolean showDeeds = true;
	public Boolean showGuardTowers = true;
	public Boolean showStructures = true;
	
	public Boolean verbose = false;
	
	public int threadLimit = 2;
	
	public Boolean gen_map_shading = true;
	public Boolean gen_map_shade_paths = true;
	public Boolean gen_map_water = true;
	public Boolean gen_map_bridges = true;
	
	public Boolean replaceFiles = true;
	
	public File wurmMapLocation;
	public File saveLocation;
	
	private void copyFromJar() {
		try {
			InputStream in = this.getClass().getResourceAsStream("./" + this.propertiesFile);
			MapBuilder.fileManager.copy(in, this.propertiesFile);
			in.close();
		} catch(Exception e) {
			System.out.println("Error copying properties file from jar - " + e.getMessage());
		}
	}

	public boolean load() {
		System.out.println("Loading " + propertiesFile + " file!");
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream(propertiesFile);
		} catch (Exception e) {
			System.out.println("[ERROR] problem loading properties FileInputStream - " + e.getMessage());
			System.out.println("Copying properties file from jar... please configure and restart program.");
			copyFromJar();
			return false;
		}
		
		if (input != null) {
			try {
				prop.load(input);
			} catch (Exception e) {
				System.out.println("Error loading properties file - " + e.getMessage());
			}
		}
		
		final String maploc = prop.getProperty("wurmMapLocation", "C:/location/to/map/folder");
		final String saveloc = prop.getProperty("saveLocation", "C:/location/to/save/folder");
		
		System.out.println("[INFO] Map location: " + maploc);
		System.out.println("[INFO] Save location: " + saveloc);
		
		if (maploc.equals("C:/location/to/map/folder") || saveloc.equals("C:/location/to/save/folder")) {
			System.out.println("[ERROR] Looks like you are using the default map or save location. Please change in your config file.");
			return false;
		}
		
		this.showDeeds = Boolean.parseBoolean(prop.getProperty("showDeeds", Boolean.toString(this.showDeeds)));
		this.showGuardTowers = Boolean.parseBoolean(prop.getProperty("showGuardTowers", Boolean.toString(this.showGuardTowers)));
		this.showStructures = Boolean.parseBoolean(prop.getProperty("showStructures", Boolean.toString(this.showStructures)));
		
		this.verbose = Boolean.parseBoolean(prop.getProperty("verbose", Boolean.toString(this.verbose)));
		
		this.threadLimit = Integer.parseInt(prop.getProperty("threads", Integer.toString(this.threadLimit)));
		
		this.gen_map_shading = Boolean.parseBoolean(prop.getProperty("mapGenerateShading", Boolean.toString(this.gen_map_shading)));
		this.gen_map_shade_paths = Boolean.parseBoolean(prop.getProperty("mapShadePaths", Boolean.toString(this.gen_map_shade_paths)));
		this.gen_map_water = Boolean.parseBoolean(prop.getProperty("mapGenerateWater", Boolean.toString(this.gen_map_water)));
		this.gen_map_bridges = Boolean.parseBoolean(prop.getProperty("mapGenerateBridges", Boolean.toString(this.gen_map_bridges)));
		
		this.wurmMapLocation = new File(maploc);
		this.saveLocation = new File(saveloc);
		
		this.replaceFiles = Boolean.parseBoolean(prop.getProperty("replaceFiles", Boolean.toString(this.replaceFiles)));

		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
}
