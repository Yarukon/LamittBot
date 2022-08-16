package me.yarukon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.yarukon.node.NodeManager;
import me.yarukon.thread.impl.*;
import me.yarukon.utils.FFXIVUtil;
import me.yarukon.value.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
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
                this.onGroupCommand(event.getGroup(), event.getGroup().getId(), event.getSender().getId(), event.getMessage(), event.getMessage().contentToString(), INSTANCE.values.get(event.getGroup().getId()));
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onFriendMessage(FriendMessageEvent event) {
        if (this.log == null) {
            log = event.getBot().getLogger();
        }

        try {
            if (event.getFriend().getId() == INSTANCE.botOwnerQQ) {
                this.onFriendCommand(event.getFriend(), event.getMessage().contentToString());
            }
        } catch (Exception ignored) {
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
            if (processingQQ.contains(qq)) { //防止滥用
                return;
            }

            if (value.enableSSI.getValue()) { //起源服务器信息查询功能
                if (value.SSIServerList.hasKey(msg.substring(1))) {
                    new ValveServerQueryThread(api, qq, value, value.SSIServerList.getValue(msg.substring(1))).start();
                }
            }

            if (value.mapQuery.getValue()) {
                if (value.SSIServerList.hasKey(msg.substring(1))) {
                    new MapQueryThread(api, qq, value).start();
                }
            }

            if (value.daXueXi.getValue()) {
                if (msg.startsWith(".大学习") && msg.split(" ").length == 2) {
                    new DaXueXiThread(api, qq, value, msg.split(" ")[1]).start();
                }
            }

            if (value.genshinInfoQuery.getValue()) { //原神UID信息查询
                if (msg.startsWith(".原神查询")) {
                    String[] splited = msg.split(" ");
                    if (splited.length != 2) return; //当长度超出限制时不进行解析
                    if (!StringUtils.isNumeric(splited[1])) return; //当文本中含有非数字字符时不进行解析
                    if (splited[1].length() != 9) return; //当值不等于9位时不进行解析

                    long uid = Long.parseLong(splited[1]);
                    new GenshinQueryThread(api, qq, value, uid).start();
                }
            }

            if (value.minecraftStatQuery.getValue()) {
                if (value.minecraftServerIP.hasKey(msg.substring(1))) {
                    new MinecraftServerQueryThread(api, qq, value, value.minecraftServerIP.getValue(msg.substring(1))).start();
                }
            }

            if (msg.startsWith(".mitem") && value.priceCheck.getValue()) {
                String[] sp = msg.split(" ");
                if (sp.length == 4) {
                    if (!StringUtils.isNumeric(sp[3])) {
                        api.sendMessage("非数字!");
                        return;
                    }

                    new PriceCheckThread(api, qq, value, sp[1], sp[2], Integer.parseInt(sp[3])).start();
                } else if(sp.length == 3) {
                    new PriceCheckThread(api, qq, value, sp[1], sp[2], 10).start();
                } else {
                    api.sendMessage("用法: .mitem <物品名称> <大区/服务器名称> [显示数量]");
                }
            }

            if (qq == INSTANCE.botOwnerQQ) {
                String[] sp = msg.split(" ");

                if (msg.startsWith(".reply reload")) {
                    if (NodeManager.INSTANCE != null) {
                        try {
                            NodeManager.INSTANCE.load((JsonObject) JsonParser.parseString(FileUtils.readFileToString(BotMain.INSTANCE.autoReplyPath, StandardCharsets.UTF_8)));
                            api.sendMessage("重载了 " + NodeManager.INSTANCE.nodes.size() + " 个自动回复节点!");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            api.sendMessage("重载失败! ERR: " + ex.getLocalizedMessage());
                        }
                    }
                }

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

            if (msg.startsWith(".帮助")) {
                api.sendMessage("========== Venti Bot ==========\n" +
                        ".帮助 - 显示此帮助菜单\n" +
                        ".添加群 <群号> - 新建并启用指定群的配置\n" +
                        ".移除群 <群号> - 移除指定群的配置\n" +
                        ".群列表 - 列出已启用的群\n" +
                        ".变量列表 <群号> - 列出指定群的所有变量\n" +
                        ".所有群列表 - 显示机器人当前加入的所有群组\n" +
                        "注意: 若要配置群配置请在群内配置");
            }

            if (msg.startsWith(".添加群")) {
                if (sp.length == 2) {
                    if (StringUtils.isNumeric(sp[1]) && Long.parseLong(sp[1]) > 0) {
                        api.sendMessage(INSTANCE.addGroup(Long.parseLong(sp[1])));
                    } else {
                        api.sendMessage("参数有误, 请检查!");
                    }
                } else {
                    api.sendMessage("参数有误, 请检查!");
                }
            }

            if (msg.startsWith(".移除群")) {
                if (sp.length == 2) {
                    if (StringUtils.isNumeric(sp[1]) && Long.parseLong(sp[1]) > 0) {
                        api.sendMessage(INSTANCE.removeGroup(Long.parseLong(sp[1])));
                    } else {
                        api.sendMessage("参数有误, 请检查!");
                    }
                } else {
                    api.sendMessage("参数有误, 请检查!");
                }
            }

            if (msg.startsWith(".群列表")) {
                StringBuilder sb = new StringBuilder();
                sb.append("当前存在 ").append(INSTANCE.groupIDs.size()).append(" 个群配置:\n");

                for (long id : INSTANCE.groupIDs) {
                    Group g = api.getBot().getGroup(id);
                    sb.append(id).append(" - ").append(g == null ? "群列表不存在" : g.getName()).append("\n");
                }

                api.sendMessage(sb.substring(0, sb.length() - 1));
            }

            if (msg.startsWith(".所有群列表")) {
                StringBuilder sb = new StringBuilder();
                sb.append("当前共加入了 ").append(api.getBot().getGroups().size()).append(" 个群:\n");

                for (Group g : api.getBot().getGroups()) {
                    sb.append(g.getId()).append(" - ").append(g.getName()).append("\n");
                }

                api.sendMessage(sb.substring(0, sb.length() - 1));
            }

            if (msg.startsWith(".变量列表")) {
                if (sp.length == 2) {
                    if (StringUtils.isNumeric(sp[1]) && Long.parseLong(sp[1]) > 0) {
                        if (INSTANCE.values.containsKey(Long.parseLong(sp[1]))) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(sp[1]).append(" 的群设定:\n");

                            for (Value v : INSTANCE.values.get(Long.parseLong(sp[1])).valuesList) {
                                sb.append(v.getKey()).append(" - ").append(v.getValue()).append("\n");
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

                        if (BotMain.INSTANCE.customLocalize.containsKey(huntInfo.name)) {
                            huntInfo.name = BotMain.INSTANCE.customLocalize.get(huntInfo.name);
                        }

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
                            chain = new MessageChainBuilder().append("[" + huntInfo.dataCenter + "-" + huntInfo.region + "] " + huntInfo.rank + " " + huntInfo.name + " " + huntInfo.zone + " (" + String.format("%.2f", huntInfo.x) + ", " + String.format("%.2f", huntInfo.y) + ") " + (huntInfo.fateState)).append("\n").append(image != null ? image : new PlainText("不存在该地图文件 " + huntInfo.mapPath)).asMessageChain();
                        } else {
                            chain = new MessageChainBuilder().append("[" + huntInfo.dataCenter + "-" + huntInfo.region + "] Rank " + huntInfo.rank + " " + huntInfo.name + " " + huntInfo.zone + " (" + String.format("%.2f", huntInfo.x) + ", " + String.format("%.2f", huntInfo.y) + ")" + (huntInfo.isDead ? " [死亡]" : "")).append(!huntInfo.isDead ? "\n" : "").append(image != null ? image : huntInfo.isDead ? new PlainText("") : new PlainText("不存在该地图文件 " + huntInfo.mapPath)).asMessageChain();
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
