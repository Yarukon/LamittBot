package me.yarukon.node;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.yarukon.BotMain;
import me.yarukon.node.impl.DateNode;
import me.yarukon.node.impl.StringNode;
import me.yarukon.node.impl.TimeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodeManager {
    public static NodeManager INSTANCE;
    public HashMap<String, ArrayList<Node>> nodes = new HashMap<>();

    public NodeManager(JsonObject objIn) {
        INSTANCE = this;
        load(objIn);
    }

    public void load(JsonObject objIn) {
        nodes.clear();

        for(Map.Entry<String, JsonElement> element : objIn.entrySet()) {
            ArrayList<Node> temp = new ArrayList<>();
            for(Map.Entry<String, JsonElement> subElement : element.getValue().getAsJsonObject().entrySet()) {
                String[] splited = subElement.getKey().split(":");
                switch (splited[0]) {
                    case "TIME":
                        String[] timeSplit = splited[1].split("-");
                        TimeNode timeNode = new TimeNode(subElement.getKey(), Integer.parseInt(timeSplit[0]), timeSplit.length == 1 ? Integer.parseInt(timeSplit[0]) : Integer.parseInt(timeSplit[1]));
                        for (JsonElement e : subElement.getValue().getAsJsonArray()) {
                            timeNode.strings.add(e.getAsString());
                        }
                        temp.add(timeNode);
                        break;

                    case "DATE":
                        String[] dateRangeSplit = splited[1].split("/");
                        DateNode dateNode;
                        if (dateRangeSplit.length > 1) {
                            String[] start = dateRangeSplit[0].split("-");
                            String[] end = dateRangeSplit[1].split("-");
                            dateNode = new DateNode(subElement.getKey(), Integer.parseInt(start[0]), Integer.parseInt(start[1]), Integer.parseInt(end[0]), Integer.parseInt(end[1]));
                        } else {
                            String[] dateSplit = splited[1].split("-");
                            dateNode = new DateNode(subElement.getKey(), Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[1]));
                        }

                        for (JsonElement e : subElement.getValue().getAsJsonArray()) {
                            dateNode.strings.add(e.getAsString());
                        }

                        temp.add(dateNode);
                        break;

                    default:
                        StringNode strNode = new StringNode(subElement.getKey());
                        for (JsonElement e : subElement.getValue().getAsJsonArray()) {
                            strNode.strings.add(e.getAsString());
                        }
                        temp.add(strNode);
                }
            }

            this.nodes.put(element.getKey(), temp);
        }

        BotMain.INSTANCE.getLogger().info("加载了 " + nodes.size() + " 个自动回复节点!");
    }

}
