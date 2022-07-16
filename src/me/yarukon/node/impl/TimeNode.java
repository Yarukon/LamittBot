package me.yarukon.node.impl;

import me.yarukon.node.Node;
import me.yarukon.utils.BotUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeNode extends Node {

    public final int hourStart;
    public final int hourEnd;

    public TimeNode(String nodeName, int hourStart, int hourEnd) {
        super(nodeName);
        this.hourStart = hourStart;
        this.hourEnd = hourEnd;
    }

    public boolean isCurrentTime() {
        return BotUtils.isBetween(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), hourStart, hourEnd, true);
    }
}
