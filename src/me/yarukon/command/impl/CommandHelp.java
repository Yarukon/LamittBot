package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.command.CommandManager;
import me.yarukon.value.Values;
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
                if (sender.getId() == BotMain.INSTANCE.botOwnerQQ)
                    sb.append(CommandManager.PREFIX).append(c.getName()).append(" - ").append(c.getHelpMessage()).append(c.isCommandUsable(value) ? " [未启用]" : "").append("\n");
                else if (c.isCommandUsable(value) && (c.getOwnerOnlyType() != OwnerOnlyType.BOTH || c.getOwnerOnlyType() != OwnerOnlyType.GROUP_CHAT))
                    sb.append(CommandManager.PREFIX).append(c.getName()).append(" - ").append(c.getHelpMessage()).append("\n");
            }
        }

        group.sendMessage("========== " + BotMain.BOT_NAME + " ==========\n" + sb);
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        StringBuilder sb = new StringBuilder();
        for (Command c : BotMain.INSTANCE.commandManager.commands) {
            if (c.getType() != CommandType.GROUP_CHAT) {
                sb.append(CommandManager.PREFIX).append(c.getName()).append(" - ").append(c.getHelpMessage()).append("\n");
            }
        }

        friend.sendMessage("========== " + BotMain.BOT_NAME + " ==========\n" + sb);
    }
}
