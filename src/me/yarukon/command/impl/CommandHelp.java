package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.command.OwnerOnlyType;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandHelp extends Command {
    public CommandHelp() {
        super("help", "获取帮助", CommandType.BOTH);
        this.setOwnerOnly(true);
        this.setOwnerOnlyType(OwnerOnlyType.PRIVATE_CHAT);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        StringBuilder sb = new StringBuilder();
        for (Command c : BotMain.INSTANCE.commandManager.commands) {
            if (c.getType() != CommandType.PRIVATE_CHAT) {
                sb.append(".").append(c.getName()).append(" - ").append(c.getHelpMessage()).append(c.isOwnerOnly() && (c.getOwnerOnlyType() == OwnerOnlyType.BOTH || c.getOwnerOnlyType() == OwnerOnlyType.GROUP_CHAT) ? " [仅主人可用]" : "").append("\n");
            }
        }

        group.sendMessage("========== Venti Bot ==========\n" + sb);
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        StringBuilder sb = new StringBuilder();
        for (Command c : BotMain.INSTANCE.commandManager.commands) {
            if (c.getType() != CommandType.GROUP_CHAT) {
                sb.append(".").append(c.getName()).append(" - ").append(c.getHelpMessage()).append("\n");
            }
        }

        friend.sendMessage("========== Venti Bot ==========\n" + sb);
    }
}
