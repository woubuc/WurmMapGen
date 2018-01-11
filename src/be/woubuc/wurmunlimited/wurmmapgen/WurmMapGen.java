package be.woubuc.wurmunlimited.wurmmapgen;

import be.woubuc.wurmunlimited.wurmmapgen.database.DatabaseHandler;
import be.woubuc.wurmunlimited.wurmmapgen.filegen.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class WurmMapGen {
	
	public final static PropertiesManager properties = new PropertiesManager();
	public final static FileManager fileManager = new FileManager();
	public final static DatabaseHandler db = new DatabaseHandler();
	public final static TileMapGenerator tileMapGenerator = new TileMapGenerator();
	
	public static boolean debug = false;
	public static boolean verbose = false;
	
	public static String dataPath;
	public static String phpPath;
	
	public static void main(String[] args) throws Exception {
		// Log intro
		System.out.println("WurmMapGen v2.3.3");
		System.out.println("      Developed and maintained by woubuc");
		System.out.println("      Based on original code by garrett94");
		System.out.println("      More info: github.com/woubuc/WurmMapGen");
		
		// Get command line arguments
		Path propertiesFilePath = Paths.get("./WurmMapGen.properties").normalize();
		Path templateDirectoryPath = Paths.get("./template").normalize();
		
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "-h":
					case "--help":
						System.out.println();
						System.out.println("Documentation");
						System.out.println("      How to use: https://github.com/woubuc/WurmMapGen/wiki");
						System.out.println("      Configuration: https://github.com/woubuc/WurmMapGen/wiki/Configuration");
						System.exit(0);
						break;
						
					case "-c":
					case "--config":
						propertiesFilePath = Paths.get(args[++i]);
						break;
						
					case "-t":
					case "--template":
						templateDirectoryPath = Paths.get(args[++i]);
						break;
						
					case "-d":
					case "--debug":
						debug = true;
						
					case "-v":
					case "--verbose":
						verbose = true;
						break;
						
					default:
						System.out.println("Unknown argument '" + args[i] + "'");
						break;
				}
			}
		}
		
		System.out.println();
		System.out.println("      Properties file: " + propertiesFilePath.toAbsolutePath());
		System.out.println("      Template directory: " + templateDirectoryPath.toAbsolutePath());
		if (verbose) System.out.println("      Verbose logging enabled");
		if (debug) System.out.println("      Debug mode enabled");
		if (!verbose) System.out.println();
		
		// Time the duration of the map generation
		final long startTime = System.currentTimeMillis();
		
		// Load and assign properties
		if (!properties.load(propertiesFilePath)) return;
		dataPath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data").toString();
		phpPath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "includes").toString();
		
		fileManager.load();
		tileMapGenerator.openMap();
		
		if (!db.openDatabaseConnections()) return;
		
		tileMapGenerator.generateMapTiles();
		
		TemplateHandler templateHandler = new TemplateHandler(templateDirectoryPath);
		templateHandler.copyAssets();
		templateHandler.render();
		
		
		generateDataFiles();
		
		if (!db.closeDatabaseConnections()) return;
		
		tileMapGenerator.closeMap();
		fileManager.unload();
		
		Logger.info("");
		Logger.info("Map generated in " + (System.currentTimeMillis() - startTime) + " ms", false);
	}
	
	private static void generateDataFiles() throws IOException, SQLException {
		// data/config.json
		final ConfigFileGen configFileGen = new ConfigFileGen();
		configFileGen.generateFile();
		
		// includes/config.php
		final PhpConfigFileGen phpConfigFileGen = new PhpConfigFileGen();
		phpConfigFileGen.generateFile();
		
		// data/villages.json
		if (WurmMapGen.properties.showDeeds) {
			final VillageFileGen villageFileGen = new VillageFileGen();
			villageFileGen.generateFile();
		}
		
		// data/structures.json
		if (WurmMapGen.properties.showStructures) {
			final StructureFileGen structureFileGen = new StructureFileGen();
			structureFileGen.generateFile();
		}
		
		// data/guardtowers.json
		if (WurmMapGen.properties.showGuardTowers) {
			final GuardTowerFileGen guardTowerFileGen = new GuardTowerFileGen();
			guardTowerFileGen.generateFile();
		}
		
		// data/portals.json
		if (WurmMapGen.properties.showPortals) {
			PortalFileGen portalFileGen = new PortalFileGen();
			portalFileGen.generateFile();
		}
	}
}
