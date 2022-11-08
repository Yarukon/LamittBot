package me.yarukon.thread;

import me.yarukon.BotMain;
import me.yarukon.utils.TimeHelper;
import me.yarukon.utils.WebsocketClient;

import java.net.URI;

public class UpdateThread extends Thread {
    int tickrate;
    long threadTime = 0;

    public UpdateThread(int updateRate) {
        super("VentiBot update loop-thread");
        this.tickrate = updateRate;
    }

    @Override
    public void run() {
        long lastLoopTime = System.nanoTime();
        final long OPTIMAL_TIME = 1000000000 / tickrate;
        long lastUpdateTime = 0;

        BotMain.INSTANCE.getLogger().info("更新线程已注册!");
        while (true) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;

            if (updateLength < OPTIMAL_TIME) {
                continue;
            }

            lastLoopTime = now;

            lastUpdateTime += updateLength;
            if (lastUpdateTime >= 1000000000) {
                lastUpdateTime = 0;
            }

            try {
                onUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                threadTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
                Thread.sleep(threadTime);
            } catch (Exception ignored) {}
        }
    }

    public TimeHelper timeHelper = new TimeHelper();
    public TimeHelper connectionCheckTimer = new TimeHelper();

    public TimeHelper receiveAndSendMsgTimer = new TimeHelper();
    public TimeHelper cpuLoadUpdateTimer = new TimeHelper();

    public void onUpdate() {
        if (timeHelper.delay(1500, true) && BotMain.INSTANCE.wsClient != null && BotMain.INSTANCE.wsClient.isOpen() && !BotMain.INSTANCE.wsClient.isClosed()) {
            BotMain.INSTANCE.wsClient.send("Websocket Keep-Alive");
        }

        if (connectionCheckTimer.delay(5000, true) && BotMain.INSTANCE.wsClient != null && !BotMain.INSTANCE.wsClient.isOpen() && BotMain.INSTANCE.wsClient.isClosed()) {
            try {
                BotMain.INSTANCE.wsClient = new WebsocketClient(new URI("ws://127.0.0.1:11332/sonar"), BotMain.INSTANCE.getLogger());
                BotMain.INSTANCE.wsClient.connect();
                BotMain.INSTANCE.getLogger().info("Websocket 断开, 尝试重连!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (cpuLoadUpdateTimer.delay(1000, true)) {
            BotMain.INSTANCE.cpuLoad = BotMain.INSTANCE.cpu.getSystemCpuLoadBetweenTicks(BotMain.INSTANCE.prevTicks) * 100;
            BotMain.INSTANCE.prevTicks = BotMain.INSTANCE.cpu.getSystemCpuLoadTicks();
        }

        if (receiveAndSendMsgTimer.delay(60000, true)) {
            BotMain.receiveInOneMin = BotMain.receiveInOneMinTemp;
            BotMain.sendInOneMin = BotMain.sendInOneMinTemp;

            BotMain.receiveInOneMinTemp = 0;
            BotMain.sendInOneMinTemp = 0;
        }
    }
}