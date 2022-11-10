package me.yarukon.utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BotUtils {
    public static final Random rand = new Random();
    public static Gson gson = new Gson();
    public static String sendGet(String url, String param) throws Exception {
        URL obj = new URL(url + (param != null && !param.isEmpty() ? "?" + param : ""));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        con.setRequestMethod("GET"); // 请求方法为GET

        // Headers
        con.setRequestProperty("Accept", "application/json, text/plain");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36 Edg/100.0.1185.50");

        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String line;

        while ((line = br.readLine()) != null) {
            line = new String(line.getBytes());
            sb.append(line);
        }

        br.close();
        con.disconnect();

        return sb.toString();
    }

    public static boolean isBetween(int target, int min, int max, boolean withEquals) {
        return withEquals ? (target >= min && target <= max) : (target > min && target < max);
    }

}
