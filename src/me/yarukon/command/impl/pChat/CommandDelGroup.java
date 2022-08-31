package me.yarukon.command.impl.pChat;

import me.yarukon.BotMain;
import me.yarukon.value.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class CommandDelGroup extends Command {
    public CommandDelGroup() {
        super("del", "删除对应的群的配置", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {

    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        if (args.length == 1) {
            if (StringUtils.isNumeric(args[0]) && Long.parseLong(args[0]) > 0) {
                friend.sendMessage(BotMain.INSTANCE.removeGroup(Long.parseLong(args[0])));
            } else {
                friend.sendMessage("参数有误, 请检查!");
            }
        }
    }
}
