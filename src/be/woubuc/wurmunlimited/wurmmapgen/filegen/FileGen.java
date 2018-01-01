package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import org.apache.commons.io.FilenameUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public abstract class FileGen {
	
	private String fileName;
	void setFileName(String fileName) { this.fileName = fileName; }
	
	/**
	 * Generates the file
	 * @param  basePath  The path where the datafile should be generated
	 */
	public void generateFile(String basePath) throws IOException {
		String filePath = Paths.get(basePath, fileName).normalize().toAbsolutePath().toString();
		String data = generateData();
		
		if (data == null) {
			Logger.error("Could not generate data");
			return;
		}
		
		// Write data to file
		Logger.details("Creating " + filePath);
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(data);
		writer.close();
		
		Logger.ok("Created " + FilenameUtils.getName(filePath));
	}

	protected abstract String generateData();
}
