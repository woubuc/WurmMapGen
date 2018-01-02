package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import org.apache.commons.io.FilenameUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public abstract class FileGen {
	
	private String filePath;
	void setFilePath(Path filePath) { this.filePath = filePath.toString(); }
	
	/**
	 * Generates the file
	 */
	public void generateFile() throws IOException {
		// Generate data
		String data = generateData();
		if (data == null) return;
		
		// Write data to file
		Logger.details("Writing " + filePath);
		FileWriter writer = new FileWriter(filePath, false);
		writer.write(data);
		writer.close();
		
		Logger.ok("Created " + FilenameUtils.getName(filePath));
	}

	protected abstract String generateData();
}
