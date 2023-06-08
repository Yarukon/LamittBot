package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.command.OwnerOnlyType;
import me.yarukon.value.ValueBase;
import me.yarukon.value.Values;
import me.yarukon.value.impl.*;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class CommandChangeValue extends Command {
    public CommandChangeValue() {
        super("cval", CommandType.BOTH);
        this.setOwnerOnly(true);
        this.setOwnerOnlyType(OwnerOnlyType.BOTH);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (args.length >= 2) {
            processCVal(args, value, group);
        }
    }

    @Override
    public void privateChat(String[] originalArgs, Friend friend, String msg) {
        String[] args;
        Values value;

        if (originalArgs.length < 3) {
            return;
        }

        value = BotMain.INSTANCE.values.get(Long.parseLong(originalArgs[0]));

        if (value == null) {
            friend.sendMessage("指定的群不存在!");
            return;
        }

        args = Arrays.copyOfRange(originalArgs, 1, originalArgs.length);
        processCVal(args, value, friend);
    }

    public void processCVal(String[] args, Values value, Object instance) {
        boolean processed = false;
        boolean isPrivateChat = instance instanceof Friend;

        for (ValueBase val : value.valuesList) {
            if (val.getKey().equals(args[0])) {
                if (val instanceof BooleanValue) {
                    if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                        val.setValue(Boolean.parseBoolean(args[1].toLowerCase()));
                        sendMessage(isPrivateChat, "成功: " + val.getKey() + " 的值设置为 " + val.getValue(), instance);
                    } else {
                        sendMessage(isPrivateChat, "请输入正确的布尔值 (true 或 false)!", instance);
                    }

                    processed = true;
                } else if (val instanceof StringValue) {
                    val.setValue(args[1]);
                    sendMessage(isPrivateChat, "成功: " + val.getKey() + " 的值设置为 " + val.getValue(), instance);

                    processed = true;
                } else if (val instanceof ModeValue) {
                    ModeValue mv = (ModeValue) val;
                    if (mv.setValue(args[1])) {
                        sendMessage(isPrivateChat, "成功: " + mv.getKey() + " 的值设置为 " + mv.getValue(), instance);
                    } else {
                        sendMessage(isPrivateChat, "失败: " + mv.getKey() + " 的模式列表内不存在 " + args[1] + "! 可用的模式有 " + StringUtils.join(mv.modes, ", "), instance);
                    }

                    processed = true;
                } else if (val instanceof MultiMapValue) {
                    MultiMapValue mmv = (MultiMapValue) val;

                    if (args[1].equals("list")) {
                        StringBuilder sb = new StringBuilder();
                        for (Keypair vv : mmv.getValues().values()) {
                            sb.append(vv.key).append(" - ").append(vv.getValue()).append("\n");
                        }

                        sendMessage(isPrivateChat, sb.substring(0, sb.toString().length() - 1), instance);
                    } else if (args.length >= 3 && args[1].equals("del")) {
                        if (mmv.hasKey(args[2])) {
                            mmv.removeValue(args[2]);
                            sendMessage(isPrivateChat, "移除成功", instance);
                        } else {
                            sendMessage(isPrivateChat, "指定的键值 " + args[2] + " 不存在!", instance);
                        }
                    } else if (args.length >= 4 && args[1].equals("add")) {
                        if (mmv.hasKey(args[2])) {
                            Keypair msv = mmv.getValue(args[2]);
                            if (msv != null) {
                                msv.setValue(args[3]);
                                sendMessage(isPrivateChat, "修改: " + msv.key + " -> " + msv.getValue(), instance);
                            } else {
                                sendMessage(isPrivateChat, "错误: 获取的键值无效!", instance);
                            }
                        } else {
                            Keypair msv = new Keypair(args[2], args[3]);
                            mmv.addValue(args[2], msv);
                            sendMessage(isPrivateChat, "添加: " + msv.key + " -> " + msv.getValue(), instance);
                        }
                    } else if (args[1].equals("!clear")) {
                        mmv.clearAll();
                        sendMessage(isPrivateChat, "清除了所有键值对!", instance);
                    }

                    processed = true;
                }

                BotMain.INSTANCE.saveConfig();
                break;
            }
        }

        if (!processed) {
            sendMessage(isPrivateChat, "处理指令失败, 请检查变量名!", instance);
        }
    }

    public void sendMessage(boolean isPrivateChat, String msg, Object instance) {
        if (isPrivateChat && instance instanceof Friend) {
            ((Friend) instance).sendMessage(msg);
            return;
        }

        ((Group) instance).sendMessage(msg);
    }
}
