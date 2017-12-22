package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

public class FileGen {
	
	private final static VillageFileGen villageFileGen = new VillageFileGen();
	private final static StructureFileGen structureFileGen = new StructureFileGen();
	private final static GuardTowerFileGen guardTowerFileGen = new GuardTowerFileGen();
	
	/**
	 * Generates the required datafiles for the map
	 */
	public void generateDataFiles() throws IOException, SQLException {
		ConfigFileGen.generateConfigFile();
		ConfigFileGen.generatePhpConfigFile();
		
		if (WurmMapGen.properties.showDeeds) {
			villageFileGen.generateVillageFile();
		}
		
		if (WurmMapGen.properties.showStructures) {
			structureFileGen.generateStructureFile(Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data", "structures.json").toString());
		}
		
		if (WurmMapGen.properties.showGuardTowers) {
			guardTowerFileGen.generateGuardTowerFile(Paths.get(WurmMapGen.properties.saveLocation.getAbsolutePath(), "data", "guardtowers.json").toString());
		}
	}
}
