package me.yarukon.utils.image;

import java.awt.Graphics2D;

public class Element {

	public int x;
	public int y;
	public int width;
	public int height;
	public int space;
	
	public Element(int x, int y, int width, int height, int space) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.space = space;
	}
	
	public void doDraw(Graphics2D g) {}
	
}
