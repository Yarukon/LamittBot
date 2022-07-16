package me.yarukon.utils;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Pattern;

public class DaXueXiUtil {
    // 拿取大学习ID
    public static void getIDs(Group groupIn) throws Exception {
        Document mainDoc = Jsoup.connect("https://news.cyol.com/gb/channels/vrGlAKDl/index.html").get();
        Element movieList = mainDoc.getElementsByClass("movie-list").first();
        Elements list = movieList.getElementsByTag("li");

        StringBuilder sb = new StringBuilder();

        int size = 0;
        for (Element ele : list) {
            if (size == 5)
                break;

            String id = ele.select("a").first().attr("href").replace("http://", "https://").replace("https://h5.cyol.com/special/daxuexi/", "");
            id = id.substring(0, id.indexOf("/"));
            String date = ele.select("a").first().select("img").attr("data-src").replace("http://", "https://").replace("https://pic.cyol.com/img/", "");
            date = date.substring(0, date.indexOf("/"));
            sb.append("青年大学习第 ").append(date).append(" 期 - ").append(id).append("\n");

            size++;
        }

        MessageChain mc = new MessageChainBuilder().append("青年大学习ID列表:\n").append(sb.toString()).build();
        groupIn.sendMessage(mc);
    }

    public static void getAnswer(String classID, Group groupIn) throws Exception {
        Document doc = Jsoup.connect("https://h5.cyol.com/special/daxuexi/" + classID + "/m.html").get();
        Elements eles = doc.getElementsByAttributeValueMatching("class", Pattern.compile("^section"));

        boolean inVideo = true;
        int questionIndex = 1;

        StringBuilder sb = new StringBuilder();

        for(Element ele : eles) {
            String clsName = ele.className();
            if (clsName.equals("section00") || clsName.endsWith("topindex")) continue;

            if((clsName.split(" ")[0].length() < 9 || clsName.split(" ").length == 2) && ele.getElementsByClass("start_btn").size() == 0 && ele.getElementsByClass("w0").size() != 0) {
                if(ele.getElementsByClass("continue").size() == 0) {
                    inVideo = false;
                    questionIndex = 1;
                    sb.append("\n");
                    continue;
                }

                StringBuilder answer = new StringBuilder();
                for(int i = 0; i < ele.children().size(); ++i) {
                    Element ans = ele.child(i);
                    String[] cclsName = ans.className().split(" ");
                    if(cclsName.length == 2 && cclsName[0].length() == 2 && ans.attr("data-a").equals("1")) {
                        answer.append((char) (64 + i));
                    }
                }

                sb.append(inVideo ? "课内" : "课后").append("第").append(questionIndex).append("题: ").append(answer).append("\n");

                questionIndex++;
            }
        }

        MessageChain mc = new MessageChainBuilder().append("青年大学习ID ").append(classID).append(" 答案:\n").append(sb.toString()).build();
        groupIn.sendMessage(mc);
    }
}
