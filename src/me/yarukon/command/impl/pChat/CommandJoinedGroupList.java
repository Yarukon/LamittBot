package me.yarukon.command.impl.pChat;

import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class CommandJoinedGroupList extends Command {
    public CommandJoinedGroupList() {
        super("glist", "显示已经加入的群列表", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {

    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前共加入了 ").append(friend.getBot().getGroups().size()).append(" 个群:\n");

        for (Group g : friend.getBot().getGroups()) {
            sb.append(g.getId()).append(" - ").append(g.getName()).append("\n");
        }

        friend.sendMessage(sb.substring(0, sb.length() - 1));
    }
}
