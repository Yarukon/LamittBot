package me.yarukon.utils.image.impl;

import me.yarukon.ffxivQuests.FFXIVQuestManager;
import me.yarukon.ffxivQuests.FFXIVQuestResult;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


public class FFXIVQuestElement extends Element {

	public FFXIVQuestResult result;

	public FFXIVQuestElement(FFXIVQuestResult result) {
		super(0, 0, 350, 105, 0);
		this.result = result;
	}

	@Override
	public void doDraw(Graphics2D g) {
		g.setColor(new Color(255, 255, 255));
		g.drawRect(x, y, width - 1, height - 1);

		g.setFont(ImageUtils.font4);
		g.drawString(result.missionName, 65, 30);

		g.setFont(ImageUtils.font5);
		g.drawString(result.patchName + " [" + result.patchVersion + "]", 65, 52);
		
		g.drawImage(FFXIVQuestManager.INSTANCE.missionImage, 5, 5, 56, 60, null);
		
		g.drawString("剩余 " + (result.totalQuestSize - result.currentQuestIndex) + " 个任务", 5, 85);
		
		String percentage = FFXIVQuestManager.INSTANCE.df.format(result.percentage * 100) + "%";
		Rectangle2D textBound = ImageUtils.font5.getStringBounds(percentage, g.getFontRenderContext());
		g.drawString(percentage, width - 5 - (int) textBound.getWidth(), 85);
				
		g.setColor(new Color(222, 223, 214));
		g.fillRect(5, 90, (int) (result.percentage * (width - 10)), 8);

		g.setColor(new Color(255, 255, 255));
		g.drawRect(5, 90, width - 10, 8);

		super.doDraw(g);
	}

}
