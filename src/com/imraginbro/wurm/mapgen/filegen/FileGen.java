package com.imraginbro.wurm.mapgen.filegen;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class FileGen {
	
	private final static VillageFileGen villageFileGen = new VillageFileGen();
	private final static StructureFileGen structureFileGen = new StructureFileGen();
	private final static GuardTowerFileGen guardTowerFileGen = new GuardTowerFileGen();
	
	/**
	 * Generates the required datafiles for the map
	 */
	public void generateFiles() throws IOException, SQLException {
		ConfigFileGen.generateConfigFile(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "config.json").toString());
		
		if (MapBuilder.propertiesManager.showDeeds) {
			villageFileGen.generateVillageFile(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "deeds.json").toString());
		}
		
		if (MapBuilder.propertiesManager.showStructures) {
			structureFileGen.generateStructureFile(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "structures.json").toString());
		}
		
		if (MapBuilder.propertiesManager.showGuardTowers) {
			guardTowerFileGen.generateGuardTowerFile(Paths.get(MapBuilder.propertiesManager.saveLocation.getAbsolutePath(), "data", "guardtowers.json").toString());
		}
	}
}
