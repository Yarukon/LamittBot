package me.yarukon.utils.image.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import me.yarukon.utils.GenshinQueryUtil;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

public class CharacterElement extends Element {
	
	public GenshinQueryUtil.Character character;

	public CharacterElement(int x, int y, int width, int height, int space, GenshinQueryUtil.Character character) {
		super(x, y, width, height, space);
		this.character = character;
	}
	
	public void doDraw(Graphics2D g) {
		super.doDraw(g);

		g.drawImage(ImageUtils.images.get("_Back" + character.rarity + ".png"), x, y, width, 128, null);

		RoundRectangle2D roundClip = new RoundRectangle2D.Double(x, y, width, 128, 12, 12);
		g.setClip(roundClip);
		if(ImageUtils.images.containsKey(character.iconPath)) {
			g.drawImage(ImageUtils.images.get(character.iconPath), x, y, width, 128, null);
		} else {
			ImageUtils.doDownloadAvatarIcon(character.iconPath);
			if(ImageUtils.images.containsKey(character.iconPath)) {
				g.drawImage(ImageUtils.images.get(character.iconPath), x, y, width, 128, null);
			}
		}
		g.setClip(null);
		
		g.drawImage(ImageUtils.images.get("_Front.png"), x, y + 13, width, 136, null);

		if(ImageUtils.images.containsKey(character.ele + ".png")) {
			g.drawImage(ImageUtils.images.get(character.ele + ".png"), x + 2, y + 2, 32, 32, null);
		}

		if(ImageUtils.images.containsKey("_" + character.rarity + "Stars.png")) {
			g.drawImage(ImageUtils.images.get("_" + character.rarity + "Stars.png"), x + (width / 2) - 48, y + 100, 96, 25, null);
		}

		g.setColor(character.activeConstellation == 6 ? new Color(255, 115, 0, 145) : new Color(0, 0, 0, 145));
		RoundRectangle2D roundedRectangle = new RoundRectangle2D.Double(x + width - 22, y + 2, 20, 32, 8, 12);
		g.fill(roundedRectangle);
		
		g.setFont(ImageUtils.font2);
		g.setColor(new Color(255, 255, 255, 255));
		Rectangle2D constellationBound = ImageUtils.font2.getStringBounds(character.activeConstellation + "", g.getFontRenderContext());
		g.drawString(character.activeConstellation + "", (int) (x + width - (22 / 2) - (constellationBound.getWidth() / 2)) - 1, y + 24);

		g.setFont(ImageUtils.font3);
		g.setColor(new Color(76, 86, 104));
		String str = character.name.equals("旅行者") ? "Lv. " + character.lvl : "Lv. " + character.lvl + "  " + character.fetter + "好感";
		Rectangle2D levelBound = ImageUtils.font3.getStringBounds(str, g.getFontRenderContext());
		g.drawString(str, (int) (x + (width / 2) - (levelBound.getWidth() / 2)), y + 142);
		
		g.setFont(ImageUtils.font1);
		g.setColor(new Color(255, 255, 255, 255));
		Rectangle2D nameBound = ImageUtils.font1.getStringBounds(character.name, g.getFontRenderContext());
		g.drawString(character.name, (int) (x + (width / 2) - (nameBound.getWidth() / 2)), y + 173);
	}

}
