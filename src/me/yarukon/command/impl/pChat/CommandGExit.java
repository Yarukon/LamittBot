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

public class CommandGExit extends Command {
    public CommandGExit() {
        super("!gexit", "退出指定的群 (危险指令, 使用前三思)", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
        this.setUsage(".!gexit <群名称(部分/全拼)>");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {}

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        if (args.length >= 1) {
            try {
                Bot bot = Bot.getInstanceOrNull(BotMain.INSTANCE.targetBotQQ);
                if (bot != null) {
                    boolean succ = false;
                    for (Group g : bot.getGroups()) {
                        if (g.getName().contains(args[0])) {
                            friend.sendMessage("尝试退出群 " + g.getName() + " !");
                            g.quit();
                            succ = true;
                            break;
                        }
                    }

                    if (!succ) {
                        friend.sendMessage("找不到对应的群!");
                    }
                } else {
                    friend.sendMessage("无法获取到Bot对象, 这不该发生的!");
                }
            } catch (Exception ex) {
                friend.sendMessage("错误: " + ex.getLocalizedMessage());
            }
        } else {
            this.sendUsage(friend);
        }
    }
}
