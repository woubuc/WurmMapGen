package com.imraginbro.wurm.mapgen;

public class MapGen {
	
	public static void main(String[] args) throws Exception {
		System.out.println("WurmMapGen v2.0.0-indev");
		System.out.println("      by garrett92 and woubuc");
		System.out.println("      more info github.com/garrett92/WurmMapGen");
		
		// Allow for users to try -h or -help and just get the link to the repo, instead of running the mapgen
		if (args.length > 0) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		new MapBuilder();
		final long endTime = System.currentTimeMillis();
		final long totalTime = (endTime - startTime);
		
		System.out.println();
		System.out.println("Map generated");
		System.out.println("      duration " + totalTime + " ms");
	}
	
}
