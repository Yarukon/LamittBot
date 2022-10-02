package me.yarukon.ffxivQuests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.yarukon.BotMain;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Map;

public class FFXIVQuestManager {

    public static FFXIVQuestManager INSTANCE;

    private File questListPath;

    private LinkedList<FFXIVQuestModel> questModels = new LinkedList<>();

    public BufferedImage missionImage;

    public DecimalFormat df = new DecimalFormat("0.00");

    public FFXIVQuestManager() throws Exception {
        INSTANCE = this;
        this.questListPath = new File(BotMain.INSTANCE.extResources, "QuestList.json");
        this.missionImage = ImageIO.read(new File(BotMain.INSTANCE.extResources, "mission.png"));
        JsonObject json = (JsonObject) JsonParser.parseString(FileUtils.readFileToString(this.questListPath, StandardCharsets.UTF_8));
        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().equals("Pre")) {
                FFXIVQuestModel model = new FFXIVQuestModel("重生之境", "2.0", true);
                for(Map.Entry<String, JsonElement> entry1 : entry.getValue().getAsJsonObject().entrySet()) {
                    LinkedList<String> temp = new LinkedList<>();
                    JsonArray array = entry1.getValue().getAsJsonArray();

                    for(JsonElement element : array) {
                        temp.add(element.getAsString());
                    }

                    model.preQuests.put(entry1.getKey(), temp);
                }

                questModels.add(model);
            } else {
                String[] patchNameAndVer = entry.getKey().split("_");
                FFXIVQuestModel model = new FFXIVQuestModel(patchNameAndVer[0], patchNameAndVer[1], false);
                JsonArray array = entry.getValue().getAsJsonArray();
                for(JsonElement element : array) {
                    model.quests.add(element.getAsString());
                }

                questModels.add(model);
            }
        }
    }

    public FFXIVQuestResult findQuest(String name) {
        FFXIVQuestResult result = new FFXIVQuestResult(false, "", "找不到对应的任务", "", 0, 0);
        for(FFXIVQuestModel model : questModels) {
            if (model.isPre) {
                boolean canBreak = false;

                for(Map.Entry<String, LinkedList<String>> entry : model.preQuests.entrySet()) {
                    if (entry.getValue().contains(name)) {
                        result.success = true;
                        result.missionName = name;
                        result.patchName = model.patchName;
                        result.patchVersion = model.patchVersion;
                        result.currentQuestIndex = entry.getValue().indexOf(name) + 1;
                        result.totalQuestSize = entry.getValue().size() + questModels.get(1).quests.size();
                        result.calc();
                        canBreak = true;
                        break;
                    }
                }

                if (canBreak)
                    break;
            } else {
                if (model.quests.contains(name)) {
                    result.success = true;
                    result.missionName = name;
                    result.patchName = model.patchName;
                    result.patchVersion = model.patchVersion;
                    result.currentQuestIndex = model.quests.indexOf(name) + 1;
                    result.totalQuestSize = model.quests.size();
                    result.calc();
                    break;
                }
            }
        }

        return result;
    }
}
