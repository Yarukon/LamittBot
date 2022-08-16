package me.yarukon.utils;

import me.yarukon.BotMain;
import me.yarukon.UniversalisJson;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;
import me.yarukon.utils.image.impl.LineElement;
import me.yarukon.utils.image.impl.LineHeadElement;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class FFXIVUtil {
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static float flagToPixel(float scale, float flag, int resolution) {
        return (flag - 1f) * 50f * scale * (float) resolution / 2048;
    }

    public static boolean containsItem(String itemName, ArrayList<String> nameLikeItems) {
        boolean wannaSearch = itemName.startsWith("?");
        String newItemName = null;

        if (wannaSearch) {
            newItemName = itemName.replace("?", "");
        }

        for (Map.Entry<String, Integer> entry : BotMain.INSTANCE.itemIDs.entrySet()) {
            if (entry.getKey().equals(itemName)) {
                nameLikeItems.clear();
                return true;
            } else if (entry.getKey().contains(itemName) || (wannaSearch && entry.getKey().contains(newItemName))) {
                nameLikeItems.add(entry.getKey());
            }
        }

        return false;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static ByteArrayOutputStream genImage(String itemName, boolean isHQ, String zoneOrWorld, UniversalisJson jsonIn) {
        ByteArrayOutputStream stream = null;
        try {
            LineElement head = new LineElement(5, 25, 800, 30, 5, "物品名称: " + itemName + (isHQ ? " (HQ)" : "") + " 大区/服务器: " + zoneOrWorld);
            LineElement head2 = new LineElement(5, 50, 800, 30, 5, "最后更新: " + sdf.format(new Timestamp(jsonIn.lastUploadTime)));
            LineHeadElement hqText = new LineHeadElement(30, 75, 30, 20, 5, "高品质");
            LineHeadElement itemCountAndPrice = new LineHeadElement(140, 75, 30, 20, 5, "数量/价格");
            LineHeadElement retainer = new LineHeadElement(500, 75, 30, 20, 5, "雇员名称");
            ArrayList<Element> elements = new ArrayList<>();
            elements.add(head);
            elements.add(head2);
            elements.add(hqText);
            elements.add(itemCountAndPrice);
            elements.add(retainer);

            int startY = 100;
            for(UniversalisJson.UniversalisListingJson jj : jsonIn.listings) {
                elements.add(new LineHeadElement(73, startY, 30, 20, 5, jj.hq ? "Y" : "N"));
                elements.add(new LineHeadElement(140, startY, 30, 20, 5, jj.quantity + "x " + jj.pricePerUnit + " (共" + jj.total + ")"));
                elements.add(new LineHeadElement(500, startY, 30, 20, 5, jj.retainerName + (jj.worldName != null ? " @ " + jj.worldName : "")));
                startY += 25;
            }

            stream = new ByteArrayOutputStream();
            ImageUtils.createImage(800, 0, true, elements, stream);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return stream;
    }
}
