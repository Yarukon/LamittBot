package me.yarukon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.yarukon.node.Node;
import me.yarukon.node.NodeManager;
import me.yarukon.node.impl.DateNode;
import me.yarukon.node.impl.TimeNode;
import me.yarukon.utils.BotUtils;
import me.yarukon.utils.DaXueXiUtil;
import me.yarukon.utils.GenshinQueryUtil;
import me.yarukon.utils.MineStat;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.ImageUtils;
import me.yarukon.utils.image.impl.LineElement;
import me.yarukon.utils.image.impl.LineHeadElement;
import me.yarukon.utils.source.ServerPlayer;
import me.yarukon.utils.source.SteamServerInfo;
import me.yarukon.utils.source.SteamServerQuery;
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
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventFactory extends SimpleListenerHost {

    public MiraiLogger log = null;
    public BotMain INSTANCE;
    public CopyOnWriteArrayList<Long> processingQQ = new CopyOnWriteArrayList<>();

    public EventFactory(BotMain main) {
        this.INSTANCE = main;
    }

    @EventHandler
    public void onMessage(GroupMessageEvent event) {
        if (this.log == null) {
            log = event.getBot().getLogger();
        }

        try {
            if (INSTANCE.groupIDs.contains(event.getGroup().getId())) {
                this.onGroupCommand(event.getGroup(), event.getGroup().getId(), event.getSender().getId(), event.getMessage(), event.getMessage().contentToString(), INSTANCE.values.get(event.getGroup().getId()));
            }

            if (event.getMessage().contentToString().equals(".!退出该群")) {
                event.getGroup().sendMessage("尝试退出该群!");
                event.getGroup().quit();
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

            if (value.priceCheck.getValue()) {
                if (msg.startsWith(".mitem")) {
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
            }

            if (qq == INSTANCE.botOwnerQQ) {
                String[] sp = msg.split(" ");

                if (msg.startsWith(".replys reload")) {
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
                        sb.append(v.getKey()).append(" - ").append(v.getValue()).append("\n");
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
                        ".设置变量 <群号> <变量名称> <值> - 设置指定群的变量状态\n" +
                        ".所有群列表 - 显示机器人当前加入的所有群组");
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

            if (msg.startsWith(".变量设置")) {
                if (sp.length == 4) {
                    if (StringUtils.isNumeric(sp[1]) && Long.parseLong(sp[1]) > 0) {
                        if (INSTANCE.groupIDs.contains(Long.parseLong(sp[1]))) {
                            ArrayList<Value> v = INSTANCE.values.get(Long.parseLong(sp[1])).valuesList;
                            boolean success = false;

                            for (Value val : v) {
                                if (val.getKey().equals(sp[2])) {
                                    if (val instanceof BooleanValue) {
                                        if (sp[3].equalsIgnoreCase("true") || sp[3].equalsIgnoreCase("false")) {
                                            val.setValue(Boolean.parseBoolean(sp[3].toLowerCase()));
                                            api.sendMessage("成功: " + val.getKey() + " 的值设置为 " + val.getValue());
                                        } else {
                                            api.sendMessage("该变量为布尔值, 请输入正确的值 (true 或 false)!");
                                        }
                                    } else if (val instanceof StringValue) {
                                        val.setValue(sp[3]);
                                        api.sendMessage("成功: " + val.getKey() + " 的值设置为 " + val.getValue());
                                    } else if (val instanceof ModeValue) {
                                        ModeValue mv = (ModeValue) val;
                                        if (mv.setValue(sp[3])) {
                                            api.sendMessage("成功: " + mv.getKey() + " 的值设置为 " + mv.getValue());
                                        } else {
                                            api.sendMessage("失败: " + mv.getKey() + " 的模式列表内不存在 " + sp[3] + "! 可用的模式有 " + StringUtils.join(mv.modes, ", "));
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
        BufferedImage imgCopy = deepCopy(imageIn);
        Graphics2D g = imgCopy.createGraphics();

        if (BotMain.INSTANCE.redFlagImage != null) {
            AffineTransform affTransform = new AffineTransform();
            g.setColor(new Color(255, 255, 255));
            affTransform.translate(flagToPixel(scale, xPos, resolution) - 16, flagToPixel(scale, yPos, resolution) - 16);
            affTransform.scale(1, 1);
            g.drawImage(BotMain.INSTANCE.redFlagImage, affTransform, null);
        } else {
            g.setColor(new Color(0, 0, 255));
            g.setStroke(new BasicStroke(10));
            theBox.setFrame(flagToPixel(scale, xPos, resolution) - 5, flagToPixel(scale, yPos, resolution) - 5, 10, 10);
            g.draw(theBox);
        }

        g.dispose();
        return imgCopy;
    }

    public BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public float flagToPixel(float scale, float flag, int resolution) {
        return (flag - 1f) * 50f * scale * (float) resolution / 2048;
    }

    public class ProcessThread extends Thread {
        public Group api;
        public long qq;
        public Values value;

        public ProcessThread(Group api, long qq, Values value) {
            this.api = api;
            this.qq = qq;
            this.value = value;
        }

        public void run() {
            if (!processingQQ.contains(qq)) {
                processingQQ.add(qq);
            }

            try {
                this.action();
            } catch (Exception ex) {
                api.sendMessage("错误: " + ex.getMessage());
                ex.printStackTrace();
            }

            processingQQ.remove(qq);
        }

        public void action() throws Exception {
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public class PriceCheckThread extends ProcessThread {
        public String itemName;
        public String zoneOrWorld;

        public int listingAmount;

        public PriceCheckThread(Group api, long qq, Values value, String itemName, String zoneOrWorld, int listingAmount) {
            super(api, qq, value);
            this.itemName = itemName;
            this.zoneOrWorld = zoneOrWorld;
            this.listingAmount = listingAmount;
        }

        @Override
        public void action() throws Exception {
            boolean isHQ = false;
            String dataCenterName = BotMain.INSTANCE.getDataCenterNameFromFriendlyName(zoneOrWorld);
            if (dataCenterName != null || BotMain.INSTANCE.isDataCenterExist(zoneOrWorld) || BotMain.INSTANCE.isZoneExist(zoneOrWorld)) {
                if (dataCenterName != null) {
                    this.zoneOrWorld = dataCenterName;
                }

                if (itemName.contains("HQ") || itemName.contains("高品质")) {
                    isHQ = true;
                    itemName = itemName.replace("HQ", "");
                    itemName = itemName.replace("高品质", "");
                }

                if (BotMain.INSTANCE.itemIDs.containsKey(itemName)) {
                    int itemID = BotMain.INSTANCE.itemIDs.get(itemName);
                    String result = BotUtils.sendGet("https://universalis.app/api/" + URLEncoder.encode(zoneOrWorld, "UTF-8") + "/" + itemID, "listings=" + listingAmount + (isHQ ? "&hq=true" : ""));
                    UniversalisJson json = BotUtils.gson.fromJson(result, UniversalisJson.class);
                    if (json.itemID != 0) {
                        ByteArrayOutputStream stream = genImage(itemName, isHQ, zoneOrWorld, json);
                        if (stream != null) {
                            ByteArrayInputStream inStream = new ByteArrayInputStream(stream.toByteArray());
                            ExternalResource extRes = ExternalResource.create(inStream);
                            Image img = api.uploadImage(extRes);
                            api.sendMessage(new At(api.getOrFail(qq).getId()).plus("\n").plus(img));

                            extRes.close();
                            inStream.close();
                            stream.close();
                        } else {
                            api.sendMessage("图片生成失败!");
                        }
                    } else {
                        api.sendMessage("物品 " + itemName + " 不支持在市场上售卖!");
                    }
                } else {
                    api.sendMessage("物品 " + itemName + " 不存在!");
                }
            } else {
                api.sendMessage("大区/服务器 " + zoneOrWorld + " 不存在!");
            }
            super.action();
        }

        public ByteArrayOutputStream genImage(String itemName, boolean isHQ, String zoneOrWorld, UniversalisJson jsonIn) {
            ByteArrayOutputStream stream = null;
            try {
                LineElement head = new LineElement(5, 25, 800, 30, 5, "物品名称: " + itemName + (isHQ ? " (HQ)" : "") + " 大区/服务器: " + zoneOrWorld);
                LineElement head2 = new LineElement(5, 50, 800, 30, 5, "最后更新: " + sdf.format(new Timestamp(jsonIn.lastUploadTime)));
                LineHeadElement hqText = new LineHeadElement(30, 75, 30, 20, 5, "高品质");
                LineHeadElement itemCountAndPrice = new LineHeadElement(140, 75, 30, 20, 5, "数量/价格");
                LineHeadElement retainer = new LineHeadElement(500, 75, 30, 20, 5, "雇员名称");
                ArrayList<Element> elements = new ArrayList<>();
                elements.add(head);
                elements.add(head2);
                elements.add(hqText);
                elements.add(itemCountAndPrice);
                elements.add(retainer);

                int startY = 100;
                for(UniversalisJson.UniversalisListingJson jj : jsonIn.listings) {
                    elements.add(new LineHeadElement(73, startY, 30, 20, 5, jj.hq ? "Y" : "N"));
                    elements.add(new LineHeadElement(140, startY, 30, 20, 5, jj.quantity + "x " + jj.pricePerUnit + " (共" + jj.total + ")"));
                    elements.add(new LineHeadElement(500, startY, 30, 20, 5, jj.retainerName + (jj.worldName != null ? " @ " + jj.worldName : "")));
                    startY += 25;
                }

                stream = new ByteArrayOutputStream();
                ImageUtils.createImage(800, 0, true, elements, stream);
            } catch(Exception ex) {
                ex.printStackTrace();
            }

            return stream;
        }
    }

    public class AutoReplyThread extends ProcessThread {
        public String message;

        public AutoReplyThread(Group api, long qq, Values value, String message) {
            super(api, qq, value);
            this.message = message;
        }

        public void action() {
            if (NodeManager.INSTANCE != null) {
                for(Map.Entry<String, ArrayList<Node>> entry : NodeManager.INSTANCE.nodes.entrySet()) {
                    if (message.contains(entry.getKey())) {
                        for(Node node : entry.getValue()) {
                            if (node instanceof TimeNode) {
                                if (((TimeNode) node).isCurrentTime()) {
                                    MessageChain msgChain = new MessageChainBuilder().append(new At(qq)).append(" ").append(node.getRandomStr().replace("{target}", api.getOrFail(qq).getNameCard())).build();
                                    api.sendMessage(msgChain);
                                    break;
                                }
                            } else if (node instanceof DateNode) {
                                if (((DateNode) node).isCurrentDate()) {
                                    MessageChain msgChain = new MessageChainBuilder().append(new At(qq)).append(" ").append(node.getRandomStr().replace("{target}", api.getOrFail(qq).getNameCard())).build();
                                    api.sendMessage(msgChain);
                                    break;
                                }
                            } else {
                                MessageChain msgChain = new MessageChainBuilder().append(new At(qq)).append(" ").append(node.getRandomStr().replace("{target}", api.getOrFail(qq).getNameCard())).build();
                                api.sendMessage(msgChain);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public final String REGEX = "^[a-z0-9A-Z]+$";

    public class DaXueXiThread extends ProcessThread {

        public String cmd;

        public DaXueXiThread(Group api, long qq, Values value, String cmdIn) {
            super(api, qq, value);
            this.cmd = cmdIn;
        }

        public void action() {
            if (cmd.equals("列表")) {
                try {
                    DaXueXiUtil.getIDs(api);
                } catch (Exception exception) {
                    api.sendMessage("获取时发生了一些问题, 请重试!\n" + exception.getMessage());
                }
            } else {
                try {
                    if (cmd.matches(REGEX)) {
                        DaXueXiUtil.getAnswer(cmd, api);
                    } else {
                        api.sendMessage("输入的ID无效, 请检查!");
                    }
                } catch (Exception exception) {
                    api.sendMessage("获取时发生了一些问题, 请重试!\n" + exception.getMessage());
                }
            }
        }
    }

    public class MinecraftServerQueryThread extends ProcessThread {

        public Keypair stringValue;

        public MinecraftServerQueryThread(Group api, long qq, Values value, Keypair keypair) {
            super(api, qq, value);
            this.stringValue = keypair;
        }

        @Override
        public void action() {
            String[] sp = stringValue.getValue().split(":");
            MineStat mineStat = new MineStat(sp[0], sp.length == 2 ? Integer.parseInt(sp[1]) : 25565);
            if (mineStat.isServerUp()) {
                api.sendMessage(new At(api.getOrFail(qq).getId()).plus(stringValue.getValue() + " 服务器信息\n" +
                        "MOTD: " + mineStat.getMotd().replaceAll("\247.", "") + "\n" +
                        "Ping: " + mineStat.getLatency() + "ms\n" +
                        mineStat.getCurrentPlayers() + "/" + mineStat.getMaximumPlayers() + " 在线"));
            } else {
                api.sendMessage(new At(api.getOrFail(qq).getId()).plus(stringValue.getValue() + " 服务器信息\n" +
                        "连接超时或服务器处于离线状态"));
            }
        }
    }

    public class ValveServerQueryThread extends ProcessThread {

        public Keypair msValue;

        public ValveServerQueryThread(Group api, long qq, Values value, Keypair msValue) {
            super(api, qq, value);
            this.msValue = msValue;
        }

        @Override
        public void action() throws Exception {
            log.info("起源服务器查询 > " + api.getId() + " > 发起人 " + qq);
            SteamServerQuery query = new SteamServerQuery(msValue.getValue().contains(":") ? msValue.getValue() : msValue.getValue() + ":27015");
            SteamServerInfo result = query.getInfo();

            String name = "服务器离线/连接超时/返回无效";
            String currentMap = "未知";
            String currentPly = "未知";
            String currentVer = "未知";
            String serverEnv = "未知";
            String latency = "0ms";
            StringBuilder plys = new StringBuilder();

            if (result != null) {
                name = result.getName();
                currentMap = result.getMap();
                currentVer = result.getVersion();
                serverEnv = getServerType((char) result.getEnvironment());
                currentPly = result.getPlayers() + "/" + result.getMaxPlayers();
                latency = result.getLatency() + "ms";
            } else if (query.getPlayer() != null) {
                currentPly = query.getPlayer().getPlayers().length + "/未知";
            }

            if (value.enableSSIPlyList.getValue()) {
                if (query.getPlayer() != null) {
                    for (ServerPlayer player : query.getPlayer().getPlayers()) {
                        plys.append(player.getName()).append(" 分数: ").append(player.getScore()).append("\n");
                    }
                } else {
                    plys.append("服务器离线/无玩家/玩家过多");
                }
            }

            api.sendMessage(new At(api.getOrFail(qq).getId()).plus(
                    "[SSI] 服务器信息\n" +
                            name + "\n" +
                            "版本: " + currentVer + "\n" +
                            "地图: " + currentMap + "\n" +
                            "系统类型: " + serverEnv + "\n" +
                            "在线玩家: " + currentPly + "\n" +
                            "延时: " + latency + "\n" +
                            (value.enableSSIPlyList.getValue() ? "\n——————————————\n" + (plys.toString().endsWith("\n") ? plys.substring(0, plys.length() - 1) : plys) : "")));

            log.info("起源服务器查询 > " + api.getId() + " > 发起人 " + qq + " > 结束");
        }
    }

    public String getServerType(char typeIn) {
        switch (typeIn) {
            case 'l':
                return "Linux";

            case 'w':
                return "Windows";

            case 'm':
                return "MacOS";

            default:
                return "Unknown";
        }
    }

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
}
