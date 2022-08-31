package me.yarukon.command.impl;

import me.yarukon.value.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.thread.impl.PriceCheckThread;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class CommandMItem extends Command {

    public CommandMItem() {
        super("mitem", "FFXIV查价指令", CommandType.GROUP_CHAT);
        this.setUsage(".mitem <物品名称> <大区/服务器> [查询数量]");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (value.priceCheck.getValue()) {
            if (args.length == 3) {
                if (!StringUtils.isNumeric(args[2])) {
                    group.sendMessage("非数字!");
                    return;
                }

                new PriceCheckThread(group, sender.getId(), value, args[0], args[1], Integer.parseInt(args[2])).start();
            } else if(args.length == 2) {
                new PriceCheckThread(group, sender.getId(), value, args[0], args[1], 10).start();
            } else {
                group.sendMessage(this.getUsage());
            }
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {}

    @Override
    public boolean isCommandUsable(Values values) {
        return values.priceCheck.getValueState();
    }
}
