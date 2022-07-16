package me.yarukon.utils.image.impl;

import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class LineElement extends Element {

	public String text;
	
	public LineElement(int x, int y, int width, int height, int space, String text) {
		super(x, y, width, height, space);
		this.text = text;
	}

	@Override
	public void doDraw(Graphics2D g) {
		super.doDraw(g);
		
		g.setColor(new Color(255, 255, 255, 175));
		g.setFont(ImageUtils.font4);
		Rectangle2D textBound = ImageUtils.font1.getStringBounds(text, g.getFontRenderContext());
		g.drawString(text, (int) (x + (width / 2) - (textBound.getWidth() / 2)), y);
	}

}
