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

public class CommandGState extends Command {
    public CommandGState() {
        super("gstate", "调整群配置的启用状态", CommandType.PRIVATE_CHAT);
        this.setOwnerOnly(true);
        this.setUsage(".gstate <群名称部分/全名>");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {

    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        if (args.length >= 1) {
            try {
                Bot b = Bot.getInstanceOrNull(BotMain.INSTANCE.targetBotQQ);
                if (b != null) {
                    boolean succ = false;
                    String message = "";
                    for (Group g : b.getGroups()) {
                        if (g.getName().contains(args[0])) {
                            if (BotMain.INSTANCE.groupIDs.contains(g.getId())) {
                                succ = true;
                                Values v = BotMain.INSTANCE.values.get(g.getId());
                                v.enabled.setValue(!v.enabled.getValueState());
                                friend.sendMessage("群 " + g.getName() + " 配置启用状态更改为 " + (v.enabled.getValue() ? "启用" : "禁用"));
                                BotMain.INSTANCE.saveConfig();
                            } else {
                                message = "群 " + g.getName() + " 没有可用的群配置!";
                            }
                            break;
                        }
                    }

                    if (!succ && message.isEmpty()) {
                        message = "找不到包含关键词 " + args[0] + " 的群名称!";
                    }

                    if (!succ) {
                        friend.sendMessage(message);
                    }
                } else {
                    friend.sendMessage("无法获取到 Bot 对象, 这不该发生的!");
                }
            } catch (Exception ex) {
                friend.sendMessage("Exception: " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        } else {
            this.sendUsage(friend);
        }
    }
}
