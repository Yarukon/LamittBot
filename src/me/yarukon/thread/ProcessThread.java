package me.yarukon.thread;

import me.yarukon.EventFactory;
import me.yarukon.value.Values;
import net.mamoe.mirai.contact.Group;

public class ProcessThread extends Thread {
    public Group api;
    public long qq;
    public Values value;

    public ProcessThread(Group api, long qq, Values value) {
        this.api = api;
        this.qq = qq;
        this.value = value;
    }

    public void run() {
        if (!EventFactory.processingQQ.contains(qq)) {
            EventFactory.processingQQ.add(qq);
        }

        try {
            this.action();
        } catch (Exception ex) {
            api.sendMessage("错误: " + ex.getMessage());
            ex.printStackTrace();
        }

        EventFactory.processingQQ.remove(qq);
    }

    public void action() throws Exception {
    }
}
