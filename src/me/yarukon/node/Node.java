package me.yarukon.node;

import me.yarukon.utils.BotUtils;

import java.util.ArrayList;

public class Node {
    public final String nodeName;
    public final ArrayList<String> strings = new ArrayList<>();

    public Node(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getRandomStr() {
        return strings.get(BotUtils.rand.nextInt(strings.size()));
    }
}
