package me.yarukon.command.impl.pChat;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.value.ValueBase;
import me.yarukon.value.Values;
import me.yarukon.value.impl.MultiBooleanValue;
import me.yarukon.value.impl.MultiMapValue;
import me.yarukon.value.impl.MultiStringValue;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class CommandGroupValList extends Command {
    public CommandGroupValList() {
        super("gVal", "获取群的设置列表", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {

    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        if (args.length == 0) {
            return;
        }

        if (StringUtils.isNumeric(args[0]) && Long.parseLong(args[0]) > 0) {
            if (BotMain.INSTANCE.values.containsKey(Long.parseLong(args[1]))) {
                StringBuilder sb = new StringBuilder();
                sb.append(args[1]).append(" 的群设定:\n");

                for (ValueBase v : BotMain.INSTANCE.values.get(Long.parseLong(args[1])).valuesList) {
                    sb.append(v.getKey()).append(" - ").append((v instanceof MultiBooleanValue || v instanceof MultiMapValue || v instanceof MultiStringValue) ? "不支持显示" : v.getValue()).append("\n");
                }

                friend.sendMessage(sb.toString());
            } else {
                friend.sendMessage("失败: 指定的群不存在!");
            }
        } else {
            friend.sendMessage("参数有误, 请检查!");
        }
    }
}
