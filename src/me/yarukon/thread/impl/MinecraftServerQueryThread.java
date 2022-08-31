package me.yarukon.thread.impl;

import me.yarukon.value.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.MineStat;
import me.yarukon.value.impl.Keypair;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;

public class MinecraftServerQueryThread extends ProcessThread {

    public Keypair stringValue;

    public MinecraftServerQueryThread(Group api, long qq, Values value, Keypair keypair) {
        super(api, qq, value);
        this.stringValue = keypair;
    }

    @Override
    public void action() {
        String[] sp = stringValue.getValue().split(":");
        MineStat mineStat = new MineStat(sp[0], sp.length == 2 ? Integer.parseInt(sp[1]) : 25565);
        if (mineStat.isServerUp()) {
            api.sendMessage(new At(api.getOrFail(qq).getId()).plus(stringValue.getValue() + " 服务器信息\n" +
                    "MOTD: " + mineStat.getMotd().replaceAll("\247.", "") + "\n" +
                    "Ping: " + mineStat.getLatency() + "ms\n" +
                    mineStat.getCurrentPlayers() + "/" + mineStat.getMaximumPlayers() + " 在线"));
        } else {
            api.sendMessage(new At(api.getOrFail(qq).getId()).plus(stringValue.getValue() + " 服务器信息\n" +
                    "连接超时或服务器处于离线状态"));
        }
    }
}
