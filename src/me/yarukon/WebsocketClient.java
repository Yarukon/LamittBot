package me.yarukon;

import com.google.gson.Gson;
import me.yarukon.utils.BotUtils;
import net.mamoe.mirai.utils.MiraiLogger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebsocketClient extends WebSocketClient {

    public MiraiLogger logger;

    public WebsocketClient(URI serverUri, MiraiLogger logger) {
        super(serverUri);
        this.logger = logger;
        this.setConnectionLostTimeout(-1);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.logger.info("连接至 " + this.getURI().toString());
    }

    @Override
    public void onMessage(String s) {
        HuntInfo info = BotUtils.gson.fromJson(s, HuntInfo.class);
        BotMain.INSTANCE.eventFactory.huntInfoBroadcast(info);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        this.logger.info("连接断开 原因: " + s);
    }

    @Override
    public void onError(Exception e) {
        this.logger.error(e.getMessage(), e);
    }
}
