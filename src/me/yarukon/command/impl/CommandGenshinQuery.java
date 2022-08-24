package me.yarukon.command.impl;

import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.thread.impl.GenshinQueryThread;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class CommandGenshinQuery extends Command {
    public CommandGenshinQuery() {
        super("原神查询", "原神信息查询");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (value.genshinInfoQuery.getValue()) { //原神UID信息查询
            if (args.length != 1) return; //当长度超出限制时不进行解析
            if (!StringUtils.isNumeric(args[0])) return; //当文本中含有非数字字符时不进行解析
            if (args[0].length() != 9) return; //当值不等于9位时不进行解析

            long uid = Long.parseLong(args[0]);
            new GenshinQueryThread(group, sender.getId(), value, uid).start();
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {

    }
}
