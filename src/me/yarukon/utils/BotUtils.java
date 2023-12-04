package me.yarukon.utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BotUtils {
    public static final Random rand = new Random();
    public static Gson gson = new Gson();

    public static boolean proxyValid = false;
    public static String proxyIP = "";
    public static short proxyPort = 0;

    public static String sendGet(String url, String param) throws Exception {
        return sendGet(url, param, false);
    }

    public static String sendGet(String url, String param, boolean useProxy) throws Exception {
        URL obj = new URL(url + (param != null && !param.isEmpty() ? "?" + param : ""));

        Proxy proxy = proxyValid ? new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyIP, proxyPort)) : null;
        HttpURLConnection con = (HttpURLConnection) (proxyValid ? obj.openConnection(proxy) : obj.openConnection());

        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        con.setRequestMethod("GET"); // 请求方法为GET

        // Headers
        con.setRequestProperty("Accept", "application/json, text/plain");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

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
