package be.woubuc.wurmunlimited.wurmmapgen;

import com.samskivert.mustache.Mustache;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemplateHandler {

	private final String templateDirectory = "./template";
	
	/**
	 * Renders the index.html Pebble template and writes the output to the configured output directory
	 */
	public void render() {
		System.out.println();
		System.out.println("Template");
		
		try {
			if (WurmMapGen.properties.verbose) System.out.println("      Assembling template data");
			
			Map<String, Object> data = new HashMap<>();
			data.put("serverName", WurmMapGen.properties.serverName);
			data.put("enableRealtimeMarkers", WurmMapGen.properties.enableRealtimeMarkers);
			
			data.put("showPlayers", WurmMapGen.properties.showPlayers);
			data.put("showDeeds", WurmMapGen.properties.showDeeds);
			data.put("showGuardTowers", WurmMapGen.properties.showGuardTowers);
			data.put("showStructures", WurmMapGen.properties.showStructures);
			
			if (WurmMapGen.properties.verbose) System.out.println("      Compiling index.html");
			
			FileReader template = new FileReader(templateDirectory + File.separator + "index.html");
			FileWriter output = new FileWriter(WurmMapGen.properties.saveLocation.getAbsolutePath() + File.separator + "index.html", false);
			
			Mustache.compiler().compile(template).execute(data, output);
			
			template.close();
			output.close();
			
		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("   OK Rendered index.html");
	}
	
	/**
	 * Copies the stylesheets, javascript files and images from the template directory into the output directory.
	 */
	public void copyAssets() throws IOException {
		System.out.println();
		System.out.println("Template assets");
		
		copyAssetsDirectory("app");
		copyAssetsDirectory("css");
		copyAssetsDirectory("data");
		copyAssetsDirectory("dist");
		copyAssetsDirectory("includes");
		copyAssetsDirectory("markers");
		
		System.out.println("   OK Template asset files copied");
	}
	
	/**
	 * Copies a subdirectory of the template directory into the configured destination
	 * @param directory the directory within ./template to copy
	 */
	private void copyAssetsDirectory(String directory) throws IOException {
		if (WurmMapGen.properties.verbose) System.out.println("      Copying directory " + directory);
		
		FileUtils.copyDirectory(
				new File(templateDirectory + File.separator + directory),
				new File(WurmMapGen.properties.saveLocation.getAbsolutePath() + File.separator + directory)
		);
	}

}
