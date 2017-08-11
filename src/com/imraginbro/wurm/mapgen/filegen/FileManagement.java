package com.imraginbro.wurm.mapgen.filegen;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.imraginbro.wurm.mapgen.MapBuilder;

public class FileManagement {
	
	final static String separator = java.io.File.separator;
	
	public File map_topLayer = null;
	public File db_wurmZones = null;
	public File db_wurmItems = null;
	public File db_wurmPlayers = null;
	
	File[] fileBackupArray = null;
	
	public void load() {
		map_topLayer = new File(MapBuilder.propertiesManager.wurmMapLocation.getAbsolutePath() + separator + "top_layer.map");
		db_wurmZones = new File(MapBuilder.propertiesManager.wurmMapLocation.getAbsolutePath() + separator + "sqlite" + separator + "wurmzones.db");
		db_wurmItems = new File(MapBuilder.propertiesManager.wurmMapLocation.getAbsolutePath() + separator + "sqlite" + separator + "wurmitems.db");
		db_wurmPlayers = new File(MapBuilder.propertiesManager.wurmMapLocation.getAbsolutePath() + separator + "sqlite" + separator + "wurmplayers.db");
		fileBackupArray = new File[]{map_topLayer, db_wurmZones, db_wurmItems, db_wurmPlayers};
	}
	
	public void relocateFileVars() {
		map_topLayer = new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + map_topLayer.getName());
		db_wurmZones = new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + db_wurmZones.getName());
		db_wurmItems = new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + db_wurmItems.getName());
		db_wurmPlayers = new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + db_wurmPlayers.getName());
	}
	
	public void saveToFile(BufferedImage newImg, File file) throws IOException {
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
	
	public void copy(InputStream source , String destination) {
        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }
	
	public void extractRescources(String zipFileLocation) throws Exception {
		System.out.println("Copying "+zipFileLocation+" file from jar...");
		InputStream in = FileManagement.class.getResourceAsStream(zipFileLocation);
		copy(in, MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + "required.zip");	
		in.close();
		System.out.println("Extracting resources from "+zipFileLocation+"...");
		unzip(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + "required.zip", MapBuilder.propertiesManager.saveLocation.getAbsolutePath());
	}
	
	public void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		if (!MapBuilder.propertiesManager.replaceFiles && new File(filePath).exists()) {
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
	
	public void unzip(String zipFilePath, String destDirectory) throws IOException {
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
	public void copyFile(File sourceFile, File destFile) throws IOException {
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
	
	public void makeTempCopies() {
		new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp").mkdirs();
		for (int i = 0; i < fileBackupArray.length; i++) {
			final File old = fileBackupArray[i];
			if (!old.exists()) {
				continue;
			}
			System.out.println("Creating a temp copy of "+old.getName()+"...");
			try {
				copyFile(old, new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + old.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		relocateFileVars();
	}
	
	public void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
}
