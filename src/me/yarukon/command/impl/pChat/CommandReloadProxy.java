package me.yarukon.command.impl.pChat;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.value.Values;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandReloadProxy extends Command {
    public CommandReloadProxy() {
        super("reloadPxy", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        friend.sendMessage(BotMain.INSTANCE.loadProxy() ? "重载成功." : "重载失败!");
    }
}
