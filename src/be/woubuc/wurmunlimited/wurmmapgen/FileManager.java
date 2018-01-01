package be.woubuc.wurmunlimited.wurmmapgen;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
	
	public WurmFile map_topLayer;
	public DatabaseFile db_wurmZones;
	public DatabaseFile db_wurmItems;
	public DatabaseFile db_wurmPlayers;
	
	public DatabaseFile db_modSupport = null;
	
	private Path tempDir;
	
	/**
	 * Initialises a new FileManager instance
	 */
	FileManager() {
		// Get temp directory path
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"),"WurmMapGen", Long.toString(System.currentTimeMillis())).toAbsolutePath().normalize();
	}
	
	/**
	 * Loads all required files and makes copies in a temp directory
	 */
	public void load() throws IOException {
		Logger.title("Create temp file copies");
		
		Files.createDirectories(tempDir);
		Logger.details("Created directory " + tempDir.toString());
		
		Logger.details("Loading required files");
		map_topLayer = new WurmFile(Paths.get(WurmMapGen.properties.wurmMapLocation.getAbsolutePath(), "top_layer.map"));
		
		db_wurmZones = new DatabaseFile(Paths.get(WurmMapGen.properties.wurmMapLocation.getAbsolutePath(), "sqlite", "wurmzones.db"));
		db_wurmItems = new DatabaseFile(Paths.get(WurmMapGen.properties.wurmMapLocation.getAbsolutePath(), "sqlite", "wurmitems.db"));
		db_wurmPlayers = new DatabaseFile(Paths.get(WurmMapGen.properties.wurmMapLocation.getAbsolutePath(), "sqlite", "wurmplayers.db"));
		
		Path modSupportPath = Paths.get(WurmMapGen.properties.wurmMapLocation.getAbsolutePath(), "sqlite", "modsupport.db");
		if (modSupportPath.toFile().exists()) {
			db_modSupport = new DatabaseFile(modSupportPath);
		}
		
		Logger.details("Copying files to temp directory");
		map_topLayer = map_topLayer.copyToDirectory(tempDir);
		db_wurmZones = db_wurmZones.copyToDirectory(tempDir);
		db_wurmItems = db_wurmItems.copyToDirectory(tempDir);
		db_wurmPlayers = db_wurmPlayers.copyToDirectory(tempDir);
		
		if (db_modSupport != null) {
			db_modSupport = db_modSupport.copyToDirectory(tempDir);
		}
		
		Logger.ok("Temp files copied");
	}
	
	/**
	 * Unloads the files and deletes the previously created temporary files
	 */
	public void unload() throws IOException {
		Logger.title("Remove temp file copies");
		
		FileUtils.deleteDirectory(tempDir.toFile());
		Logger.ok("Directory deleted");
	}
	
	/**
	 * Describes a Wurm Unlimited SQLite db file
	 */
	class DatabaseFile extends File {
		
		/**
		 * Initialises the file
		 * @param  filePath  Path to the file
		 */
		DatabaseFile(Path filePath) {
			super(filePath.toAbsolutePath().normalize().toString());
		}
		
		/**
		 * Copies the db file and all associated files to the given directory
		 * @param  destination  The destination directory
		 */
		DatabaseFile copyToDirectory(Path destination) throws IOException {
			Logger.details("-> " + this.getName());
			
			DatabaseFile dest = new DatabaseFile(Paths.get(destination.toString(), this.getName()));
			FileUtils.copyFile((File) this, dest);
			
			File wal = new File(this.getAbsolutePath() + "-wal");
			if (wal.exists()) {
				Logger.details("-> " + wal.getName());
				FileUtils.copyFileToDirectory(wal, destination.toFile());
			}
			
			File shm = new File(this.getAbsolutePath() + "-shm");
			if (shm.exists()) {
				Logger.details("-> " + shm.getName());
				FileUtils.copyFileToDirectory(shm, destination.toFile());
			}
			
			return dest;
		}
	}
	
	/**
	 * Describes a Wurm Unlimited file
	 */
	class WurmFile extends File {
		
		/**
		 * Initialises the file
		 * @param  filePath  Path to the file
		 */
		WurmFile(Path filePath) {
			super(filePath.toAbsolutePath().normalize().toString());
		}
		
		/**
		 * Copies the map file to the given directory
		 * @param  destination  The destination directory
		 */
		WurmFile copyToDirectory(Path destination) throws IOException {
			Logger.details("-> " + this.getName());
			
			WurmFile dest = new WurmFile(Paths.get(destination.toString(), this.getName()));
			FileUtils.copyFile((File) this, dest);
			
			return dest;
		}
	}
}
