package me.yarukon.command.impl;

import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.source.SteamServerInfo;
import me.yarukon.utils.source.SteamServerQuery;
import me.yarukon.value.Values;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

// Temp hard-coding
public class CommandMap extends Command {

    public CommandMap() {
        super("map", "Temp hard coding command", CommandType.GROUP_CHAT);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (group.getId() == 219903624) {
            new MapProcessThread(group, sender.getId(), value).start();
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {}

    public class MapProcessThread extends ProcessThread {
        public MapProcessThread(Group api, long qq, Values value) {
            super(api, qq, value);
        }

        @Override
        public void action() throws Exception {
            SteamServerQuery query = new SteamServerQuery("103.205.254.230", 27099);
            SteamServerInfo result = query.getInfo();
            if (result != null) {
                MessageChain chain = new MessageChainBuilder().append(new At(api.getOrFail(qq).getId())).append("http://39.105.209.208:12249/zs/maps/").append(result.getMap()).append(".bsp.bz2").build();
                api.sendMessage(chain);
            } else {
                api.sendMessage("错误: 无法获取到服务器信息");
            }
            super.action();
        }
    }
}
