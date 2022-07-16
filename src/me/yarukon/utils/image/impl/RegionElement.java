package me.yarukon.utils.image.impl;

import me.yarukon.utils.GenshinQueryUtil;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map.Entry;

public class RegionElement extends Element {

	public GenshinQueryUtil.Region region;
	
	public RegionElement(int x, int y, int width, int height, int space, GenshinQueryUtil.Region region) {
		super(x, y, width, height, space);
		this.region = region;
	}
	
	public void doDraw(Graphics2D g) {
		super.doDraw(g);

		g.setColor(new Color(57, 68, 103));
		RoundRectangle2D roundedRectangle = new RoundRectangle2D.Double(x, y, width, height, 12, 12);
		g.fill(roundedRectangle);

		g.setColor(new Color(255, 255, 255));
		g.drawImage(ImageUtils.images.get("_Region_FG.png"), x + 7, y + 5, width - (7 * 2), height - (5 * 2), null);
		
		if(ImageUtils.images.containsKey(region.iconPath)) {
			g.drawImage(ImageUtils.images.get(region.iconPath), x + 10, y + 10, 128, 128, null);
		} else {
			ImageUtils.doDownloadRegionIcon(region.iconPath);
			if(ImageUtils.images.containsKey(region.iconPath)) {
				g.drawImage(ImageUtils.images.get(region.iconPath), x, y, 128, 128, null);
			}
		}
		
		float startY = 55;
		g.setFont(ImageUtils.font3);
		g.drawString(region.place, x + 145, y + startY);
		
		startY += 20;
		g.drawString("探索进度 " + region.explorePercennt + "%", x + 145, y + startY);

		startY += 20;
		g.drawString((region.isCity ? "声望" : "") + "等级 " + region.Lvl, x + 145, y + startY);

		startY += 20;
		if(region.offerings != null) {
			for(Entry<String, Integer> off : region.offerings.entrySet()) {
				g.drawString(off.getKey() + "等级 " + off.getValue(), x + 145, y + startY);
			}
		}
	}

}
