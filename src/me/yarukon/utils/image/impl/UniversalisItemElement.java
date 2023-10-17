package me.yarukon.utils.image.impl;

import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class UniversalisItemElement extends Element {

	private final boolean isHQ;
	private final String price;
	private final String total;
	private final String retainer;
	private final String retainerWorld;
	
	public UniversalisItemElement(int x, int y, int width, int height, int space, boolean isHQ, String price, String total, String retainer, String retainerWorld) {
		super(x, y, width, height, space);
		this.isHQ = isHQ;
		this.price = price;
		this.total = total;
		this.retainer = retainer;
		this.retainerWorld = retainerWorld;
	}

	@Override
	public void doDraw(Graphics2D g) {
		super.doDraw(g);
		
		g.setColor(new Color(88, 90, 94));
		g.drawRoundRect(x, y, width, height, 5, 5);
		
		g.setColor(new Color(255, 255, 255, 175));
		g.setFont(ImageUtils.gamesym);
		g.drawString(isHQ ? "\ue03c" : "", x + 7, y + 23);
		
		// Price (LOL)
		g.setFont(ImageUtils.font1);
		Rectangle2D priceBound = ImageUtils.font1.getStringBounds(price, g.getFontRenderContext());
		g.drawString(price, x + 70, y + 23);
		
		g.setFont(ImageUtils.gamesym);
		g.drawString("\ue049", x + 70 + (int) priceBound.getWidth(), y + 23);
		
		g.setFont(ImageUtils.font1);
		String totalStr = " (共" + total;
		Rectangle2D totalBound = ImageUtils.font1.getStringBounds(totalStr, g.getFontRenderContext());
		
		g.drawString(totalStr, x + 80 + (int) priceBound.getWidth(), y + 23);
		g.setFont(ImageUtils.gamesym);
		g.drawString("\ue049", x + 80 + (int) priceBound.getWidth() + (int) totalBound.getWidth(), y + 23);
		
		g.setFont(ImageUtils.font1);
		g.drawString(")", x + 91 + (int) priceBound.getWidth() + (int) totalBound.getWidth(), y + 23);

		// Retainer name
		
		g.drawString(retainer, x + 470, y + 23);
		if (retainerWorld != null) {
			Rectangle2D retainerNameBound = ImageUtils.font1.getStringBounds(retainer, g.getFontRenderContext());
			g.setFont(ImageUtils.gamesym);
			g.drawString("\ue05d", x + 472 + (int) retainerNameBound.getWidth(), y + 23);
			
			g.setFont(ImageUtils.font1);
			g.drawString(retainerWorld, x + 473 + (int) retainerNameBound.getWidth() + 20, y + 23);
		}
	}
	
	

}
