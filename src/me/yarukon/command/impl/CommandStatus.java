package me.yarukon.command.impl;

import me.yarukon.BotMain;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.command.OwnerOnlyType;
import me.yarukon.value.Values;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import oshi.util.FormatUtil;

public class CommandStatus extends Command {
    public CommandStatus() {
        super("status", "显示Bot状态", CommandType.BOTH);
        this.setOwnerOnly(true);
        this.setOwnerOnlyType(OwnerOnlyType.BOTH);
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        group.sendMessage(getMsg().toString());
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        friend.sendMessage(getMsg().toString());
    }

    public StringBuilder getMsg() {
        Bot b = Bot.getInstanceOrNull(BotMain.INSTANCE.targetBotQQ);

        StringBuilder sb = new StringBuilder();
        sb.append("[Venti Bot] Version ").append(BotMain.PLUGIN_VERSION).append("\n"); // Version
        sb.append(b == null ? "Unknown" : b.getNick()).append(" (").append(BotMain.INSTANCE.targetBotQQ).append(")").append("\n"); // Bot name and QQ
        sb.append("加入的群: ").append(b == null ? "-1" : b.getGroups().size()).append(" 个 已启用: ").append(BotMain.INSTANCE.values.size()).append(" 个").append("\n"); // Group size and enabled size
        sb.append("接收信息: ").append(BotMain.receiveInOneMin).append("/m 总量: ").append(BotMain.totalReceive).append("\n"); // Received message in total
        sb.append("发送信息: ").append(BotMain.sendInOneMin).append("/m 总量: ").append(BotMain.totalSend).append("\n"); // Sent message in total

        long passed = (System.currentTimeMillis() - BotMain.startTimestamp) / 1000;
        long h = passed / 3600;
        long m = (passed % 3600) / 60;
        long s = passed % 60;

        sb.append("已运行时长: ").append(String.format("%02d:%02d:%02d", h, m, s)).append("\n"); // Run time
        sb.append("CPU占用率: ").append(Math.floor(BotMain.INSTANCE.cpuLoad)).append("%").append("\n"); // CPU Load

        long memUsed = BotMain.INSTANCE.hardware.getMemory().getTotal() - BotMain.INSTANCE.hardware.getMemory().getAvailable();
        sb.append("内存占用率: ").append(FormatUtil.formatBytes(memUsed)).append("/").append(FormatUtil.formatBytes(BotMain.INSTANCE.hardware.getMemory().getTotal())); // MEM load
        return sb;
    }
}
