package me.yarukon.utils.image;

import me.yarukon.BotMain;
import net.mamoe.mirai.utils.MiraiLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageUtils {
	public static Font font1;
	public static Font font2;
	public static Font gamesym;

	public void init() {
		font1 = getFont("siyuan", 20, true, BotMain.INSTANCE.extResources.getAbsolutePath());
		font2 = getFont("siyuan", 16, true, BotMain.INSTANCE.extResources.getAbsolutePath());
		gamesym = getFont("gamesym", 20, BotMain.INSTANCE.extResources.getAbsolutePath());
	}

	public static Font getFont(String fontName, int size, String path) {
		return getFont(fontName, size, false, path);
	}

	public static Font getFont(String fontName, int size, boolean otf, String path) {
		Font font;
		try {
			InputStream is = Files.newInputStream(Paths.get(path + File.separator + fontName + (otf ? ".otf" : ".ttf")));
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			font = font.deriveFont(Font.PLAIN, size);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error loading font " + fontName);
			font = new Font("Microsoft Yahei UI", Font.PLAIN, size);
		}
		return font;
	}

	private static int[] getWidthAndHeight(ArrayList<Element> elements, int w, int h, boolean autoCalcHeight) {
		int height = autoCalcHeight ? 0 : h;

		if(autoCalcHeight) {
			int lastY = 0;
			for(Element ele : elements) {
				if((ele.y + ele.height + ele.space) != lastY) {
					height += (ele.y - lastY) + ele.height + ele.space;
					lastY = ele.y + ele.height + ele.space;
				}
			}
		}

		return new int[] {w, height};
	}

	public static void createImage(int w, int h, boolean autoCalcHeight, ArrayList<Element> elements, ByteArrayOutputStream out) throws Exception {
		createImage(w, h, autoCalcHeight, elements, out, "png");
	}

	public static void createImage(int w, int h, boolean autoCalcHeight, ArrayList<Element> elements, ByteArrayOutputStream out, String format) throws Exception {
		int[] arr = getWidthAndHeight(elements, w, h, autoCalcHeight);
		int width = arr[0];
		int height = arr[1];
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D g = image.createGraphics();

		g.setColor(new Color(47, 49, 54));
		g.fillRect(0, 0, width, height);

		//抗锯齿
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		for(Element ele : elements) {
			ele.doDraw(g);
		}

		g.dispose();
		ImageIO.write(image, format, out);
	}

	public static ByteArrayInputStream createImageToInputStream(int w, int h, boolean autoCalcHeight, ArrayList<Element> elements, String format) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		ImageUtils.createImage(w, h, autoCalcHeight, elements, stream, format);

		ByteArrayInputStream inStream = new ByteArrayInputStream(stream.toByteArray());
		stream.close();

		return inStream;
	}
}
