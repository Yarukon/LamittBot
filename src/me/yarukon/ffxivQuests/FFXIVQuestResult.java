package me.yarukon.ffxivQuests;

import java.text.DecimalFormat;

public class FFXIVQuestResult {

    public boolean success;
    public String missionName;
    public String patchName;
    public String patchVersion;
    public int currentQuestIndex;
    public int totalQuestSize;
    public float percentage;

    public FFXIVQuestResult(boolean success, String missionName, String patchName, String patchVersion, int currentQuestIndex, int totalQuestSize) {
        this.success = success;
        this.missionName = missionName;
        this.patchName = patchName;
        this.patchVersion = patchVersion;
        this.currentQuestIndex = currentQuestIndex;
        this.totalQuestSize = totalQuestSize;
    }

    public void calc() {
        this.percentage = this.currentQuestIndex / (float) this.totalQuestSize;
    }

    public String toString() {
        DecimalFormat df = new DecimalFormat("0.00");
        return this.success + " - " + this.missionName + " - " + this.patchName + " - " + this.patchVersion + " - " + this.currentQuestIndex + " - " + this.totalQuestSize + " - " + df.format((this.percentage * 100)) + "%";
    }
}
