package me.yarukon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.yarukon.command.CommandManager;
import me.yarukon.node.NodeManager;
import me.yarukon.thread.impl.*;
import me.yarukon.utils.FFXIVUtil;
import me.yarukon.value.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;
import org.apache.commons.io.FileUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

                if (f.getId() == INSTANCE.botOwnerQQ) {
                    this.onFriendCommand(f, s);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

            if (value.genshinInfoQuery.getValue() && msg.startsWith(".原神查询")) { //原神UID信息查询
                String[] splited = msg.split(" ");
                if (splited.length != 2) return; //当长度超出限制时不进行解析
                if (!StringUtils.isNumeric(splited[1])) return; //当文本中含有非数字字符时不进行解析
                if (splited[1].length() != 9) return; //当值不等于9位时不进行解析

                long uid = Long.parseLong(splited[1]);
                new GenshinQueryThread(api, qq, value, uid).start();
            }

            if (qq == INSTANCE.botOwnerQQ) {
                String[] sp = msg.split(" ");

                if (msg.startsWith(".变量列表")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(groupId).append(" 的群设定:\n");
                    for (Value v : INSTANCE.values.get(groupId).valuesList) {
                        sb.append(v.getKey()).append(" - ").append((v instanceof MultiBooleanValue || v instanceof MultiMapValue || v instanceof MultiStringValue) ? "不支持显示" : v.getValue()).append("\n");
                    }
                    api.sendMessage(sb.toString());
                }

                if (msg.startsWith(".变量设置")) {
                    if (sp.length == 3) {
                        boolean success = false;

                        for (Value val : value.valuesList) {
                            if (val.getKey().equals(sp[1])) {
                                if (val instanceof BooleanValue) {
                                    if (sp[2].equalsIgnoreCase("true") || sp[2].equalsIgnoreCase("false")) {
                                        val.setValue(Boolean.parseBoolean(sp[2].toLowerCase()));
                                        api.sendMessage("成功: " + val.getKey() + " 的值设置为 " + val.getValue());
                                    } else {
                                        api.sendMessage("该变量为布尔值, 请输入正确的值 (true 或 false)!");
                                    }
                                } else if (val instanceof StringValue) {
                                    val.setValue(sp[2]);
                                    api.sendMessage("成功: " + val.getKey() + " 的值设置为 " + val.getValue());
                                } else if (val instanceof ModeValue) {
                                    ModeValue mv = (ModeValue) val;
                                    if (mv.setValue(sp[2])) {
                                        api.sendMessage("成功: " + mv.getKey() + " 的值设置为 " + mv.getValue());
                                    } else {
                                        api.sendMessage("失败: " + mv.getKey() + " 的模式列表内不存在 " + sp[2] + "! 可用的模式有 " + StringUtils.join(mv.modes, ", "));
                                    }
                                } else if (val instanceof MultiMapValue) {
                                    MultiMapValue mv = (MultiMapValue) val;
                                    if (sp[2].equals("list")) {
                                        StringBuilder sb = new StringBuilder();
                                        for(Keypair vv : mv.getValues().values()) {
                                            sb.append(vv.key).append(" - ").append(vv.getValue()).append("\n");
                                        }

                                        api.sendMessage(sb.substring(0, sb.toString().length() - 1));
                                    }
                                }
                                success = true;

                                INSTANCE.saveConfig();
                                break;
                            }
                        }

                        if (!success) {
                            api.sendMessage("失败: 未能找到对应的变量!");
                        }
                    } else if (sp.length == 4) {
                        if (sp[2].equals("del")) {
                            boolean succ = false;

                            for (Value v : value.valuesList) {
                                if (v instanceof MultiMapValue) {
                                    MultiMapValue mmv = (MultiMapValue) v;
                                    if (mmv.getKey().equals(sp[1])) {
                                        if (mmv.hasKey(sp[3])) {
                                            mmv.removeValue(sp[3]);
                                            api.sendMessage("移除成功");
                                            succ = true;
                                        } else {
                                            api.sendMessage("不存在!");
                                            succ = true;
                                        }

                                        INSTANCE.saveConfig();
                                        break;
                                    }
                                }
                            }

                            if (!succ) {
                                api.sendMessage("失败: 未能找到对应的变量!");
                            }
                        }
                    } else if (sp.length == 5) {
                        if (sp[2].equals("add")) {
                            boolean succ = false;
                            for (Value v : value.valuesList) {
                                if (v instanceof MultiMapValue) {
                                    MultiMapValue mmv = (MultiMapValue) v;
                                    if (mmv.getKey().equals(sp[1])) {
                                        if (mmv.hasKey(sp[3])) {
                                            Keypair msv = mmv.getValue(mmv.getKey());
                                            msv.setValue(sp[4]);
                                            api.sendMessage("成功: " + msv.key + " 修改为 " + msv.getValue());
                                        } else {
                                            Keypair msv = new Keypair(sp[3], sp[4]);
                                            mmv.addValue(sp[3], msv);
                                            api.sendMessage("添加: " + msv.key + " 值 " + msv.getValue());
                                        }

                                        succ = true;
                                        INSTANCE.saveConfig();
                                        break;
                                    }
                                }
                            }

                            if (!succ) {
                                api.sendMessage("失败: 未能找到对应的变量!");
                            }
                        }
                    }
                }

                if (msg.startsWith(".sonar")) {
                    if (sp.length == 3) {
                        if (sp[1].equals("region")) {
                            if (sp[2].equals("list")) {
                                StringBuilder sb = new StringBuilder();
                                for (Map.Entry<String, MultiBoolean> a : value.regions.getValues().entrySet()) {
                                    sb.append(String.format("%s: %s", a.getKey(), a.getValue().getState())).append("\n");
                                }

                                api.sendMessage(sb.substring(0, sb.toString().length() - 1));
                                return;
                            }

                            MultiBoolean mb = value.regions.getSetting(sp[2]);
                            if (mb != null) {
                                mb.setState(!mb.getState());
                                api.sendMessage(mb.name + ": " + mb.state);
                                BotMain.INSTANCE.saveConfig();
                            } else {
                                api.sendMessage("数据中心 " + sp[2] + " 不存在!");
                            }

                            return;
                        } else if (sp[1].equals("rank")) {
                            if (sp[2].equals("list")) {
                                StringBuilder sb = new StringBuilder();
                                for (Map.Entry<String, MultiBoolean> a : value.ranks.getValues().entrySet()) {
                                    sb.append(String.format("%s: %s", a.getKey(), a.getValue().getState())).append("\n");
                                }

                                api.sendMessage(sb.substring(0, sb.toString().length() - 1));
                                return;
                            }

                            MultiBoolean mb = value.ranks.getSetting(sp[2]);
                            if (mb != null) {
                                mb.setState(!mb.getState());
                                api.sendMessage(mb.name + ": " + mb.state);
                                BotMain.INSTANCE.saveConfig();
                            } else {
                                api.sendMessage("等级 " + sp[2] + " 不存在!");
                            }

                            return;
                        } else if (sp[1].equals("fate") && sp[2].equals("list")) {
                            if (value.fateFilter.getValues().size() == 0) return;
                            api.sendMessage("白名单(" + value.fateFilter.getValues().size() + "): " + StringUtils.join(value.fateFilter.getValues(), " / "));

                            return;
                        }
                    } else if (sp.length == 4) {
                        if (sp[1].equals("fate")) {
                            switch (sp[2]) {
                                case "add":
                                    api.sendMessage("添加FATE白名单 " + sp[3] + " " + (value.fateFilter.addValue(sp[3]) ? "成功" : "失败"));
                                    BotMain.INSTANCE.saveConfig();
                                    break;
                                case "del":
                                    api.sendMessage("移除FATE白名单 " + sp[3] + " " + (value.fateFilter.delValue(sp[3]) ? "成功" : "失败"));
                                    BotMain.INSTANCE.saveConfig();
                                    break;
                            }

                            return;
                        }
                    }

                    api.sendMessage("用法: .sonar <region/rank/fate> <list/add/del|value> <FATE名称>");
                }
            }
        }
    }

    private void onFriendCommand(Friend api, String msg) {
        if (msg.startsWith(".")) {
            String[] sp = msg.split(" ");

            if (msg.startsWith(".发送消息")) {
                if (sp.length >= 3) {
                    if (StringUtils.isNumeric(sp[1]) && Long.parseLong(sp[1]) > 0) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            if (sp.length > 3) {
                                for(int i = 2; i < sp.length; ++i) {
                                    sb.append(sp[i]).append(" ");
                                }
                            } else {
                                sb.append(sp[2]);
                            }

                            Bot b = Bot.getInstanceOrNull(INSTANCE.targetBotQQ);
                            if (b != null) {
                                Group group = b.getGroupOrFail(Long.parseLong(sp[1]));
                                if (group != null) {
                                    group.sendMessage(sb.toString());
                                    api.sendMessage("成功: " + group.getName() + " -> " + sb);
                                } else {
                                    api.sendMessage("Bot未在指定的群号内!");
                                }
                            }
                        } catch (Exception ex) {
                            api.sendMessage("执行时发生错误! Exception:\n" + ex.getLocalizedMessage());
                        }
                    } else {
                        api.sendMessage("无效群号!");
                    }
                } else {
                    api.sendMessage("用法: .发送消息 <群号> <消息>");
                }
            }

            if (msg.startsWith(".变量列表")) {
                if (sp.length == 2) {
                    if (StringUtils.isNumeric(sp[1]) && Long.parseLong(sp[1]) > 0) {
                        if (INSTANCE.values.containsKey(Long.parseLong(sp[1]))) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(sp[1]).append(" 的群设定:\n");

                            for (Value v : INSTANCE.values.get(Long.parseLong(sp[1])).valuesList) {
                                sb.append(v.getKey()).append(" - ").append((v instanceof MultiBooleanValue || v instanceof MultiMapValue || v instanceof MultiStringValue) ? "不支持显示" : v.getValue()).append("\n");
                            }

                            api.sendMessage(sb.toString());
                        } else {
                            api.sendMessage("失败: 指定的群不存在!");
                        }
                    } else {
                        api.sendMessage("参数有误, 请检查!");
                    }
                } else {
                    api.sendMessage("参数有误, 请检查!");
                }
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
                            ImageIO.write(getHuntPosition(BotMain.INSTANCE.huntMap.get(huntInfo.mapPath), huntInfo.x, huntInfo.y, 1f, 1024), "jpg", os);
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

    private final Rectangle2D theBox = new Rectangle2D.Float();

    public BufferedImage getHuntPosition(BufferedImage imageIn, float xPos, float yPos, float scale, int resolution) {
        BufferedImage imgCopy = FFXIVUtil.deepCopy(imageIn);
        Graphics2D g = imgCopy.createGraphics();

        if (BotMain.INSTANCE.redFlagImage != null) {
            AffineTransform affTransform = new AffineTransform();
            g.setColor(new Color(255, 255, 255));
            affTransform.translate(FFXIVUtil.flagToPixel(scale, xPos, resolution) - 16, FFXIVUtil.flagToPixel(scale, yPos, resolution) - 16);
            affTransform.scale(1, 1);
            g.drawImage(BotMain.INSTANCE.redFlagImage, affTransform, null);
        } else {
            g.setColor(new Color(0, 0, 255));
            g.setStroke(new BasicStroke(10));
            theBox.setFrame(FFXIVUtil.flagToPixel(scale, xPos, resolution) - 5, FFXIVUtil.flagToPixel(scale, yPos, resolution) - 5, 10, 10);
            g.draw(theBox);
        }

        g.dispose();
        return imgCopy;
    }
}
