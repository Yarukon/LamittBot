package me.yarukon.command.impl.pChat;

import me.yarukon.BotMain;
import me.yarukon.value.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandGroupList extends Command {
    public CommandGroupList() {
        super("list", "显示已经启用的群", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {}

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前存在 ").append(BotMain.INSTANCE.groupIDs.size()).append(" 个群配置:\n");

        for (long id : BotMain.INSTANCE.groupIDs) {
            Group g = friend.getBot().getGroup(id);
            sb.append(id).append(" - ").append(g == null ? "群列表不存在" : g.getName()).append("\n");
        }

        friend.sendMessage(sb.substring(0, sb.length() - 1));
    }
}
