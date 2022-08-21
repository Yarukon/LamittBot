package me.yarukon.thread.impl;

import me.yarukon.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.source.ServerPlayer;
import me.yarukon.utils.source.SteamServerInfo;
import me.yarukon.utils.source.SteamServerQuery;
import me.yarukon.value.Keypair;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;

public class ValveServerQueryThread extends ProcessThread {

    public Keypair msValue;

    public ValveServerQueryThread(Group api, long qq, Values value, Keypair msValue) {
        super(api, qq, value);
        this.msValue = msValue;
    }

    @Override
    public void action() throws Exception {
        SteamServerQuery query = new SteamServerQuery(msValue.getValue().contains(":") ? msValue.getValue() : msValue.getValue() + ":27015");
        SteamServerInfo result = query.getInfo();

        String name = "服务器离线/连接超时/返回无效";
        String currentMap = "未知";
        String currentPly = "未知";
        String currentVer = "未知";
        String serverEnv = "未知";
        String latency = "0ms";
        StringBuilder plys = new StringBuilder();

        if (result != null) {
            name = result.getName();
            currentMap = result.getMap();
            currentVer = result.getVersion();
            serverEnv = result.getServerEnvironment();
            currentPly = result.getPlayers() + "/" + result.getMaxPlayers();
            latency = result.getLatency() + "ms";
        } else if (query.getPlayer() != null) {
            name = "服务器返回的A2S数据无效!";
            currentPly = query.getPlayer().getPlayers().length + "/未知";
        }

        if (value.enableSSIPlyList.getValue()) {
            if (query.getPlayer() != null) {
                for (ServerPlayer player : query.getPlayer().getPlayers()) {
                    plys.append(player.getName()).append(" 分数: ").append(player.getScore()).append("\n");
                }
            } else {
                plys.append("服务器离线/无玩家/玩家过多");
            }
        }

        api.sendMessage(new At(api.getOrFail(qq).getId()).plus(
                "[SSI] 服务器信息\n" +
                        name + "\n" +
                        "版本: " + currentVer + "\n" +
                        "地图: " + currentMap + "\n" +
                        "系统类型: " + serverEnv + "\n" +
                        "在线玩家: " + currentPly + "\n" +
                        "延时: " + latency + "\n" +
                        (value.enableSSIPlyList.getValue() ? "\n——————————————\n" + (plys.toString().endsWith("\n") ? plys.substring(0, plys.length() - 1) : plys) : "")));
    }
}