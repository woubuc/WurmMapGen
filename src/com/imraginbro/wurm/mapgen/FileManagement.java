package com.imraginbro.wurm.mapgen;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class FileManagement {
	
	final static String separator = java.io.File.separator;
	
	public static void relocateFileVars() {
		MapGen.map_topLayer = new File(MapGen.saveLocation.getAbsolutePath() + separator + "tmp" + separator + MapGen.map_topLayer.getName());
		MapGen.db_wurmZones = new File(MapGen.saveLocation.getAbsolutePath() + separator + "tmp" + separator + MapGen.db_wurmZones.getName());
		MapGen.db_wurmItems = new File(MapGen.saveLocation.getAbsolutePath() + separator + "tmp" + separator + MapGen.db_wurmItems.getName());
		MapGen.db_wurmPlayers = new File(MapGen.saveLocation.getAbsolutePath() + separator + "tmp" + separator + MapGen.db_wurmPlayers.getName());
	}
	
	public static void saveToFile(BufferedImage newImg, File file) throws IOException {
		ImageWriter writer = null;
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
		if (iter.hasNext()) {
			writer = (ImageWriter)iter.next();
		}
		ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		writer.setOutput(ios);
		ImageWriteParam param = new JPEGImageWriteParam(java.util.Locale.getDefault());
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
		param.setCompressionQuality(1f);
		writer.write(null, new IIOImage( newImg, null, null ), param);
	}
	
	public static void copy(InputStream source , String destination) {
        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }
	
	public static void extractRescources(String zipFileLocation, File saveLocation) throws Exception {
		System.out.println("Copying "+zipFileLocation+" file from jar...");
		InputStream in = FileManagement.class.getResourceAsStream(zipFileLocation);
		copy(in, saveLocation.getAbsolutePath() + separator + "tmp" + separator + "required.zip");	
		in.close();
		System.out.println("Extracting resources from "+zipFileLocation+"...");
		unzip(saveLocation.getAbsolutePath() + separator + "tmp" + separator + "required.zip", saveLocation.getAbsolutePath());
	}
	
	public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		if (!MapGen.replaceFiles && new File(filePath).exists()) {
			return;
		}
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
	
	public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = destDirectory + separator + entry.getName();
            File checkDir = new File(destDirectory + separator + entry.getName()).getParentFile();
            if (!checkDir.exists()) {
            	checkDir.mkdir();
            }
            new File(destDirectory).mkdirs();
            if (!entry.isDirectory()) {
                extractFile(zipIn, filePath);
            } else {
                new File(filePath).mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
	
	@SuppressWarnings("resource")
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }
	    FileChannel source = null;
	    FileChannel destination = null;
	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	public static void makeTempCopies(File[] files, File saveLocation) throws IOException {
		new File(saveLocation.getAbsolutePath() + separator + "tmp").mkdirs();
		for (int i = 0; i < files.length; i++) {
			File old = files[i];
			System.out.println("Creating a temp copy of "+old.getName()+"...");
			copyFile(old, new File(saveLocation.getAbsolutePath() + separator + "tmp" + separator + old.getName()));
		}
	}
	
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	public static void copyPropertiesFile() {
		try {
			InputStream in = FileManagement.class.getResourceAsStream("/resources/WurmMapGen.properties");
			copy(in, "WurmMapGen.properties");
			in.close();
		} catch(Exception e) {
			System.out.println("Error copying properties file from jar - " + e.getMessage());
		}
	}
	
	public static boolean loadPropValues() {
		System.out.println("Loading WurmMapGen.properties file!");
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("WurmMapGen.properties");
		} catch (Exception e) {
			System.out.println("[ERROR] problem loading properties FileInputStream - " + e.getMessage());
			System.out.println("Copying properties file from jar... please configure and restart program.");
			copyPropertiesFile();
			return false;
		}
		if (input != null) {
			try {
				prop.load(input);
			} catch (Exception e) {
				System.out.println("Error loading properties file - " + e.getMessage());
			}
		}
		
		String maploc = prop.getProperty("wurmMapLocation", "C:/location/to/map/folder");
		String saveloc = prop.getProperty("saveLocation", "C:/location/to/save/folder");
		
		System.out.println("[INFO] Map location: " + maploc);
		System.out.println("[INFO] Save location: " + saveloc);
		
		if (maploc.equals("C:/location/to/map/folder") || saveloc.equals("C:/location/to/save/folder")) {
			System.out.println("[ERROR] Looks like you are using the default map or save location. Please change in your config file.");
			return false;
		}
		
		MapGen.showDeeds = Boolean.parseBoolean(prop.getProperty("showDeeds", Boolean.toString(MapGen.showDeeds)));
		MapGen.showGuardTowers = Boolean.parseBoolean(prop.getProperty("showGuardTowers", Boolean.toString(MapGen.showGuardTowers)));
		MapGen.showStructures = Boolean.parseBoolean(prop.getProperty("showStructures", Boolean.toString(MapGen.showStructures)));
		
		MapGen.gen_map_shading = Boolean.parseBoolean(prop.getProperty("mapGenerateShading", Boolean.toString(MapGen.gen_map_shading)));
		MapGen.gen_map_shade_paths = Boolean.parseBoolean(prop.getProperty("mapShadePaths", Boolean.toString(MapGen.gen_map_shade_paths)));
		MapGen.gen_map_water = Boolean.parseBoolean(prop.getProperty("mapGenerateWater", Boolean.toString(MapGen.gen_map_water)));
		MapGen.gen_map_bridges = Boolean.parseBoolean(prop.getProperty("mapGenerateBridges", Boolean.toString(MapGen.gen_map_bridges)));
		
		MapGen.wurmMapLocation = new File(maploc);
		MapGen.saveLocation = new File(saveloc);
		
		MapGen.replaceFiles = Boolean.parseBoolean(prop.getProperty("replaceFiles", Boolean.toString(MapGen.replaceFiles)));
		

		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
}
