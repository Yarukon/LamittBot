package me.yarukon.node.impl;

import me.yarukon.BotMain;
import me.yarukon.node.Node;
import me.yarukon.utils.BotUtils;

import java.util.Calendar;

public class DateNode extends Node {
    public final int startMonth;
    public final int startDay;
    public final int endMonth;
    public final int endDay;

    public DateNode(String nodeName, int month, int day) {
        this(nodeName, month, day, month, day);
    }

    public DateNode(String nodeName, int startMonth, int startDay, int endMonth, int endDay) {
        super(nodeName);
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }

    public boolean isCurrentDate() {
        Calendar c = Calendar.getInstance();
        return BotUtils.isBetween(c.get(Calendar.MONTH) + 1, startMonth, endMonth, true) && BotUtils.isBetween(c.get(Calendar.DAY_OF_MONTH), startDay, endDay, true);
    }
}
