package me.yarukon.value;

import me.yarukon.value.impl.*;

import java.util.ArrayList;

public class Values {
    public long groupID;
    public ArrayList<ValueBase> valuesList = new ArrayList<>();

    //起源服务器信息查询 (一般用于GMOD)
    public BooleanValue enableSSI = new BooleanValue("SSIEnabled", false, valuesList);
    public BooleanValue enableSSIPlyList = new BooleanValue("SSIEnablePlayerList", false, valuesList);

    public MultiMapValue SSIServerList = new MultiMapValue("SSIServerList", valuesList);

    //原神信息查询
    public BooleanValue genshinInfoQuery = new BooleanValue("GenshinInfoQuery", false, valuesList);

    //MC服务器信息查询
    public BooleanValue minecraftStatQuery = new BooleanValue("MinecraftStatQuery", false, valuesList);
    public MultiMapValue minecraftServerIP = new MultiMapValue("MinecraftServerIP", valuesList);

    //青年大学习答案解析
    public BooleanValue daXueXi = new BooleanValue("DaXueXi", false, valuesList);

    // 唢呐
    public MultiBooleanValue regions = new MultiBooleanValue("SonarRegion", valuesList, new MultiBoolean("陆行鸟", false), new MultiBoolean("莫古力", false), new MultiBoolean("猫小胖", false), new MultiBoolean("豆豆柴", false));
    public MultiBooleanValue ranks = new MultiBooleanValue("SonarRanks", valuesList, new MultiBoolean("SS", false), new MultiBoolean("S", false), new MultiBoolean("A", false), new MultiBoolean("B", false));
    public MultiStringValue fateFilter = new MultiStringValue("FateFilter", valuesList);

    // 物价查询
    public BooleanValue priceCheck = new BooleanValue("PriceCheck", false, valuesList);

    // 自动回复
    public BooleanValue autoReply = new BooleanValue("AutoReply", false, valuesList);

    public BooleanValue ffxivQuest = new BooleanValue("FFXIVQuest", false, valuesList);

    public Values(long groupID) {
        this.groupID = groupID;
    }

}
