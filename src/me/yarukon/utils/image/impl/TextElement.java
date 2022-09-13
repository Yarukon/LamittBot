package me.yarukon.utils.image.impl;

import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.*;

public class TextElement extends Element {
	private Font font;
	public String text;
	
	public TextElement(int x, int y, int width, int height, int space, String text, Font font) {
		super(x, y, width, height, space);
		this.text = text;
		this.font = font;
	}

	@Override
	public void doDraw(Graphics2D g) {
		super.doDraw(g);
		
		g.setColor(new Color(255, 255, 255, 175));
		g.setFont(font);
		g.drawString(text, x, y);
	}
	
}
