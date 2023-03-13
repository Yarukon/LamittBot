package me.yarukon.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class DaXueXiUtil {
    // 拿取大学习ID
    public static LinkedHashMap<String, String> getIDs() throws Exception {
        LinkedHashMap<String, String> temp = new LinkedHashMap<>();

        Document mainDoc = Jsoup.connect("https://news.cyol.com/gb/channels/vrGlAKDl/index.html").get();
        Element movieList = mainDoc.getElementsByClass("movie-list").first();
        Elements list = movieList.getElementsByTag("li");

        int size = 0;
        for (Element ele : list) {
            if (size == 5)
                break;

            String id = ele.select("a").first().attr("href").replace("http://", "https://").replace("https://h5.cyol.com/special/daxuexi/", "");
            id = id.substring(0, id.indexOf("/"));
            String date = ele.select("div").first().html();
            date = date.substring(date.lastIndexOf(">") + 1, date.lastIndexOf(">") + 11);

            temp.put(date, id);

            size++;
        }

        return temp;
    }

    public static String getAnswer(String classID) throws Exception {
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

        return sb.toString();
    }
}
