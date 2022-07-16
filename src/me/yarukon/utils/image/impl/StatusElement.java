package me.yarukon.utils.image.impl;

import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;


public class StatusElement extends Element {
	public String text;
	public String value;
	
	public StatusElement(int x, int y, int width, int height, int space, String text, String value) {
		super(x, y, width, height, space);
		this.text = text;
		this.value = value;
	}

	@Override
	public void doDraw(Graphics2D g) {
		super.doDraw(g);
		
		g.setColor(new Color(32, 34, 37));
		RoundRectangle2D roundedRectangle = new RoundRectangle2D.Double(x, y, width, height, 12, 12);
		g.fill(roundedRectangle);
		
		g.setColor(new Color(255, 255, 255, 175));
		g.setFont(ImageUtils.font1);
		Rectangle2D textBound = ImageUtils.font1.getStringBounds(text, g.getFontRenderContext());
		g.drawString(text, (int) (x + (width / 2) - (textBound.getWidth() / 2)), y + height - 18);
		
		Rectangle2D valueBound = ImageUtils.font1.getStringBounds(value, g.getFontRenderContext());
		g.drawString(value, (int) (x + (width / 2) - (valueBound.getWidth() / 2)), (int) (y + (height / 2) - (valueBound.getHeight() / 2)));
	}
}
