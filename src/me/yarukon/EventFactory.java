package me.yarukon;

import me.yarukon.command.CommandManager;
import me.yarukon.node.NodeManager;
import me.yarukon.thread.impl.*;
import me.yarukon.utils.FFXIVUtil;
import me.yarukon.utils.json.HuntInfo;
import me.yarukon.value.*;
import me.yarukon.value.impl.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventFactory extends SimpleListenerHost {

    public MiraiLogger log = null;
    public BotMain INSTANCE;
    public static CopyOnWriteArrayList<Long> processingQQ = new CopyOnWriteArrayList<>();

    public EventFactory(BotMain main) {
        this.INSTANCE = main;
    }

    @EventHandler
    public void onMessage(GroupMessageEvent event) {
        if (this.log == null) {
            log = event.getBot().getLogger();
        }

        try {
            if (event.getMessage().contentToString().equals(".!退出该群")) {
                event.getGroup().sendMessage("尝试退出该群!");
                event.getGroup().quit();

                // 退群了还处理后面的东西干啥
                return;
            }

            if (INSTANCE.groupIDs.contains(event.getGroup().getId())) {
                Group g = event.getGroup();
                Member s = event.getSender();
                MessageChain msgChain = event.getMessage();
                String msg = msgChain.contentToString();
                Values v = INSTANCE.values.get(event.getGroup().getId());

                if (msg.startsWith(CommandManager.PREFIX)) {
                    BotMain.INSTANCE.commandManager.groupChatCommand(g, s, msgChain, msg, v);
                }

                this.onGroupCommand(g, g.getId(), s.getId(), msgChain, msg, v);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BotMain.totalReceive++;
        BotMain.receiveInOneMinTemp++;
    }

    @EventHandler
    public void onFriendMessage(FriendMessageEvent event) {
        if (this.log == null) {
            log = event.getBot().getLogger();
        }

        try {
            Friend f = event.getFriend();
            String s = event.getMessage().contentToString();

            if (s.startsWith(CommandManager.PREFIX)) {
                BotMain.INSTANCE.commandManager.privateChatCommand(f, s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BotMain.totalReceive++;
        BotMain.receiveInOneMinTemp++;
    }

    @EventHandler
    public void onPostSendGroupMessage(GroupMessagePostSendEvent evt) {
        BotMain.totalSend++;
        BotMain.sendInOneMinTemp++;
    }

    @EventHandler
    public void onPostSendMessage(FriendMessagePostSendEvent evt) {
        BotMain.totalSend++;
        BotMain.sendInOneMinTemp++;
    }

    @EventHandler
    public void onInviteToGroup(BotInvitedJoinGroupRequestEvent event) {
        if (this.log == null) {
            log = event.getBot().getLogger();
        }

        if (event.getInvitorId() == BotMain.INSTANCE.botOwnerQQ) {
            event.accept();
        } else {
            event.ignore();
        }
    }

    @EventHandler
    public void onAddBotAsFriend(NewFriendRequestEvent event) {
        if (this.log == null) {
            log = event.getBot().getLogger();
        }

        event.reject(false);
    }

    private void onGroupCommand(Group api, long groupId, long qq, MessageChain msgChain, String msg, Values value) {
        if (value.autoReply.getValue()) {
            At at = (At) msgChain.stream().filter(At.class::isInstance).findFirst().orElse(null);
            if (at != null && at.getTarget() == BotMain.INSTANCE.targetBotQQ && NodeManager.INSTANCE != null) {
                if (processingQQ.contains(qq)) { //防止滥用
                    return;
                }

                new AutoReplyThread(api, qq, value, msg).start();
            }
        }

        if (msg.startsWith(".")) {
            // 因为这些是通过关键词判断的, 只能保留
            if (value.enableSSI.getValue() && value.SSIServerList.hasKey(msg.substring(1))) { //起源服务器信息查询功能
                new ValveServerQueryThread(api, qq, value, value.SSIServerList.getValue(msg.substring(1))).start();
            }

            if (value.minecraftStatQuery.getValue() && value.minecraftServerIP.hasKey(msg.substring(1))) {
                new MinecraftServerQueryThread(api, qq, value, value.minecraftServerIP.getValue(msg.substring(1))).start();
            }
        }
    }

    public void huntInfoBroadcast(HuntInfo huntInfo) {
        try {
            Bot b = Bot.getInstanceOrNull(BotMain.INSTANCE.targetBotQQ);
            if (b != null) {
                for (Map.Entry<Long, Values> v : BotMain.INSTANCE.values.entrySet()) {
                    Group g = b.getGroup(v.getKey());
                    if (g == null) continue;

                    boolean isFate = huntInfo.rank.equals("FATE") && v.getValue().regions.getValue(huntInfo.dataCenter) && v.getValue().fateFilter.hasValue(huntInfo.name);
                    if ((v.getValue().regions.getValue(huntInfo.dataCenter) && v.getValue().ranks.getValue(huntInfo.rank)) || isFate) {
                        if (!BotMain.INSTANCE.huntMap.containsKey(huntInfo.mapPath)) {
                            File f = new File(BotMain.INSTANCE.huntMapPath, huntInfo.mapPath + ".png");
                            if (f.exists()) {
                                BufferedImage tempImg = ImageIO.read(f);
                                BufferedImage result = new BufferedImage(tempImg.getWidth(), tempImg.getHeight(), BufferedImage.TYPE_INT_RGB);
                                result.createGraphics().drawImage(tempImg, 0, 0, Color.WHITE, null);
                                BotMain.INSTANCE.huntMap.put(huntInfo.mapPath, result);
                            }
                        }

                        Image image = null;
                        if (!huntInfo.isDead && BotMain.INSTANCE.huntMap.containsKey(huntInfo.mapPath)) {
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            ImageIO.write(FFXIVUtil.getHuntPosition(BotMain.INSTANCE.huntMap.get(huntInfo.mapPath), huntInfo.x, huntInfo.y, 1f, 1024), "jpg", os);
                            InputStream is = new ByteArrayInputStream(os.toByteArray());
                            ExternalResource res = ExternalResource.create(is);
                            image = g.uploadImage(res);

                            is.close();
                            os.close();
                            res.close();
                        }

                        MessageChain chain;
                        if (isFate) {
                            chain = new MessageChainBuilder().append("[" + huntInfo.dataCenter + "-" + huntInfo.region + "] " + huntInfo.rank + " " + huntInfo.name + " " + huntInfo.zone + " (" + String.format("%.1f", huntInfo.x) + ", " + String.format("%.1f", huntInfo.y) + ") " + (huntInfo.fateState)).append("\n").append(image != null ? image : new PlainText("不存在该地图文件 " + huntInfo.mapPath)).asMessageChain();
                        } else {
                            chain = new MessageChainBuilder().append("[" + huntInfo.dataCenter + "-" + huntInfo.region + "] Rank " + huntInfo.rank + " " + huntInfo.name + " " + huntInfo.zone + " (" + String.format("%.1f", huntInfo.x) + ", " + String.format("%.1f", huntInfo.y) + ")" + (huntInfo.isDead ? " [死亡]" : "")).append(!huntInfo.isDead ? "\n" : "").append(image != null ? image : huntInfo.isDead ? new PlainText("") : new PlainText("不存在该地图文件 " + huntInfo.mapPath)).asMessageChain();
                        }

                        g.sendMessage(chain);
                        System.gc();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
