package me.yarukon.command.impl;

import me.yarukon.command.Command;
import me.yarukon.ffxivQuests.FFXIVQuestManager;
import me.yarukon.ffxivQuests.FFXIVQuestResult;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;
import me.yarukon.utils.image.impl.FFXIVQuestElement;
import me.yarukon.value.Values;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

public class CommandFFXIVQuests extends Command {
    public CommandFFXIVQuests() {
        super("quest", "查询FF14的主线进度");
        this.setUsage(".quest <任务名称>");
    }

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (args.length >= 1) {
            FFXIVQuestResult result = FFXIVQuestManager.INSTANCE.findQuest(args[0]);
            if (result.success) {
                Image image = null;
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    generateImage(stream, result);
                    InputStream is = new ByteArrayInputStream(stream.toByteArray());
                    ExternalResource res = ExternalResource.create(is);
                    image = group.uploadImage(res);

                    is.close();
                    stream.close();
                    res.close();
                } catch (Exception ignored) {}

                try {
                    if (image != null) {
                        group.sendMessage(new MessageChainBuilder().append(image).append("\n").append("https://ff14.huijiwiki.com/wiki/").append(URLEncoder.encode("任务:" + result.missionName, "UTF-8")).asMessageChain());
                    } else {
                        group.sendMessage(new MessageChainBuilder().append("生成照片失败").append("\n").append("https://ff14.huijiwiki.com/wiki/").append(URLEncoder.encode("任务:" + result.missionName, "UTF-8")).asMessageChain());
                    }
                } catch (Exception ignored) {}
            } else {
                group.sendMessage(new MessageChainBuilder().append("找不到对应的任务 ").append(args[0]).asMessageChain());
            }
        } else {
            this.sendUsage(group);
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {

    }

    public void generateImage(ByteArrayOutputStream os, FFXIVQuestResult result) throws Exception {
        ArrayList<Element> elements = new ArrayList<>();
        elements.add(new FFXIVQuestElement(result));
        ImageUtils.createImage(350, 0, true, elements, os, "jpg");
    }

    @Override
    public boolean isCommandUsable(Values values) {
        return values.ffxivQuest.getValueState();
    }
}
