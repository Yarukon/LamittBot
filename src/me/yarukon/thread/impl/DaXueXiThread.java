package me.yarukon.thread.impl;

import me.yarukon.value.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.DaXueXiUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.HashMap;
import java.util.Map;

public class DaXueXiThread extends ProcessThread {

    public String cmd;

    public final String REGEX = "^[a-z0-9A-Z]+$";

    public DaXueXiThread(Group api, long qq, Values value, String cmdIn) {
        super(api, qq, value);
        this.cmd = cmdIn;
    }

    public void action() {
        if (cmd != null && cmd.equals("list")) {
            try {
                HashMap<String, String> ids = DaXueXiUtil.getIDs();
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : ids.entrySet()) {
                    sb.append("第 ").append(entry.getKey()).append("期 - ").append(entry.getValue()).append("\n");
                }
                MessageChain mc = new MessageChainBuilder().append("青年大学习ID列表:\n").append(sb.substring(0, sb.length() - 1)).build();
                api.sendMessage(mc);
            } catch (Exception exception) {
                api.sendMessage("获取时发生了一些问题, 请重试!\n" + exception.getMessage());
            }
        } else {
            try {
                if (cmd == null || cmd.isEmpty()) {
                    Map.Entry<String, String> firstEntry = DaXueXiUtil.getIDs().entrySet().stream().findFirst().orElse(null);
                    if (firstEntry != null) {
                        String answer = DaXueXiUtil.getAnswer(firstEntry.getValue());
                        MessageChain mc = new MessageChainBuilder().append("青年大学习ID ").append(firstEntry.getKey()).append(" 答案:\n").append(answer).build();
                        api.sendMessage(mc);
                    } else {
                        api.sendMessage("获取失败!");
                    }
                } else if (cmd.matches(REGEX)) {
                    String answer = DaXueXiUtil.getAnswer(cmd);
                    MessageChain mc = new MessageChainBuilder().append("青年大学习ID ").append(cmd).append(" 答案:\n").append(answer).build();
                    api.sendMessage(mc);
                } else {
                    api.sendMessage("输入的ID无效, 请检查!");
                }
            } catch (Exception exception) {
                api.sendMessage("获取时发生了一些问题, 请重试!\n" + exception.getMessage());
            }
        }
    }
}
