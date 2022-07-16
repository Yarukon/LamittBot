package me.yarukon.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.yarukon.utils.image.Element;
import me.yarukon.utils.image.impl.*;

public enum GenshinQueryUtil {

    INSTANCE;

    public String dsSalt = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs";
    public String mhyVersion = "2.11.1";
    public String client_type = "5";

    private String generateDS(String url, String body) {
        String query = "";
        String[] urlPart = url.split("\\?");
        if(urlPart.length == 2) {
            String[] paras = urlPart[1].split("&");
            query = String.join("&", paras);
        }

        long time = System.currentTimeMillis() / 1000;
        String rand = getRandStr(6);
        String md5 = md5(String.format("salt=%s&t=%d&r=%s&b=%s&q=%s", dsSalt, time, rand, body, query));

        return String.format("%s,%s,%s", time, rand, md5);
    }

    private String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception ex) {
            return "";
        }
    }

    private String getRandStr(int amount) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < amount; ++i) {
            result.append(chars.charAt(new Random().nextInt(chars.length())));
        }
        return result.toString();
    }

    public String[] sendGet(String serverId, long uid) throws Exception {
        String url = "https://api-takumi.mihoyo.com/game_record/app/genshin/api/index?role_id=" + uid + "&server=" + serverId;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET"); // 请求方法为GET

        // Headers
        con.setRequestProperty("Accept", "application/json, text/plain");
        con.setRequestProperty("DS", generateDS(url, ""));
        con.setRequestProperty("Cookie", "_ga=GA1.2.568383698.1630747327; _gid=GA1.2.1020298170.1630747327; _MHYUUID=52ce3868-e094-49d1-a84b-4f0cf57bfd96; aliyungf_tc=aefaaec81c39f8bcca3f66f898582ca91e32535590801fcfe8fff186024140ab; UM_distinctid=17bb01dd15117-01dbd666e9d7c4-a7d193d-1fa400-17bb01dd152bf8; ltoken=GG0YYZ1YGxNbdvHdSJ3dZSfE7RqvzwVkbXpvVOu2; ltuid=6064082; cookie_token=isCNlp5W21z1yUMufU1HIsx4BtfMNsX5SgFtL71c; account_id=6064082");
        con.setRequestProperty("Origin", "https://webstatic.mihoyo.com");
        con.setRequestProperty("x-rpc-app_version", mhyVersion);
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 11; Mi 9 Pro 5G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/82.0.0.0 Mobile Safari/537.36 miHoYoBBS/2.4.0");
        con.setRequestProperty("x-rpc-client_type", client_type);
        con.setRequestProperty("Referer", "https://webstatic.mihoyo.com/app/community-game-records/index.html?v=6");
        con.setRequestProperty("Accept-Encoding", "deflate");
        con.setRequestProperty("Accept-Language", "zh-CN,en-US;q=0.8");
        con.setRequestProperty("X-Requested-With", "com.mihoyo.hyperion");

        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String line;

        while ((line = br.readLine()) != null) {
            line = new String(line.getBytes());
            sb.append(line);
        }

        br.close();
        con.disconnect();

        return new String[]{con.getResponseCode() + "", sb.toString()};
    }

    public ArrayList<Character> characterAnalysis(JsonArray arr) {
        ArrayList<Character> characters = new ArrayList<>();

        for (int i = 0; i < arr.size(); ++i) {
            JsonObject obj = arr.get(i).getAsJsonObject();
            String characterName = getString(obj.get("name"));
            int characterLevel = getInteger(obj.get("level"));
            String characterElement = getString(obj.get("element"));
            int fetter = getInteger(obj.get("fetter"));
            int rarityLvl = getInteger(obj.get("rarity"));
            int activeConstellationNum = getInteger(obj.get("actived_constellation_num"));
            String iconPath = getString(obj.get("image")).replace("https://upload-bbs.mihoyo.com/game_record/genshin/character_icon/UI_AvatarIcon_", "");

            characters.add(new Character(characterName, characterLevel, characterElement, Math.min(rarityLvl, 5), activeConstellationNum, fetter, iconPath));
        }

        //重新排序
        characters.sort(Comparator.comparingInt(a -> a.lvl));
        Collections.reverse(characters);

        return characters;
    }

    //状态解析
    public ArrayList<Element> statusAnalysis(long id, JsonObject obj) {
        ArrayList<Element> tempo = new ArrayList<>();

        tempo.add(new CenterTextElement(75, 23, 660, 20, 5, "「" + id + "」 角色背包"));

        int startX = 75;
        int startY = 35;
        ArrayList<Character> characters = characterAnalysis(obj.getAsJsonArray("avatars"));
        for(int i = 0; i < characters.size(); ++i) {
            Character c = characters.get(i);
            CharacterElement ele = new CharacterElement(startX, startY, 128, 170, 5, c);
            tempo.add(ele);
            startX += ele.width + ele.space;

            if(startX + ele.width > 770 && (i + 1) != characters.size()) {
                startY += ele.height + ele.space + 10;
                startX = 75;
            } else if(i + 1 == characters.size()) {
                startY += ele.height + ele.space + 10;
            }
        }

        JsonObject obj2 = obj.getAsJsonObject("stats");
        String activeDays = getIntAsString(obj2.get("active_day_number")); //活跃天数
        String achievement = getIntAsString(obj2.get("achievement_number")); //完成成就数量
        String avatarAmount = getIntAsString(obj2.get("avatar_number")); //拥有的角色数量
        String waypoint = getIntAsString(obj2.get("way_point_number")); //已解锁的传送点数量
        String domain = getIntAsString(obj2.get("domain_number")); //已解锁的秘境数量
        String spiralAbyss = getString(obj2.get("spiral_abyss")).startsWith("-") ? "暂未挑战" : getString(obj2.get("spiral_abyss")); //深渊进度
        String common_chest = getIntAsString(obj2.get("common_chest_number")); //已打开的普通宝箱
        String exquisite_chest = getIntAsString(obj2.get("exquisite_chest_number")); //已打开的精致宝箱
        String precious_chest = getIntAsString(obj2.get("precious_chest_number")); //已打开珍贵宝箱
        String luxurious_chest = getIntAsString(obj2.get("luxurious_chest_number")); //已打开的华丽宝箱
        String anemoculus = getIntAsString(obj2.get("anemoculus_number")); //已收集的风神瞳数量
        String geoculus = getIntAsString(obj2.get("geoculus_number")); //已收集的岩神瞳数量
        String electroculus = getIntAsString(obj2.get("electroculus_number")); //已收集的雷神瞳数量

        startY += 25;
        tempo.add(new CenterTextElement(75, startY, 660, 20, 5, "「" + id + "」 状态"));
        startY += 15;

        ArrayList<Status> stats = new ArrayList<>();
        stats.add(new Status("活跃天数", activeDays));
        stats.add(new Status("已达成成就", achievement));
        stats.add(new Status("角色数量", avatarAmount));
        stats.add(new Status("已解锁传送点", waypoint));
        stats.add(new Status("已解锁秘境", domain));
        stats.add(new Status("深渊螺旋", spiralAbyss));
        stats.add(new Status("普通宝箱", common_chest));
        stats.add(new Status("精致宝箱", exquisite_chest));
        stats.add(new Status("珍贵宝箱", precious_chest));
        stats.add(new Status("华丽宝箱", luxurious_chest));
        stats.add(new Status("风神瞳", anemoculus));
        stats.add(new Status("岩神瞳", geoculus));
        stats.add(new Status("雷神瞳", electroculus));

        startX = 75;
        for(int i = 0; i < stats.size(); ++i) {
            Status c = stats.get(i);
            StatusElement stat = new StatusElement(startX, startY, 128, 80, 5, c.text, c.value);
            tempo.add(stat);
            startX += stat.width + stat.space;

            if(startX + stat.width > 770 && (i + 1) != stats.size()) {
                startY += stat.height + stat.space;
                startX = 75;
            } else if(i + 1 == stats.size()) {
                startY += stat.height + stat.space;
            }
        }

        startY += 25;
        tempo.add(new CenterTextElement(75, startY, 660, 20, 5, "「" + id + "」 世界探索"));
        startY += 15;

        //当前探索进度
        ArrayList<Region> regions = new ArrayList<>();
        JsonArray arr = obj.getAsJsonArray("world_explorations");
        for(int i = 0; i < arr.size(); ++i) {
            JsonObject shit = arr.get(i).getAsJsonObject();
            int level = shit.get("level").getAsInt();
            String name = shit.get("name").getAsString();
            String iconPath = shit.get("icon").getAsString().replace("https://upload-bbs.mihoyo.com/game_record/genshin/city_icon/UI_ChapterIcon_", "_Region_");
            boolean isOffering = shit.get("type").getAsString().equals("Offering");
            float percentage = Math.min(100, (float) (shit.get("exploration_percentage").getAsInt() / 10));
            JsonArray offering = shit.getAsJsonArray("offerings");
            if(offering.size() > 0) {
                HashMap<String, Integer> off = new HashMap<>();
                for(int a = 0; a < offering.size(); ++a) {
                    JsonObject dicc = offering.get(a).getAsJsonObject();
                    String offeringName = dicc.get("name").getAsString();
                    int offeringLvl = dicc.get("level").getAsInt();
                    off.put(offeringName, offeringLvl);
                }

                regions.add(new Region(name, percentage + "", level + "", !isOffering, iconPath, off));
            } else {
                regions.add(new Region(name, percentage + "", level + "", !isOffering, iconPath, null));
            }

        }

        startX = 75;
        for(int i = 0; i < regions.size(); ++i) {
            Region c = regions.get(i);
            RegionElement reg = new RegionElement(startX, startY, 325, 156, 10, c);
            tempo.add(reg);
            startX += reg.width + reg.space;

            if(startX + reg.width > 770 && (i + 1) != regions.size()) {
                startY += reg.height + reg.space;
                startX = 75;
            } else if(i + 1 == regions.size()) {
                startY += reg.height + reg.space;
            }
        }

        //家园系统
        JsonArray arr2 = obj.getAsJsonArray("homes");
        if(arr2.size() > 0) {
            startY += 20;
            tempo.add(new CenterTextElement(75, startY, 660, 20, 5, "「" + id + "」 尘歌壶"));
            startY += 15;

            for(int i = 0; i < arr2.size(); ++i) {
                JsonObject shit = arr2.get(i).getAsJsonObject();
                tempo.add(new HomeElement(75, startY, 660, 150, 10, shit.get("name").getAsString(), shit.get("level").getAsInt(), shit.get("comfort_num").getAsInt(), shit.get("comfort_level_name").getAsString(), shit.get("visit_num").getAsInt(), shit.get("item_num").getAsInt()));
                startY += 160;
            }
        }

        startY += 15;
        tempo.add(new CenterTextElement(75, startY, 660, 20, 5, "Generated by Venti-Bot by Yarukon"));
        return tempo;
    }

    private String getString(JsonElement a) {
        return a.getAsString();
    }

    private int getInteger(JsonElement a) {
        return a.getAsInt();
    }

    private String getIntAsString(JsonElement a) {
        return a.getAsInt() + "";
    }

    public static class Character {
        public String name; //名称
        public int lvl; //等级
        public String ele; //元素
        public int rarity; //稀有度
        public int activeConstellation; //已激活的命之座数量
        public int fetter; //好感度
        public String iconPath; //头像路径

        public Character(String name, int lvl, String ele, int rarity, int activeConstellation, int fetter, String iconPath) {
            this.name = name;
            this.lvl = lvl;
            this.ele = ele;
            this.rarity = rarity;
            this.activeConstellation = activeConstellation;
            this.fetter = fetter;
            this.iconPath = iconPath;
        }
    }

    public static class Status {
        public String text;
        public String value;

        public Status(String text, String value) {
            this.text = text;
            this.value = value;
        }
    }

    public static class Region {
        public String place;
        public String explorePercennt;
        public String Lvl;
        public boolean isCity;
        public String iconPath;
        public HashMap<String, Integer> offerings = new HashMap<>();

        public Region(String place, String explorePercent, String lvl, boolean isCity, String iconPath, HashMap<String, Integer> offerings) {
            this.place = place;
            this.explorePercennt = explorePercent;
            this.Lvl = lvl;
            this.isCity = isCity;
            this.iconPath = iconPath;
            this.offerings = offerings;
        }
    }
}