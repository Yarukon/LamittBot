package me.yarukon.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.yarukon.BotMain;
import me.yarukon.Values;
import me.yarukon.command.Command;
import me.yarukon.command.CommandType;
import me.yarukon.node.NodeManager;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;

public class CommandReplyReload extends Command {
    public CommandReplyReload() {
        super("rReload", "重载自动回复节点", CommandType.BOTH);
        this.setOwnerOnly(true);
    }

    public Exception lastException = null;

    @Override
    public void groupChat(String[] args, Group group, Member sender, MessageChain msgChain, String msg, Values value) {
        if (doReload()) {
            group.sendMessage("重载了 " + NodeManager.INSTANCE.nodes.size() + " 个自动回复节点!");
        } else {
            group.sendMessage("重载失败!\n" + lastException == null ? "Unknown error" : lastException.getLocalizedMessage());
            lastException = null;
        }
    }

    @Override
    public void privateChat(String[] args, Friend friend, String msg) {
        if (doReload()) {
            friend.sendMessage("重载了 " + NodeManager.INSTANCE.nodes.size() + " 个自动回复节点!");
        } else {
            friend.sendMessage("重载失败!\n" + lastException == null ? "Unknown error" : lastException.getLocalizedMessage());
            lastException = null;
        }
    }

    public boolean doReload() {
        try {
            NodeManager.INSTANCE.load((JsonObject) JsonParser.parseString(FileUtils.readFileToString(BotMain.INSTANCE.autoReplyPath, StandardCharsets.UTF_8)));
            return true;
        } catch (Exception ex) {
            lastException = ex;
            ex.printStackTrace();
        }

        return false;
    }
}
