package me.yarukon.command;

import me.yarukon.value.Values;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public abstract class Command {

    private final String name;
    private final String helpMessage;
    private final CommandType type;
    private boolean ownerOnly = false;

    private OwnerOnlyType ownerOnlyType = OwnerOnlyType.BOTH;

    private String usage;

    public Command(String name) {
        this(name, "未设置帮助信息");
    }

    public Command(String name, CommandType type) {
        this(name, "未设置帮助信息", type);
    }

    public Command(String name, String helpMessage) {
        this(name, helpMessage, CommandType.GROUP_CHAT);
    }

    public Command(String name, String helpMessage, CommandType type) {
        this.name = name;
        this.helpMessage = helpMessage;
        this.type = type;
    }

    public boolean match(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    public boolean isOwnerOnly() {
        return ownerOnly;
    }

    public void setOwnerOnly(boolean ownerOnly) {
        this.ownerOnly = ownerOnly;
    }

    public CommandType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getHelpMessage() {
        return helpMessage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsage() {
        return "使用方法: " + usage;
    }

    public OwnerOnlyType getOwnerOnlyType() {
        return ownerOnlyType;
    }

    public void setOwnerOnlyType(OwnerOnlyType ownerOnlyType) {
        this.ownerOnlyType = ownerOnlyType;
    }

    public void sendUsage(Group group) {
        group.sendMessage(this.getUsage());
    }

    public boolean isCommandUsable(Values values) {
        return true;
    }

    public abstract void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value);

    public abstract void privateChat(String[] args, Friend friend, String msg);
}
