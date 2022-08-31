package me.yarukon.thread.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.yarukon.BotMain;
import me.yarukon.value.Values;
import me.yarukon.thread.ProcessThread;
import me.yarukon.utils.GenshinQueryUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;

public class GenshinQueryThread extends ProcessThread {

    public long uid;

    public GenshinQueryThread(Group api, long qq, Values value, long uid) {
        super(api, qq, value);
        this.uid = uid;
    }

    @Override
    public void action() {
        String str = "原神玩家 " + uid + " 的信息: ";
        try {
            String uidStr = String.valueOf(uid);
            String serverId = (uidStr.startsWith("1") || uidStr.startsWith("2")) ? "cn_gf01" : String.valueOf(uid).startsWith("5") ? "cn_qd01" : "N/A";

            if (serverId.equalsIgnoreCase("N/A")) {
                api.sendMessage(new At(api.getOrFail(qq).getId()).plus(str + "\n解析时发生错误\n不受支持的UID"));
                this.join();
            }

            String[] query = GenshinQueryUtil.INSTANCE.sendGet(serverId, uid);

            if (!query[0].equals("200")) this.join(); //当网页返回结果不是200时不发送信息

            JsonObject obj = (JsonObject) JsonParser.parseString(query[1]);
            Image img = null;
            boolean success;
            if (obj.get("retcode").getAsInt() != 0) {
                str += "\n错误 " + obj.get("retcode").getAsInt() + ": " + obj.get("message").getAsString();
                success = false;
            } else {
                ExternalResource res = ExternalResource.create(new ByteArrayInputStream(BotMain.INSTANCE.imgUtil.generateStatImage(uid, obj.getAsJsonObject("data")).toByteArray()));
                img = api.uploadImage(res);
                res.close();
                success = true;
            }

            MessageChain chain = new MessageChainBuilder().append(new At(api.getOrFail(qq).getId())).append(new PlainText("\n")).append((img != null ? img : new PlainText(success ? "图片上传发生错误" : str))).build();
            api.sendMessage(chain);
        } catch (Exception ex) {
            api.sendMessage(new At(api.getOrFail(qq).getId()).plus(str + "\n解析时发生错误\n" + ex.getLocalizedMessage()));
        }
    }

}