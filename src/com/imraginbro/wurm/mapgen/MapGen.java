package com.imraginbro.wurm.mapgen;

public class MapGen {

	public static void main(String[] args) throws Exception {
		final long startTime = System.currentTimeMillis();
		new MapBuilder();
		final long endTime = System.currentTimeMillis();
		final long totalTime = (endTime - startTime)/1000;
		System.out.println("Finished with map generation! " + totalTime + " seconds.");
	}
	
}
