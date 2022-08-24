package me.yarukon.command;

import me.yarukon.BotMain;
import me.yarukon.EventFactory;
import me.yarukon.Values;
import me.yarukon.command.impl.CommandDXX;
import me.yarukon.command.impl.CommandGenshinQuery;
import me.yarukon.command.impl.pChat.*;
import me.yarukon.command.impl.CommandMItem;
import me.yarukon.command.impl.CommandReplyReload;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandManager {
    public final List<Command> commands = new CopyOnWriteArrayList<>();
    public static final String PREFIX = ".";

    public CommandManager() {
        initCommands();
    }

    public void initCommands() {
        // 群指令
        addCommand(new CommandDXX());
        addCommand(new CommandMItem());
        addCommand(new CommandGenshinQuery());

        // 私聊指令
        addCommand(new CommandHelp());
        addCommand(new CommandAddGroup());
        addCommand(new CommandDelGroup());
        addCommand(new CommandGroupList());
        addCommand(new CommandQueueClear());
        addCommand(new CommandJoinedGroupList());

        // 两者
        addCommand(new CommandReplyReload());
    }

    public void addCommand(Command cmd) {
        commands.add(cmd);
    }

    public void groupChatCommand(Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (EventFactory.processingQQ.contains(sender.getId()))
            return;

        String[] split = msg.substring(1).split(" ");

        if (split.length == 0)
            return;

        String cmdName = split[0];

        Command command = commands.stream().filter(cmd -> cmd.match(cmdName)).findFirst().orElse(null);

        try {
            if (command != null && command.getType() != CommandType.PRIVATE_CHAT) {
                if (command.isOwnerOnly() && group.getId() != BotMain.INSTANCE.botOwnerQQ)
                    return;

                String[] args = new String[split.length - 1];
                System.arraycopy(split, 1, args, 0, split.length - 1);
                command.groupChat(args, group, sender, msgChain, msg, value);
            }
        } catch (CommandException exception) {}
    }

    public void privateChatCommand(Friend friend, String msg) {
        String[] split = msg.substring(1).split(" ");

        if (split.length == 0)
            return;

        String cmdName = split[0];

        Command command = commands.stream().filter(cmd -> cmd.match(cmdName)).findFirst().orElse(null);

        try {
            if (command != null && command.getType() != CommandType.GROUP_CHAT) {
                if (command.isOwnerOnly() && friend.getId() != BotMain.INSTANCE.botOwnerQQ)
                    return;

                String[] args = new String[split.length - 1];
                System.arraycopy(split, 1, args, 0, split.length - 1);
                command.privateChat(args, friend, msg);
            }
        } catch (CommandException exception) {}
    }
}
