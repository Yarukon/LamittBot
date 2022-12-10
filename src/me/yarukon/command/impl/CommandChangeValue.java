package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.value.ValueBase;
import me.yarukon.value.Values;
import me.yarukon.value.impl.*;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class CommandChangeValue extends Command {
    public CommandChangeValue() {
        super("cval");
        this.setOwnerOnly(true);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (args.length >= 2) {
            boolean processed = false;

            for (ValueBase val : value.valuesList) {
                if (val.getKey().equals(args[0])) {
                    if (val instanceof BooleanValue) {
                        if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                            val.setValue(Boolean.parseBoolean(args[1].toLowerCase()));
                            group.sendMessage("成功: " + val.getKey() + " 的值设置为 " + val.getValue());
                        } else {
                            group.sendMessage("请输入正确的布尔值 (true 或 false)!");
                        }

                        processed = true;
                    } else if (val instanceof StringValue) {
                        val.setValue(args[1]);
                        group.sendMessage("成功: " + val.getKey() + " 的值设置为 " + val.getValue());

                        processed = true;
                    } else if (val instanceof ModeValue) {
                        ModeValue mv = (ModeValue) val;
                        if (mv.setValue(args[1])) {
                            group.sendMessage("成功: " + mv.getKey() + " 的值设置为 " + mv.getValue());
                        } else {
                            group.sendMessage("失败: " + mv.getKey() + " 的模式列表内不存在 " + args[1] + "! 可用的模式有 " + StringUtils.join(mv.modes, ", "));
                        }

                        processed = true;
                    } else if (val instanceof MultiMapValue) {
                        MultiMapValue mmv = (MultiMapValue) val;

                        if (args[1].equals("list")) {
                            StringBuilder sb = new StringBuilder();
                            for(Keypair vv : mmv.getValues().values()) {
                                sb.append(vv.key).append(" - ").append(vv.getValue()).append("\n");
                            }

                            group.sendMessage(sb.substring(0, sb.toString().length() - 1));
                        } else if(args.length >= 3 && args[1].equals("del")) {
                            if (mmv.hasKey(args[2])) {
                                mmv.removeValue(args[2]);
                                group.sendMessage("移除成功");
                            } else {
                                group.sendMessage("指定的键值 " + args[2] + " 不存在!");
                            }
                        } else if (args.length >= 4 && args[1].equals("add")) {
                            if (mmv.hasKey(args[2])) {
                                Keypair msv = mmv.getValue(args[2]);
                                if (msv != null) {
                                    msv.setValue(args[3]);
                                    group.sendMessage("修改: " + msv.key + " -> " + msv.getValue());
                                } else {
                                    group.sendMessage("错误: 获取的键值无效!");
                                }
                            } else {
                                Keypair msv = new Keypair(args[2], args[3]);
                                mmv.addValue(args[2], msv);
                                group.sendMessage("添加: " + msv.key + " -> " + msv.getValue());
                            }
                        }

                        processed = true;
                    }

                    BotMain.INSTANCE.saveConfig();
                    break;
                }
            }

            if (!processed) {
                group.sendMessage("处理指令失败, 请检查变量名!");
            }
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {

    }
}
