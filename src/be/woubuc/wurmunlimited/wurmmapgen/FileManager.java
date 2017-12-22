package be.woubuc.wurmunlimited.wurmmapgen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class FileManager {
	
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
		System.out.println("\nCreate temp copies");
		new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp").mkdirs();
		for (int i = 0; i < fileBackupArray.length; i++) {
			final File old = fileBackupArray[i];
			if (!old.exists()) {
				continue;
			}
			System.out.println("      Creating a temp copy of "+old.getName()+"...");
			try {
				copyFile(old, new File(MapBuilder.propertiesManager.saveLocation.getAbsolutePath() + separator + "tmp" + separator + old.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		relocateFileVars();
		System.out.println("   OK Temp copies created");
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
