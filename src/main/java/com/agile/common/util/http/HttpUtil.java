package com.agile.common.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static java.lang.System.in;

/**
 * @author 佟盟
 * 日期 2019/11/7 18:11
 * 描述 Http工具
 * @version 1.0
 * @since 1.0
 */
public class HttpUtil {
    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = client.execute(get);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                return toStringContent(response);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private static String toStringContent(HttpResponse response) {
        StringBuilder temp = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line;
            String newLine = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                temp.append(line).append(newLine);
            }
            in.close();
        } catch (IOException ignored) {

        }
        return temp.toString();
    }

    public static boolean isIPv4(String str) {
        if (!Pattern.matches("[0-9]*[.][0-9]*[.][0-9]*[.][0-9]*", str)) {
            return false;
        } else {
            String[] arrays = str.split("\\.");

            return Integer.parseInt(arrays[0]) < 256 && arrays[0].length() <= 3
                    && Integer.parseInt(arrays[1]) < 256 && arrays[0].length() <= 3
                    && Integer.parseInt(arrays[2]) < 256 && arrays[0].length() <= 3
                    && Integer.parseInt(arrays[3]) < 256 && arrays[0].length() <= 3;
        }

    }

    public static boolean isIPv6(String str) {
        return Pattern.matches("[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][:][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]", str);
    }
}
