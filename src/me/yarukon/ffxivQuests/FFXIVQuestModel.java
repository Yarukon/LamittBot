package me.yarukon.ffxivQuests;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class FFXIVQuestModel {

    public final String patchName;
    public final String patchVersion;
    public final boolean isPre;

    public LinkedHashMap<String, LinkedList<String>> preQuests;
    public LinkedList<String> quests;

    public FFXIVQuestModel(String patchName, String patchVersion, boolean isPre) {
        this.patchName = patchName;
        this.patchVersion = patchVersion;
        this.isPre = isPre;

        if (this.isPre)
            preQuests = new LinkedHashMap<>();
        else
            quests = new LinkedList<>();
    }

}
