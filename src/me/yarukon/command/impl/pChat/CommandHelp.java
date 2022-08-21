package me.yarukon.command.impl.pChat;

import me.yarukon.BotMain;
import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandHelp extends Command {
    public CommandHelp() {
        super("help", "获取帮助", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {

    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        StringBuilder sb = new StringBuilder();
        for (Command c : BotMain.INSTANCE.commandManager.commands) {
            if (c.getType() == CommandType.PRIVATE_CHAT) {
                sb.append(".").append(c.getName()).append(" - ").append(c.getHelpMessage()).append("\n");
            }
        }

        friend.sendMessage("========== Venti Bot ==========\n" + sb);
    }
}
