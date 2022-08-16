package me.yarukon.thread.impl;

import me.yarukon.EventFactory;
import me.yarukon.Values;
import me.yarukon.node.Node;
import me.yarukon.node.NodeManager;
import me.yarukon.node.impl.DateNode;
import me.yarukon.node.impl.TimeNode;
import me.yarukon.thread.ProcessThread;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.Map;

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