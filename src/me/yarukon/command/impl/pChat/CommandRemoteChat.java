package me.yarukon.command.impl.pChat;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.value.Values;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class CommandRemoteChat extends Command {
    public CommandRemoteChat() {
        super("rChat", "在指定群内发送信息", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
        this.setUsage(".rChat <群号> <信息...>");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {}

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        if (args.length >= 2) {
            if (StringUtils.isNumeric(args[0]) && Long.parseLong(args[0]) > 0) {
                try {
                    StringBuilder sb = new StringBuilder();
                    if (args.length > 2) {
                        for(int i = 1; i < args.length; ++i) {
                            sb.append(args[i]).append(" ");
                        }
                    } else {
                        sb.append(args[1]);
                    }

                    Bot b = Bot.getInstanceOrNull(BotMain.INSTANCE.targetBotQQ);
                    if (b != null) {
                        Group group = b.getGroupOrFail(Long.parseLong(args[0]));
                        if (group != null) {
                            group.sendMessage(sb.toString());
                            friend.sendMessage("成功: " + group.getName() + " -> " + sb);
                        } else {
                            friend.sendMessage("机器人未在指定的群号内!");
                        }
                    }
                } catch (Exception ex) {
                    friend.sendMessage("执行时发生错误! Exception:\n" + ex.getLocalizedMessage());
                }
            } else {
                friend.sendMessage("无效群号!");
            }
        } else {
            this.sendUsage(friend);
        }
    }
}
