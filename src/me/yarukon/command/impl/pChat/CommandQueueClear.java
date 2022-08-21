package me.yarukon.command.impl.pChat;

import me.yarukon.EventFactory;
import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandQueueClear extends Command {
    public CommandQueueClear() {
        super("clearqueue", "清除正在执行指令的QQ序列", CommandType.PRIVATE_CHAT);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {

    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        EventFactory.processingQQ.clear();
        friend.sendMessage("尝试清除! 清除后有 " + EventFactory.processingQQ.size() + " 个序列.");
    }
}
