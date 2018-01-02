package be.woubuc.wurmunlimited.wurmmapgen.database;

public final class Portal {
	
	private String name;
	private int posX;
	private int posY;
	
	public String getName() { return name; }
	public int getPosX() { return posX; }
	public int getPosY() { return posY; }
	
	Portal(String name, int posX, int posY) {
		this.name = name;
		this.posX = (int) Math.floor(posX / 4);
		this.posY = (int) Math.floor(posY / 4);
	}
}
