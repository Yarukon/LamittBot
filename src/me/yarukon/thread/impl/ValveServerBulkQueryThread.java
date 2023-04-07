package me.yarukon.thread.impl;

import com.sun.jna.platform.win32.WinGDI;
import me.yarukon.BotMain;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;
import me.yarukon.utils.image.impl.LineElement;
import me.yarukon.utils.image.impl.TextElement;
import me.yarukon.utils.source.SteamServerInfo;
import me.yarukon.utils.source.SteamServerQuery;
import me.yarukon.value.Values;
import me.yarukon.value.impl.Keypair;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ValveServerBulkQueryThread extends ProcessThread {

    public Keypair msValue;

    public ValveServerBulkQueryThread(Group api, long qq, Values value, Keypair msValue) {
        super(api, qq, value);
        this.msValue = msValue;
    }

    @Override
    public void action() throws Exception {
        ArrayList<Element> elements = new ArrayList<>();

        int startY = 25;
        LineElement head = new LineElement(5, startY, 600, 30, 5, "起源服务器批量查询");
        elements.add(head);

        startY += 30;
        for(String s : msValue.getValue().split(",")) {
            SteamServerQuery query = new SteamServerQuery(s);
            SteamServerInfo result = query.getInfo();

            String name = "服务器离线/连接超时/返回无效";
            String currentMap = "未知";
            String currentPly = "未知";
            String latency = "0ms";

            if (result != null) {
                name = result.getName();
                currentMap = result.getMap();
                currentPly = result.getPlayers() + " / " + Math.abs(result.getMaxPlayers());
                latency = result.getLatency() + "ms";

                if (BotMain.INSTANCE.mapTranslation.containsKey(currentMap)) {
                    currentMap += " (" + BotMain.INSTANCE.mapTranslation.get(currentMap) + ")";
                }
            } else if (query.getPlayer() != null) {
                name = "服务器返回的A2S数据无效!";
                currentPly = query.getPlayer().getPlayers().length + "/未知";
            }

            TextElement serverIP = new TextElement(5, startY, 600, 20, 0, ">>  "+ s, ImageUtils.font4);
            TextElement serverName = new TextElement(5, startY + 25, 600, 20, 0, name, ImageUtils.font4);
            TextElement mapName = new TextElement(5, startY + 50, 600, 20, 0, "地图: " + currentMap, ImageUtils.font4);
            TextElement playerNum = new TextElement(5, startY + 75, 600, 20, 0, "玩家: " + currentPly, ImageUtils.font4);
            TextElement _latency = new TextElement(5, startY + 100, 600, 20, 0, "延迟: " + latency, ImageUtils.font4);

            elements.add(serverIP);
            elements.add(serverName);
            elements.add(mapName);
            elements.add(playerNum);
            elements.add(_latency);

            startY += 130;
        }

        ByteArrayInputStream stream = ImageUtils.createImageToInputStream(600, 0, true, elements, "jpg");

        ExternalResource extRes = ExternalResource.create(stream);
        Image img = api.uploadImage(extRes);

        api.sendMessage(new At(api.getOrFail(qq).getId()).plus("\n").plus(img));

        extRes.close();
        stream.close();
    }
}
