package me.yarukon.utils.image;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;
import me.yarukon.BotMain;
import me.yarukon.utils.GenshinQueryUtil;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.impl.TextElement;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.MiraiLogger;

public class ImageUtils {
	public static Font font0;
	public static Font font1;
	public static Font font2;
	public static Font font3;
	public static Font font4;
	public static HashMap<String, Image> images = new HashMap<>();

	public void init(MiraiLogger logger) {
		this.cacheImage("iconCache", "角色头像", logger);
		this.cacheImage("elementCache", "元素图标", logger);
		this.cacheImage("regionCache", "地图区域", logger);
		this.cacheImage("homeCache", "家园背景", logger, true);
		this.cacheImage("rarityCache", "稀有值", logger);

		font0 = getFont("DatFont", 45, BotMain.INSTANCE.extResources.getAbsolutePath());
		font1 = getFont("DatFont", 20, BotMain.INSTANCE.extResources.getAbsolutePath());
		font2 = getFont("DatFont", 18, BotMain.INSTANCE.extResources.getAbsolutePath());
		font3 = getFont("DatFont", 14, BotMain.INSTANCE.extResources.getAbsolutePath());
		font4 = getFont("Sarasa", 20, BotMain.INSTANCE.extResources.getAbsolutePath());
	}

	public void generateStatImage(long id, JsonObject obj) throws Exception {
		createImage(820, 0, true, GenshinQueryUtil.INSTANCE.statusAnalysis(id, obj), Paths.get(BotMain.INSTANCE.genshin_outputPath.getAbsolutePath(), id + ".png").toFile());
	}

	public static Font getFont(String fontName, int size, String path) {
		Font font;
		try {
			InputStream is = Files.newInputStream(Paths.get(path + File.separator + fontName + ".ttf"));
			font = Font.createFont(0, is);
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
			height += 5;

			int lastY = 0;
			int lastSpace = 0;
			for(Element ele : elements) {
				if(ele.y != lastY) {
					height += ((ele.y - ele.height - ele.space) - lastY) + ele.height + ele.space;
					lastY = ele.y;
					lastSpace = ele.space;
				}
			}

			height += lastSpace;
		}

		return new int[] {w, height};
	}

	public static void createImage(int w, int h, boolean autoCalcHeight, ArrayList<Element> elements, File outFile) throws Exception {
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
		ImageIO.write(image, "png", outFile);
	}

	public static void createImage(int w, int h, boolean autoCalcHeight, ArrayList<Element> elements, ByteArrayOutputStream out) throws Exception {
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
		ImageIO.write(image, "png", out);
	}

	public void cacheImage(String path, String type, MiraiLogger logger) {
		cacheImage(path, type, logger, false);
	}

	public void cacheImage(String path, String type, MiraiLogger logger, boolean makeRoundCorner) {
		File targetPath = new File(BotMain.INSTANCE.extResources.getAbsolutePath() + File.separator + path);
		for(File p : targetPath.listFiles()) {
			try {
				BufferedImage img = ImageIO.read(p);

				if(makeRoundCorner) {
					img = this.makeRoundedCorner(img, 22);
				}

				images.put(p.getName(), img);
				logger.info(type + (makeRoundCorner ? " (圆角图像)" : "") + " - " + p.getName() + " -> 内存");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void doDownloadAvatarIcon(String imgName) {
		doDownload(imgName, "iconCache", "https://upload-bbs.mihoyo.com/game_record/genshin/character_icon/UI_AvatarIcon_" + imgName);
	}

	public static void doDownloadRegionIcon(String imgName) {
		doDownload(imgName, "regionCache", "https://upload-bbs.mihoyo.com/game_record/genshin/city_icon/UI_ChapterIcon_" + imgName.replace("_Region_", ""));
	}

	public static void doDownload(String imgName, String targetFolder, String url) {
		BotMain.INSTANCE.getLogger().warning("检测到不存在的图像文件, 开始下载 " + imgName);
		File targetPath = new File(BotMain.INSTANCE.extResources.getAbsolutePath() + File.separator + targetFolder + File.separator + imgName);

		if(targetPath.exists()) {
			BotMain.INSTANCE.getLogger().info("文件 " + imgName + " 已存在! 跳过下载...");
			return;
		}

		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			 FileOutputStream fileOutputStream = new FileOutputStream(targetPath)) {
			byte[] dataBuffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}

			BotMain.INSTANCE.getLogger().info("下载 " + imgName + " 成功! 正在缓存至内存中...");
			try {
				Image img = ImageIO.read(targetPath);
				images.put(imgName, img);
				BotMain.INSTANCE.getLogger().info("图像 - " + imgName + " -> 内存");
			} catch (Exception ex) {
				BotMain.INSTANCE.getLogger().error("加载 " + imgName + " 至内存时发生错误!");
			}
		} catch (IOException e) {
			BotMain.INSTANCE.getLogger().error("下载 " + imgName + " 失败!");
		}
	}

	public BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = output.createGraphics();

		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, null);

		g2.dispose();
		return output;
	}
}
