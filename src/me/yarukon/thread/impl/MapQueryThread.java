package me.yarukon.thread.impl;

import me.yarukon.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.source.SteamServerInfo;
import me.yarukon.utils.source.SteamServerQuery;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;

public class MapQueryThread extends ProcessThread {

    public MapQueryThread(Group api, long qq, Values value) {
        super(api, qq, value);
    }

    @Override
    public void action() throws Exception {
        SteamServerQuery query = new SteamServerQuery("43.248.185.86:27099");
        SteamServerInfo result = query.getInfo();

        if (result != null) {
            api.sendMessage(new At(qq).plus(String.format("http://39.105.209.208:12249/zs/maps/%s.bsp.bz2", result.getMap())));
        } else {
            api.sendMessage(new At(qq).plus("无法连接至服务器!"));
        }
    }
}