package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.value.MultiBoolean;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CommandSonar extends Command {
    public CommandSonar() {
        super("sonar", "群Sonar广播设置");
        this.setOwnerOnly(true);
        this.setUsage(".sonar <region/rank/fate> <list/add/del|value> <FATE名称>");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "region":
                    if (args.length >= 2) {
                        if (args[1].equals("list")) {
                            StringBuilder sb = new StringBuilder();
                            for (Map.Entry<String, MultiBoolean> a : value.regions.getValues().entrySet()) {
                                sb.append(String.format("%s: %s", a.getKey(), a.getValue().getState())).append("\n");
                            }

                            group.sendMessage(sb.substring(0, sb.toString().length() - 1));
                            return;
                        }

                        MultiBoolean mb = value.regions.getSetting(args[1]);
                        if (mb != null) {
                            mb.setState(!mb.getState());
                            group.sendMessage("成功: " + mb.name + " - " + mb.state);
                            BotMain.INSTANCE.saveConfig();
                        } else {
                            group.sendMessage("数据中心 " + args[1] + " 不存在!");
                        }
                    } else {
                        this.sendUsage(group);
                    }
                    break;

                case "rank":
                    if (args.length >= 2) {
                        if (args[1].equals("list")) {
                            StringBuilder sb = new StringBuilder();
                            for (Map.Entry<String, MultiBoolean> a : value.ranks.getValues().entrySet()) {
                                sb.append(String.format("%s: %s", a.getKey(), a.getValue().getState())).append("\n");
                            }
                            group.sendMessage(sb.substring(0, sb.toString().length() - 1));
                            return;
                        }

                        MultiBoolean mb = value.ranks.getSetting(args[1]);
                        if (mb != null) {
                            mb.setState(!mb.getState());
                            group.sendMessage("成功: " + mb.name + " - " + mb.state);
                            BotMain.INSTANCE.saveConfig();
                        } else {
                            group.sendMessage("等级 " + args[1] + " 不存在!");
                        }
                    } else {
                        this.sendUsage(group);
                    }
                    break;

                case "fate":
                    if (args.length >= 2) {
                        if (args[1].equals("list")) {
                            if (value.fateFilter.getValues().size() == 0) break;
                            group.sendMessage("白名单(" + value.fateFilter.getValues().size() + "): " + StringUtils.join(value.fateFilter.getValues(), " / "));
                            break;
                        }

                        if (args.length >= 3) {
                            switch (args[1]) {
                                case "add":
                                    group.sendMessage("添加FATE白名单 " + args[2] + " " + (value.fateFilter.addValue(args[2]) ? "成功" : "失败"));
                                    BotMain.INSTANCE.saveConfig();
                                    break;

                                case "del":
                                    group.sendMessage("移除FATE白名单 " + args[2] + " " + (value.fateFilter.delValue(args[2]) ? "成功" : "失败"));
                                    BotMain.INSTANCE.saveConfig();
                            }
                        } else {
                            this.sendUsage(group);
                        }
                    } else {
                        this.sendUsage(group);
                    }
                    break;

                default:
                    this.sendUsage(group);
            }
        } else {
            this.sendUsage(group);
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {}
}
