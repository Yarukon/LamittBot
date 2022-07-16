package me.yarukon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.yarukon.node.NodeManager;
import me.yarukon.utils.image.ImageUtils;
import me.yarukon.value.*;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotMain extends JavaPlugin {
    public long botOwnerQQ;

    public String filePath = "";
    public File extResources;
    public File genshinExtRes;

    public File cachePath;
    public HashMap<Long, Values> values = new HashMap<>();
    public CopyOnWriteArrayList<Long> groupIDs = new CopyOnWriteArrayList<>();
    public static BotMain INSTANCE;

    public ImageUtils imgUtil = new ImageUtils();

    // 狩猎地图缓存
    public BufferedImage redFlagImage;
    public File huntMapPath;
    public HashMap<String, BufferedImage> huntMap = new HashMap<>();

    // 物品ID缓存
    public HashMap<String, Integer> itemIDs = new HashMap<>();

    // 自定义汉化缓存
    public HashMap<String, String> customLocalize = new HashMap<>();

    // 自动回复节点
    public File autoReplyPath;

    public BotMain() {
        super(new JvmPluginDescriptionBuilder("me.yarukon.BotMain", "1.0").author("Yarukon").info("Venti Bot").build());
    }

    public EventFactory eventFactory;

    public WebsocketClient wsClient;

    public final String[] dataCenterFriendlyName = new String[] {"狗", "猪", "猫", "鸟"};
    public final String[] dataCenterName = new String[] {"豆豆柴", "莫古力", "猫小胖", "陆行鸟"};
    public final String[] allZoneName = new String[] {"水晶塔", "银泪湖", "太阳海岸", "伊修加德", "拂晓之间", "旅人栈桥", "梦羽宝境", "潮风亭", "白金幻象", "白银乡", "神拳痕", "龙巢神殿", "延夏", "摩杜纳", "柔风海湾", "海猫茶屋", "琥珀原", "紫水栈桥", "静语庄园", "宇宙和音", "幻影群岛", "拉诺西亚", "晨曦王座", "沃仙曦染", "神意之地", "红玉海", "萌芽池"};

    @Override
    public void onEnable() {
        //初始化
        INSTANCE = this;

        values.clear();
        this.getLogger().info("[Yarukon] Venti Bot 正在启动...");

        filePath = this.getConfigFolder().getAbsolutePath();
        extResources = new File(filePath + File.separator + "VentiBot" + File.separator + "resource");
        genshinExtRes = new File(extResources.getAbsolutePath() + File.separator + "GenshinRes");
        cachePath = new File(filePath + File.separator + "VentiBot" + File.separator + "cache");

        //创建资源目录
        if(!extResources.exists())
            extResources.mkdir();

        //创建缓存目录
        if(!cachePath.exists())
            cachePath.mkdir();

        this.getLogger().info("[Yarukon] 配置文件路径为 " + filePath);
        this.getLogger().info("[Yarukon] 外部文件路径为 " + extResources.getAbsolutePath());
        this.getLogger().info("[Yarukon] 缓存文件路径为 " + cachePath.getAbsolutePath());

        // 地图文件夹
        this.huntMapPath = new File(extResources.getAbsolutePath() + File.separator + "HuntMap");

        try {
            redFlagImage = ImageIO.read(new File(this.huntMapPath + File.separator + ".." + File.separator + "redflag.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 缓存文件生成 要加啥就在下边的方法创建
        this.createCacheFolder();

        // 图片生成Util初始化
        imgUtil.init(this.getLogger());

        // 物品ID读取并缓存
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(extResources.getAbsolutePath() + File.separator + "Items.txt")), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] splited = line.split(",");
                itemIDs.put(splited[1], Integer.parseInt(splited[0]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 读取汉化并缓存
        try {
            JsonObject obj = (JsonObject) JsonParser.parseString(FileUtils.readFileToString(new File(extResources.getAbsolutePath() + File.separator + "HuntLocalize.json"), StandardCharsets.UTF_8));
            for(Map.Entry<String, JsonElement> elementEntry : obj.entrySet()) {
                this.customLocalize.put(elementEntry.getKey(), elementEntry.getValue().getAsString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 设定自动回复文件路径然后初始化
        this.autoReplyPath = Paths.get(extResources.getAbsolutePath(), "replys.json").toFile();
        try {
            new NodeManager((JsonObject) JsonParser.parseString(FileUtils.readFileToString(this.autoReplyPath, StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(this.loadConfig()) {
            GlobalEventChannel.INSTANCE.registerListenerHost(eventFactory = new EventFactory(this));

            try {
                wsClient = new WebsocketClient(new URI("ws://127.0.0.1:11332/sonar"), this.getLogger());
                wsClient.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            new Yarukon();
        }
    }

    public String getDataCenterNameFromFriendlyName(String in) {
        for(int i = 0; i < this.dataCenterFriendlyName.length; ++i) {
            if (this.dataCenterFriendlyName[i].equals(in)) {
                return this.dataCenterName[i];
            }
        }

        return null;
    }

    public boolean isDataCenterExist(String in) {
        for (String s : this.dataCenterName) {
            if (s.equals(in)) {
                return true;
            }
        }

        return false;
    }

    public boolean isZoneExist(String in) {
        for(String s : this.allZoneName) {
            if (s.equals(in)) return true;
        }

        return false;
    }

    public void createCacheFolder() {}

    public boolean loadConfig() {
        File jsonFile = new File(filePath + "/VentiBotCfg.json");
        if (!jsonFile.exists()) {
            try {
                if(jsonFile.createNewFile()) {
                    this.getLogger().error("[Yarukon] 配置文件不存在, 已自动创建!");
                } else {
                    this.getLogger().error("[Yarukon] 配置文件不存在, 但创建失败!");
                }
                return false;
            } catch (Exception ex) {
                this.getLogger().error("[Yarukon] 创建配置文件时发生错误!");
                ex.printStackTrace();
                return false;
            }
        } else {
            try {
                FileReader fileReader = new FileReader(jsonFile);
                Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8);
                int ch;
                StringBuilder sb = new StringBuilder();
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
                fileReader.close();
                reader.close();

                JsonObject root = (JsonObject) JsonParser.parseString(sb.toString());
                botOwnerQQ = root.get("BotOwnerQQ").getAsLong();
                JsonArray groups = root.get("Groups").getAsJsonArray();
                for(int i = 0; i < groups.size(); ++i) {
                    JsonObject group = groups.get(i).getAsJsonObject();
                    Values vals = new Values(group.get("GroupID").getAsLong());

                    for(Value val : vals.valuesList) {
                        if(group.has(val.getKey())) {
                            if(val instanceof BooleanValue) {
                                val.setValue(group.get(val.getKey()).getAsBoolean());
                            } else if(val instanceof StringValue) {
                                val.setValue(group.get(val.getKey()).getAsString());
                            } else if(val instanceof ModeValue) {
                                if(!((ModeValue) val).setValue(group.get(val.getKey()).getAsString())) {
                                    this.getLogger().warning("[Yarukon] " + val.getKey() + " 中不存在 " + group.get(val.getKey()).getAsString() + " 模式, 使用缺省值!");
                                }
                            } else if(val instanceof MultiBooleanValue) {
                                MultiBooleanValue vall = (MultiBooleanValue) val;
                                for(Map.Entry<String, JsonElement> settings : group.get(val.getKey()).getAsJsonObject().entrySet()) {
                                    MultiBoolean mb = vall.getSetting(settings.getKey());

                                    if(mb == null)
                                        continue;

                                    if(!settings.getValue().getAsJsonPrimitive().isBoolean())
                                        continue;

                                    mb.setState(settings.getValue().getAsBoolean());
                                }
                            } else if (val instanceof MultiMapValue) {
                                MultiMapValue vall = (MultiMapValue) val;
                                for(Map.Entry<String, JsonElement> settings : group.get(val.getKey()).getAsJsonObject().entrySet()) {
                                    vall.addValue(settings.getKey(), new Keypair(settings.getKey(), settings.getValue().getAsString()));
                                }
                            } else if (val instanceof MultiStringValue) {
                                MultiStringValue vall = (MultiStringValue) val;
                                JsonArray arr = group.get(vall.getKey()).getAsJsonArray();
                                for (int j = 0; j < arr.size(); ++j) {
                                    vall.addValue(arr.get(j).getAsString());
                                }
                            }
                        }
                    }

                    this.values.put(group.get("GroupID").getAsLong(), vals);
                    this.groupIDs.add(group.get("GroupID").getAsLong());
                }
                this.getLogger().info("[Yarukon] 读取了 " + this.values.size() + " 个数据!");
                return true;
            } catch (Exception ex) {
                this.getLogger().error("[Yarukon] 读取配置文件时发生错误, 请确认格式是否正确!");
                ex.printStackTrace();
                return false;
            }
        }
    }

    public void saveConfig() {
        try {
            File jsonFile = new File(filePath + "/VentiBotCfg.json");
            Writer fileWriter = new OutputStreamWriter(Files.newOutputStream(jsonFile.toPath()), StandardCharsets.UTF_8);;

            JsonObject root = new JsonObject();
            JsonArray groups = new JsonArray();
            root.addProperty("BotOwnerQQ", this.botOwnerQQ);

            for(Map.Entry<Long, Values> entry : this.values.entrySet()) {
                JsonObject group = new JsonObject();
                group.addProperty("GroupID", entry.getKey());
                for(Value val : entry.getValue().valuesList) {
                    if(val instanceof BooleanValue) {
                        group.addProperty(val.getKey(), ((BooleanValue) val).getValue());
                    } else if(val instanceof StringValue) {
                        group.addProperty(val.getKey(), ((StringValue) val).getValue());
                    } else if(val instanceof ModeValue) {
                        group.addProperty(val.getKey(), ((ModeValue) val).getValue());
                    } else if (val instanceof MultiBooleanValue) {
                        JsonObject obj = new JsonObject();

                        for(Map.Entry<String, MultiBoolean> mb : ((MultiBooleanValue) val).getValues().entrySet()) {
                            obj.addProperty(mb.getValue().name, mb.getValue().state);
                        }

                        group.add(val.getKey(), obj);
                    } else if (val instanceof MultiMapValue) {
                        JsonObject obj = new JsonObject();

                        for(Map.Entry<String, Keypair> mb : ((MultiMapValue) val).getValues().entrySet()) {
                            obj.addProperty(mb.getKey(), mb.getValue().getValue());
                        }

                        group.add(val.getKey(), obj);
                    } else if (val instanceof MultiStringValue) {
                        JsonArray arr = new JsonArray();

                        for(String value : ((MultiStringValue) val).getValues()) {
                            arr.add(value);
                        }

                        group.add(val.getKey(), arr);
                    }
                }
                groups.add(group);
            }

            root.add("Groups", groups);

            fileWriter.write(root.toString());
            fileWriter.close();

            this.getLogger().info("[Yarukon] 配置保存成功!");
        } catch (Exception ex) {
            ex.printStackTrace();
            this.getLogger().error("[Yarukon] 保存配置文件时发生错误!");
        }
    }

    public String addGroup(long groupID) {
        if(!this.values.containsKey(groupID)) {
            this.values.put(groupID, new Values(groupID));

            if(!this.groupIDs.contains(groupID)) {
                this.groupIDs.add(groupID);
            }

            this.saveConfig();
            return "成功: 成功添加了群 " + groupID;
        } else {
            return "失败: 该配置已存在!";
        }
    }

    public String removeGroup(long groupID) {
        if(this.values.containsKey(groupID)) {
            this.values.remove(groupID);
            this.groupIDs.remove(groupID);

            this.saveConfig();
            return "成功: 成功移除了群 " + groupID;
        } else {
            return "失败: 该配置不存在!";
        }
    }
}
