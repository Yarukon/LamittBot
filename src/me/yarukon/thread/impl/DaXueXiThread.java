package me.yarukon.thread.impl;

import me.yarukon.value.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.DaXueXiUtil;
import net.mamoe.mirai.contact.Group;

public class DaXueXiThread extends ProcessThread {

    public String cmd;

    public final String REGEX = "^[a-z0-9A-Z]+$";

    public DaXueXiThread(Group api, long qq, Values value, String cmdIn) {
        super(api, qq, value);
        this.cmd = cmdIn;
    }

    public void action() {
        if (cmd.equals("list")) {
            try {
                DaXueXiUtil.getIDs(api);
            } catch (Exception exception) {
                api.sendMessage("获取时发生了一些问题, 请重试!\n" + exception.getMessage());
            }
        } else {
            try {
                if (cmd.matches(REGEX)) {
                    DaXueXiUtil.getAnswer(cmd, api);
                } else {
                    api.sendMessage("输入的ID无效, 请检查!");
                }
            } catch (Exception exception) {
                api.sendMessage("获取时发生了一些问题, 请重试!\n" + exception.getMessage());
            }
        }
    }
}
