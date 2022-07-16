package me.yarukon.utils.image.impl;

import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.Color;
import java.awt.Graphics2D;

public class TextElement extends Element {

	public String text;
	
	public TextElement(int x, int y, int width, int height, int space, String text) {
		super(x, y, width, height, space);
		this.text = text;
	}

	@Override
	public void doDraw(Graphics2D g) {
		super.doDraw(g);
		
		g.setColor(new Color(255, 255, 255, 175));
		g.setFont(ImageUtils.font1);
		g.drawString(text, x, y);
	}
	
}
