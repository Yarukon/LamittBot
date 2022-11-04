package me.yarukon.thread.impl;

import me.yarukon.BotMain;
import me.yarukon.utils.json.UniversalisJson;
import me.yarukon.value.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.BotUtils;
import me.yarukon.utils.FFXIVUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PriceCheckThread extends ProcessThread {
    public String itemName;
    public String zoneOrWorld;

    public int listingAmount;

    public static final ArrayList<String> nameLikeItems = new ArrayList<>();

    public PriceCheckThread(Group api, long qq, Values value, String itemName, String zoneOrWorld, int listingAmount) {
        super(api, qq, value);
        this.itemName = itemName;
        this.zoneOrWorld = zoneOrWorld;
        this.listingAmount = listingAmount;
    }

    @Override
    public void action() throws Exception {
        boolean isHQ = false;
        boolean onlyNQ = false;
        String dataCenterName = BotMain.INSTANCE.getDataCenterNameFromFriendlyName(zoneOrWorld);
        if (dataCenterName != null || BotMain.INSTANCE.isDataCenterExist(zoneOrWorld) || BotMain.INSTANCE.isZoneExist(zoneOrWorld)) {
            if (dataCenterName != null) {
                this.zoneOrWorld = dataCenterName;
            }

            if (itemName.contains("NQ")) {
                onlyNQ = true;
                itemName = itemName.replace("NQ", "");
            }

            if (itemName.contains("HQ") || itemName.contains("高品质")) {
                isHQ = true;
                itemName = itemName.replace("HQ", "");
                itemName = itemName.replace("高品质", "");
            }

            if (FFXIVUtil.containsItem(itemName, nameLikeItems)) {
                int itemID = BotMain.INSTANCE.itemIDs.get(itemName);
                String result = BotUtils.sendGet("https://universalis.app/api/v2/" + URLEncoder.encode(zoneOrWorld, "UTF-8") + "/" + itemID, "listings=" + listingAmount + (isHQ ? "&hq=true" : onlyNQ ? "&hq=false" : "") + "&fields=" + FFXIVUtil.requestFields);
                UniversalisJson json = BotUtils.gson.fromJson(result, UniversalisJson.class);
                if (json.itemID != 0) {
                    ByteArrayOutputStream stream = FFXIVUtil.genImage(itemName, isHQ, onlyNQ, zoneOrWorld, json);
                    if (stream != null) {
                        ByteArrayInputStream inStream = new ByteArrayInputStream(stream.toByteArray());
                        ExternalResource extRes = ExternalResource.create(inStream);
                        Image img = api.uploadImage(extRes);
                        api.sendMessage(new At(api.getOrFail(qq).getId()).plus("\n").plus(img));

                        extRes.close();
                        inStream.close();
                        stream.close();
                    } else {
                        api.sendMessage("图片生成失败!");
                    }
                } else {
                    api.sendMessage("物品 " + itemName + " 不支持在市场上售卖!");
                }
            } else {
                ArrayList<String> subArray = new ArrayList<>(nameLikeItems.size() > 10 ? nameLikeItems.subList(0, 10) : nameLikeItems);
                api.sendMessage("物品 " + itemName + " 不存在! " + (subArray.size() > 0 ? nameLikeItems.size() <= 10 ? "相似名称的物品有 " + String.join(", ", subArray) : "相似名称的物品有 " + String.join(", ", subArray) + "等" + nameLikeItems.size() + "个结果" : "且不存在相似名称的物品!"));
                nameLikeItems.clear();
            }
        } else {
            api.sendMessage("大区/服务器 " + zoneOrWorld + " 不存在!");
        }
        super.action();
    }
}
