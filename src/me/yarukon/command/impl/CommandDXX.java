package me.yarukon.command.impl;

import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.thread.impl.DaXueXiThread;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandDXX extends Command {

    public CommandDXX() {
        super("大学习", "获取指定ID的青年大学习答案", CommandType.GROUP_CHAT);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (value.daXueXi.getValue() && args.length == 1) {
            new DaXueXiThread(group, sender.getId(), value, args[0]).start();
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {}
}
