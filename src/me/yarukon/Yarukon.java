package me.yarukon;

import me.yarukon.utils.TimeHelper;
import net.mamoe.mirai.Bot;

import java.net.URI;
import java.util.Objects;

public class Yarukon {
    public static Yarukon INSTANCE;
    public boolean isRunning;

    public TimeHelper timeHelper = new TimeHelper();
    public TimeHelper connectionCheckTimer = new TimeHelper();

    public Yarukon() {
        INSTANCE = this;

        isRunning = true;
        new VentiBotUpdateThread(60).start();
    }

    public void update() {
        if (timeHelper.delay(1500, true) && BotMain.INSTANCE.wsClient != null && !BotMain.INSTANCE.wsClient.isClosed()) {
            BotMain.INSTANCE.wsClient.send("Websocket Keep-Alive");
        }

        if (connectionCheckTimer.delay(5000, true) && BotMain.INSTANCE.wsClient != null && BotMain.INSTANCE.wsClient.isClosed()) {
            try {
                BotMain.INSTANCE.wsClient = new WebsocketClient(new URI("ws://127.0.0.1:11332/sonar"), BotMain.INSTANCE.getLogger());
                BotMain.INSTANCE.wsClient.connect();
                BotMain.INSTANCE.getLogger().info("Websocket 断开, 尝试重连!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
