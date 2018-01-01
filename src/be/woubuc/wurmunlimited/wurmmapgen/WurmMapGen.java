package be.woubuc.wurmunlimited.wurmmapgen;

import be.woubuc.wurmunlimited.wurmmapgen.filegen.FileGen;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WurmMapGen {
	
	public final static PropertiesManager properties = new PropertiesManager();
	public final static FileManager fileManager = new FileManager();
	public final static DatabaseHandler db = new DatabaseHandler();
	public final static TileMapGenerator tileMapGenerator = new TileMapGenerator();
	
	private final static FileGen fileGenerator = new FileGen();
	
	public static boolean debug = false;
	public static boolean verbose = false;
	
	public static void main(String[] args) throws Exception {
		System.out.println("WurmMapGen");
		System.out.println("      Developed and maintained by woubuc");
		System.out.println("      Based on original code by garrett94");
		System.out.println("      More info: github.com/woubuc/WurmMapGen");
		
		Path propertiesFilePath = Paths.get("./WurmMapGen.properties").normalize();
		Path templateDirectoryPath = Paths.get("./template").normalize();
		
		boolean templateOnly = false;
		boolean showHelp = false;
		
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "-c":
					case "--config":
						propertiesFilePath = Paths.get(args[++i]);
						break;
						
					case "-t":
					case "--template":
						templateDirectoryPath = Paths.get(args[++i]);
						break;
					
					case "-h":
					case "--help":
						showHelp = true;
						break;
						
					case "-d":
					case "--debug":
						debug = true;
						break;
						
					case "-v":
					case "--verbose":
						verbose = true;
						break;
						
					case "--template-only":
						templateOnly = true;
						break;
						
					default:
						System.out.println("Unknown argument '" + args[i] + "'");
						break;
				}
			}
		}
		
		System.out.println();
		System.out.println("Using properties file: " + propertiesFilePath.toAbsolutePath());
		System.out.println("      To use a different file: -c yourfile.properties");
		
		if (templateOnly) {
			System.out.println();
			System.out.println("Skipping map tile generation, only updating template");
			System.out.println("      This is a dev feature and should generally not be used");
		}
		
		// Allow for users to try something like -h or -help and just get the above info without running the mapgen
		if (showHelp) {
			return;
		}
		
		// Time the duration of the map generation
		final long startTime = System.currentTimeMillis();
		
		
		if (!properties.load(propertiesFilePath)) return;
		
		fileManager.load();
		if (!templateOnly) tileMapGenerator.openMap();
		
		if (!db.openDatabaseConnections()) return;
		
		if (!templateOnly) tileMapGenerator.generateMapTiles();
		
		TemplateHandler templateHandler = new TemplateHandler(templateDirectoryPath);
		templateHandler.copyAssets();
		templateHandler.render();
		
		fileGenerator.generateDataFiles();
		
		if (!db.closeDatabaseConnections()) return;
		
		if (!templateOnly) tileMapGenerator.closeMap();
		fileManager.unload();
		
		
		System.out.println();
		System.out.println("Map generated");
		System.out.println("      Duration: " + (System.currentTimeMillis() - startTime) + " ms");
	}
}
