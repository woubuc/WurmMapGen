package be.woubuc.wurmunlimited.wurmmapgen;

import be.woubuc.wurmunlimited.wurmmapgen.filegen.FileGen;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WurmMapGen {
	
	public final static PropertiesManager properties = new PropertiesManager();
	public final static FileManager fileManager = new FileManager();
	public final static DatabaseHandler db = new DatabaseHandler();
	public final static TileMapGenerator tileMapGenerator = new TileMapGenerator();
	
	private final static TemplateHandler templateHandler = new TemplateHandler();
	private final static FileGen fileGenerator = new FileGen();
	
	public static void main(String[] args) throws Exception {
		System.out.println("WurmMapGen");
		System.out.println("      Developed and maintained by woubuc");
		System.out.println("      Based on original code by garrett94");
		System.out.println("      More info: github.com/woubuc/WurmMapGen");
		
		// Get path to the properties file
		Path propertiesFilePath = Paths.get("./WurmMapGen.properties").normalize();
		
		// Custom properties file path: -c filename
		if (args.length >= 2) {
			if (args[0].equals("-c")) {
				propertiesFilePath = Paths.get(args[1]);
			}
		}
		
		System.out.println();
		System.out.println("Using properties file: " + propertiesFilePath.toAbsolutePath());
		System.out.println("      To use a different file: -c yourfile.properties");
		
		// Allow for users to try something like -h or -help and just get the above info without running the mapgen
		if (args.length != 0 && args.length != 2) {
			return;
		}
		
		// Time the duration of the map generation
		final long startTime = System.currentTimeMillis();
		
		
		if (!properties.load(propertiesFilePath)) return;
		
		fileManager.load();
		tileMapGenerator.openMap();
		
		if (!db.openDatabaseConnections()) return;
		
		tileMapGenerator.generateMapTiles();
		
		templateHandler.copyAssets();
		templateHandler.render();
		
		fileGenerator.generateDataFiles();
		
		if (!db.closeDatabaseConnections()) return;
		
		tileMapGenerator.closeMap();
		fileManager.unload();
		
		
		System.out.println();
		System.out.println("Map generated");
		System.out.println("      Duration: " + (System.currentTimeMillis() - startTime) + " ms");
	}
}
