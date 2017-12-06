package com.imraginbro.wurm.mapgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {
	
	public String serverName = "";
	
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
	
	/**
	 * Loads the properties from the file WurmMapGen.properties
	 * @return true if the properties were loaded successfully
	 */
	public boolean load() {
		System.out.println();
		System.out.println("Properties");
		String propertiesFile = "./WurmMapGen.properties";
		
		// Load properties file
		Properties properties = new Properties();
		try (InputStream input = new FileInputStream(propertiesFile)) {
			System.out.println("      loading properties file " + propertiesFile);
			properties.load(input);
		} catch (Exception e) {
			System.err.println("ERROR could not load properties: " + e.getMessage());
			return false;
		}
		
		final String mapLocation = properties.getProperty("wurmMapLocation", "C:/location/to/map/folder");
		final String saveLocation = properties.getProperty("saveLocation", "C:/location/to/save/folder");
		
		// Verify that the default settings have been changed
		if (mapLocation.equals("C:/location/to/map/folder") || saveLocation.equals("C:/location/to/save/folder")) {
			System.err.println("ERROR you are using default map or save location, please edit your properties file");
			return false;
		}
		
		System.out.println("      map location: " + mapLocation);
		System.out.println("      save location: " + saveLocation);
		
		this.serverName = properties.getProperty("serverName");
		
		this.showDeeds = Boolean.parseBoolean(properties.getProperty("showDeeds", Boolean.toString(this.showDeeds)));
		this.showGuardTowers = Boolean.parseBoolean(properties.getProperty("showGuardTowers", Boolean.toString(this.showGuardTowers)));
		this.showStructures = Boolean.parseBoolean(properties.getProperty("showStructures", Boolean.toString(this.showStructures)));
		
		this.verbose = Boolean.parseBoolean(properties.getProperty("verbose", Boolean.toString(this.verbose)));
		
		this.threadLimit = Integer.parseInt(properties.getProperty("threads", Integer.toString(this.threadLimit)));
		
		this.gen_map_shading = Boolean.parseBoolean(properties.getProperty("mapGenerateShading", Boolean.toString(this.gen_map_shading)));
		this.gen_map_shade_paths = Boolean.parseBoolean(properties.getProperty("mapShadePaths", Boolean.toString(this.gen_map_shade_paths)));
		this.gen_map_water = Boolean.parseBoolean(properties.getProperty("mapGenerateWater", Boolean.toString(this.gen_map_water)));
		this.gen_map_bridges = Boolean.parseBoolean(properties.getProperty("mapGenerateBridges", Boolean.toString(this.gen_map_bridges)));
		
		this.wurmMapLocation = new File(mapLocation);
		this.saveLocation = new File(saveLocation);
		
		this.replaceFiles = Boolean.parseBoolean(properties.getProperty("replaceFiles", Boolean.toString(this.replaceFiles)));

		return true;
	}
	
}
