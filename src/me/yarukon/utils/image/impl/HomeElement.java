package me.yarukon.utils.image.impl;

import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class HomeElement extends Element {
	
	public String name;
	public int level;
	public int comfort_num;
	public String comfort_level_name;
	public int visit_num;
	public int item_num;

	public HomeElement(int x, int y, int width, int height, int space, String name, int level, int comfort_num, String comfort_level_name, int visit_num, int item_num) {
		super(x, y, width, height, space);
		this.name = name;
		this.level = level;
		this.comfort_num = comfort_num;
		this.comfort_level_name = comfort_level_name;
		this.visit_num = visit_num;
		this.item_num = item_num;
	}
	
	public void doDraw(Graphics2D g) {
		super.doDraw(g);
		
		if(ImageUtils.images.containsKey(name + ".png")) {
			g.drawImage(ImageUtils.images.get(name + ".png"), x, y, width, height, null);
		}
		
		g.setColor(new Color(0, 0, 0, 175));
		g.fillRect(x, y + (height / 2 - 40), width, 80);
		
		g.setFont(ImageUtils.font0);
		g.setColor(new Color(255, 255, 255, 255));
		Rectangle2D nameBound = ImageUtils.font0.getStringBounds(name, g.getFontRenderContext());
		g.drawString(name, (int) (x + (width / 2) - (nameBound.getWidth() / 2)), y + height / 2 + 5);
		
		g.setFont(ImageUtils.font3);
		String shit = "信任等阶: " + level + " 洞天仙力: " + comfort_num + " (" + comfort_level_name + ") 共访问人次: " + visit_num + " 共获得摆件: " + item_num;
		Rectangle2D infoBound = ImageUtils.font3.getStringBounds(shit, g.getFontRenderContext());
		g.drawString(shit, (int) (x + (width / 2) - (infoBound.getWidth() / 2)), y + height / 2 + 30);
	}

}
