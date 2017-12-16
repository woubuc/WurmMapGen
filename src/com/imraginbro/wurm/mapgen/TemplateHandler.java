package com.imraginbro.wurm.mapgen;

import com.samskivert.mustache.Mustache;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TemplateHandler {

	private final String templateDirectory = "./template";
	
	/**
	 * Renders the index.html Pebble template and writes the output to the configured output directory
	 */
	public void render() {
		System.out.println("\nTemplate");
		
		try {
			if (MapBuilder.propertiesManager.verbose) System.out.println("      assembling template data");
			
			Map<String, Object> data = new HashMap<>();
			data.put("serverName", MapBuilder.propertiesManager.serverName);
			data.put("enableRealtimeMarkers", MapBuilder.propertiesManager.enableRealtimeMarkers);
			
			data.put("showPlayers", MapBuilder.propertiesManager.showPlayers);
			data.put("showDeeds", MapBuilder.propertiesManager.showDeeds);
			data.put("showGuardTowers", MapBuilder.propertiesManager.showGuardTowers);
			data.put("showStructures", MapBuilder.propertiesManager.showStructures);
			
			if (MapBuilder.propertiesManager.verbose) System.out.println("      compiling index.html");
			
			FileReader template = new FileReader(templateDirectory + File.separator + "index.html");
			FileWriter output = new FileWriter(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + File.separator + "index.html", false);
			
			Mustache.compiler().compile(template).execute(data, output);
			
			template.close();
			output.close();
			
		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("   OK rendered index.html");
	}
	
	/**
	 * Copies the stylesheets, javascript files and images from the template directory into the output directory.
	 */
	public void copyAssets() throws IOException {
		System.out.println("\nTemplate assets");
		
		copyAssetsDirectory("app");
		copyAssetsDirectory("css");
		copyAssetsDirectory("data");
		copyAssetsDirectory("dist");
		copyAssetsDirectory("includes");
		copyAssetsDirectory("markers");
		
		System.out.println("   OK template asset files copied");
	}
	
	/**
	 * Copies a subdirectory of the template directory into the configured destination
	 * @param directory the directory within ./template to copy
	 */
	private void copyAssetsDirectory(String directory) throws IOException {
		if (MapBuilder.propertiesManager.verbose) System.out.println("      copying directory " + directory);
		
		FileUtils.copyDirectory(
				new File(templateDirectory + File.separator + directory),
				new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + File.separator + directory)
		);
	}

}
