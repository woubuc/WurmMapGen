package be.woubuc.wurmunlimited.wurmmapgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesManager {
	
	public String serverName = "";
	
	public boolean enableRealtimeMarkers = false;
	public String rmiHost = "localhost";
	public String rmiPort = "8080";
	
	public int markerType = 2;
	
	public boolean showPlayers = true;
	public Boolean showDeeds = true;
	public Boolean showGuardTowers = true;
	public Boolean showStructures = true;
	
	public Boolean verbose = false;
	
	public int mapGeneratorThreads = 2;
	
	public Boolean mapGenerateShading = true;
	public Boolean mapShadePaths = true;
	public Boolean mapGenerateWater = true;
	public Boolean mapGenerateBridges = true;
	
	public File wurmMapLocation;
	public File saveLocation;
	
	/**
	 * Loads the properties from the file WurmMapGen.properties
	 * @param  propertiesFilePath  The path of the properties file to load
	 * @return true if the properties were loaded successfully
	 */
	public boolean load(Path propertiesFilePath) {
		System.out.println();
		System.out.println("Properties");
		
		// Load properties file
		Properties properties = new Properties();
		try (InputStream input = new FileInputStream(propertiesFilePath.toAbsolutePath().toString())) {
			System.out.println("      loading properties file");
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
		
		this.enableRealtimeMarkers = Boolean.parseBoolean(properties.getProperty("enableRealtimeMarkers", Boolean.toString(this.enableRealtimeMarkers)));
		this.rmiHost = properties.getProperty("rmiHost");
		this.rmiPort = properties.getProperty("rmiPort");
		
		this.markerType = Integer.parseInt(properties.getProperty("markerType", Integer.toString(this.markerType)));
		
		this.showPlayers = Boolean.parseBoolean(properties.getProperty("showPlayers", Boolean.toString(this.showPlayers)));
		this.showDeeds = Boolean.parseBoolean(properties.getProperty("showDeeds", Boolean.toString(this.showDeeds)));
		this.showGuardTowers = Boolean.parseBoolean(properties.getProperty("showGuardTowers", Boolean.toString(this.showGuardTowers)));
		this.showStructures = Boolean.parseBoolean(properties.getProperty("showStructures", Boolean.toString(this.showStructures)));
		
		this.verbose = Boolean.parseBoolean(properties.getProperty("verbose", Boolean.toString(this.verbose)));
		
		this.mapGeneratorThreads = Integer.parseInt(properties.getProperty("mapGeneratorThreads", Integer.toString(this.mapGeneratorThreads)));
		
		this.mapGenerateShading = Boolean.parseBoolean(properties.getProperty("mapGenerateShading", Boolean.toString(this.mapGenerateShading)));
		this.mapShadePaths = Boolean.parseBoolean(properties.getProperty("mapShadePaths", Boolean.toString(this.mapShadePaths)));
		this.mapGenerateWater = Boolean.parseBoolean(properties.getProperty("mapGenerateWater", Boolean.toString(this.mapGenerateWater)));
		this.mapGenerateBridges = Boolean.parseBoolean(properties.getProperty("mapGenerateBridges", Boolean.toString(this.mapGenerateBridges)));
		
		this.wurmMapLocation = new File(mapLocation);
		this.saveLocation = new File(saveLocation);
		
		if (this.markerType < 1 || this.markerType > 3) {
			System.out.println("ERROR deed marker type should be between 1 - 3");
			return false;
		}

		return true;
	}
}
