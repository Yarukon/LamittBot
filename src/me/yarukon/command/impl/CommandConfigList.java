package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.value.ValueBase;
import me.yarukon.value.Values;
import me.yarukon.value.impl.MultiBooleanValue;
import me.yarukon.value.impl.MultiMapValue;
import me.yarukon.value.impl.MultiStringValue;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandConfigList extends Command {
    public CommandConfigList() {
        super("clist", "显示当前群的配置");
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        StringBuilder sb = new StringBuilder();
        sb.append(group.getName()).append(" 的配置:\n");
        for (ValueBase v : BotMain.INSTANCE.values.get(group.getId()).valuesList) {
            sb.append(v.getKey()).append(" - ").append((v instanceof MultiBooleanValue || v instanceof MultiMapValue || v instanceof MultiStringValue) ? "不支持显示" : v.getValue()).append("\n");
        }
        group.sendMessage(sb.toString());
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {

    }
}
