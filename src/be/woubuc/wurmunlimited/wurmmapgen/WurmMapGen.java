package be.woubuc.wurmunlimited.wurmmapgen;

import be.woubuc.wurmunlimited.wurmmapgen.filegen.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WurmMapGen {
	
	public final static PropertiesManager properties = new PropertiesManager();
	public final static FileManager fileManager = new FileManager();
	public final static DatabaseHandler db = new DatabaseHandler();
	public final static TileMapGenerator tileMapGenerator = new TileMapGenerator();
	
	public static boolean debug = false;
	public static boolean verbose = false;
	
	public static void main(String[] args) throws Exception {
		// Log intro
		System.out.println("WurmMapGen");
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
		
		
		if (!properties.load(propertiesFilePath)) return;
		
		fileManager.load();
		tileMapGenerator.openMap();
		
		if (!db.openDatabaseConnections()) return;
		
		tileMapGenerator.generateMapTiles();
		
		TemplateHandler templateHandler = new TemplateHandler(templateDirectoryPath);
		templateHandler.copyAssets();
		templateHandler.render();
		
		final VillageFileGen villageFileGen = new VillageFileGen();
		final StructureFileGen structureFileGen = new StructureFileGen();
		final GuardTowerFileGen guardTowerFileGen = new GuardTowerFileGen();
		
		String dataPath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data").toString();
		String phpPath = Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "includes").toString();
		
		ConfigFileGen configFileGen = new ConfigFileGen();
		configFileGen.generateFile(dataPath);
		
		PhpConfigFileGen phpConfigFileGen = new PhpConfigFileGen();
		phpConfigFileGen.generateFile(phpPath);
		
		if (WurmMapGen.properties.showDeeds) {
			villageFileGen.generateVillageFile();
		}
		
		if (WurmMapGen.properties.showStructures) {
			structureFileGen.generateStructureFile(Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data", "structures.json").toString());
		}
		
		if (WurmMapGen.properties.showGuardTowers) {
			guardTowerFileGen.generateGuardTowerFile(Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data", "guardtowers.json").toString());
		}
		
		PortalFileGen portalFileGen = new PortalFileGen();
		portalFileGen.generateFile(dataPath);
		
		
		if (!db.closeDatabaseConnections()) return;
		
		tileMapGenerator.closeMap();
		fileManager.unload();
		
		Logger.info("");
		Logger.info("Map generated in " + (System.currentTimeMillis() - startTime) + " ms", false);
	}
}
