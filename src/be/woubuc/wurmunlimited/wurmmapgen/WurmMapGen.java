package be.woubuc.wurmunlimited.wurmmapgen;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WurmMapGen {
	
	public static void main(String[] args) throws Exception {
		System.out.println("WurmMapGen");
		System.out.println("      developed and maintained by woubuc");
		System.out.println("      based on original code by garrett94");
		System.out.println("      more info: github.com/woubuc/WurmMapGen");
		
		// Get path to the properties file
		Path propertiesFilePath = Paths.get("./WurmMapGen.properties").normalize();
		
		// Custom properties file path: -c filename
		if (args.length >= 2) {
			if (args[0].equals("-c")) {
				propertiesFilePath = Paths.get(args[1]);
			}
		}
		
		System.out.println("\nUsing properties file: " + propertiesFilePath.toAbsolutePath());
		System.out.println("      To use a different file: -c yourfile.properties");
		
		// Allow for users to try something like -h or -help and just get the above info without running the mapgen
		if (args.length != 0 && args.length != 2) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		MapBuilder mapBuilder = new MapBuilder(propertiesFilePath);
		mapBuilder.buildMap();
		
		final long endTime = System.currentTimeMillis();
		final long totalTime = (endTime - startTime);
		
		System.out.println();
		System.out.println("Map generated");
		System.out.println("      duration " + totalTime + " ms");
	}
	
}
