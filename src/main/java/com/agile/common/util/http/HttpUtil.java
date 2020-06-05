package com.agile.common.util.http;

import com.agile.common.constant.Constant;
import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.map.MapUtil;
import com.agile.common.util.object.ObjectUtil;
import com.alibaba.fastjson.JSON;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 佟盟
 * 日期 2019/11/7 18:11
 * 描述 Http工具
 * @version 1.0
 * @since 1.0
 */
public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";
    private static final String CONTENT_TYPE = "Content-type";
    private static final String CONNECTION = "Connection";

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url) {
        return send(Protocol.extract(url), RequestMethod.Get, url, null, null);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url, Object header) {
        return send(Protocol.extract(url), RequestMethod.Get, url, header, null);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.Get, url, header, param);
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String post(String url) {
        return send(Protocol.extract(url), RequestMethod.Post, url, null, null);
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String post(String url, Object param) {
        return send(Protocol.extract(url), RequestMethod.Post, url, null, param);
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String post(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.Post, url, header, param);
    }

    /**
     * put请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String put(String url) {
        return send(Protocol.extract(url), RequestMethod.Put, url, null, null);
    }

    /**
     * put请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String put(String url, Object param) {
        return send(Protocol.extract(url), RequestMethod.Put, url, null, param);
    }

    /**
     * put请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String put(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.Put, url, header, param);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String delete(String url) {
        return send(Protocol.extract(url), RequestMethod.Delete, url, null, null);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String delete(String url, Object param) {
        return send(Protocol.extract(url), RequestMethod.Delete, url, null, param);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String delete(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.Delete, url, header, param);
    }

    /**
     * 发送请求
     *
     * @param protocol 协议
     * @param method   方法
     * @param url      地址
     * @param header   请求头
     * @param param    参数
     * @return 相应信息
     */
    public static CloseableHttpResponse originalSend(Protocol protocol, RequestMethod method, String url, Object header, Object param) {
        try {
            CloseableHttpClient httpClient = getHttpClient(protocol);

            HttpRequestBase httpRequestBase = getHttpRequestBase(method);

            parseHeader(header, httpRequestBase);

            url = parseParam(url, param, httpRequestBase);

            url = parseUrl(protocol, url);

            httpRequestBase.setURI(URI.create(url));

            return httpClient.execute(httpRequestBase);

        }catch (NotFoundRequestMethodException e){
            logger.error("第二个参数 method 未成功分析出请求方式", e);
        }catch (Exception e) {
            logger.error("请求失败", e);
        }
        return null;
    }

    /**
     * 发送请求
     *
     * @param protocol 协议
     * @param method   方法
     * @param url      地址
     * @param header   请求头
     * @param param    参数
     * @return 相应信息
     */
    public static String send(Protocol protocol, RequestMethod method, String url, Object header, Object param) {
        try (CloseableHttpResponse response = originalSend(protocol, method, url, header, param)) {
            return toStringContent(response);
        } catch (Exception e) {
            logger.error("请求失败", e);
        }
        return null;
    }

    /**
     * 处理请求参数
     *
     * @param url             请求路径
     * @param param           参数
     * @param httpRequestBase 请求体
     * @return 处理后的url
     */
    private static String parseParam(String url, Object param, HttpRequestBase httpRequestBase) {
        if (param != null && httpRequestBase instanceof HttpEntityEnclosingRequestBase) {
            StringEntity entity = new StringEntity(JSON.toJSONString(param), StandardCharsets.UTF_8);
            ((HttpEntityEnclosingRequestBase) httpRequestBase).setEntity(entity);
        } else if (param != null) {
            Map<String, Object> paramMap = ObjectUtil.to(param, new TypeReference<Map<String, Object>>() {
            });
            String paramUrl = MapUtil.toUrl(paramMap);
            if (url.contains(Constant.RegularAbout.QUESTION_MARK)) {
                url += paramUrl;
            } else {
                url += (Constant.RegularAbout.QUESTION_MARK + paramUrl);
            }
        }
        return url;
    }

    /**
     * 处理请求头
     *
     * @param header          请求头
     * @param httpRequestBase 请求体
     */
    private static void parseHeader(Object header, HttpRequestBase httpRequestBase) {
        Map<String, Object> map = ObjectUtil.to(header, new TypeReference<Map<String, Object>>() {
        });
        if (map instanceof Header) {
            httpRequestBase.setHeader((Header) map);
        } else if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                httpRequestBase.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
            if (httpRequestBase.getHeaders(CONTENT_TYPE) == null) {
                httpRequestBase.setHeader(CONTENT_TYPE, "application/json; charset=utf-8");
            }
            if (httpRequestBase.getHeaders(CONNECTION) == null) {
                httpRequestBase.setHeader(CONNECTION, "Close");
            }
        }
    }

    /**
     * 处理请求体
     *
     * @param method 方法
     * @return 请求体
     * @throws NotFoundRequestMethodException 没找到对应的请求方式
     */
    private static HttpRequestBase getHttpRequestBase(RequestMethod method) throws NotFoundRequestMethodException {
        HttpRequestBase httpRequestBase;
        switch (method) {
            case Get:
                httpRequestBase = new HttpGet();
                break;
            case Put:
                httpRequestBase = new HttpPut();
                break;
            case Post:
                httpRequestBase = new HttpPost();
                break;
            case Delete:
                httpRequestBase = new HttpDelete();
                break;
            default:
                throw new NotFoundRequestMethodException();
        }
        return httpRequestBase;
    }

    /**
     * 处理请求地址
     *
     * @param protocol 请求协议
     * @param url      地址
     * @return 请求地址
     */
    private static String parseUrl(Protocol protocol, String url) {
        url = url.trim();
        String lowerCase = url.toLowerCase();
        if (Protocol.Https == protocol && !lowerCase.startsWith(HTTPS)) {
            url = HTTPS + url;
        } else if (Protocol.Http == protocol && !lowerCase.startsWith(HTTP)) {
            url = HTTP + url;
        }
        return url;
    }

    /**
     * 根据协议获取对应的HttpClient
     *
     * @param protocol 协议
     * @return HttpClient
     * @throws KeyManagementException   异常
     * @throws NoSuchAlgorithmException 异常
     */
    private static CloseableHttpClient getHttpClient(Protocol protocol) throws KeyManagementException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient;
        if (protocol == Protocol.Https) {
            httpClient = getHttpsClient();
        } else {
            httpClient = HttpClients.createDefault();
        }
        return httpClient;
    }

    /**
     * 创建httpsClient
     *
     * @return 返回httpsClient
     * @throws KeyManagementException   异常
     * @throws NoSuchAlgorithmException 异常
     */
    public static CloseableHttpClient getHttpsClient() throws KeyManagementException, NoSuchAlgorithmException {
        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(createIgnoreVerifySSL()))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClients.custom().setConnectionManager(connManager);
        return HttpClients.custom().setConnectionManager(connManager).build();
    }

    /**
     * 创建证书验证方式
     *
     * @return sslContext
     * @throws KeyManagementException   异常
     * @throws NoSuchAlgorithmException 异常
     */
    public static SSLContext createIgnoreVerifySSL() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        X509TrustManager trustManager = new X509TrustManager() {
            //该方法检查客户端的证书，若不信任该证书则抛出异常。由于我们不需要对客户端进行认证，
            //因此我们只需要执行默认的信任管理器的这个方法。JSSE中，默认的信任管理器类为TrustManager。
            @Override
            public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) {
            }

            //该方法检查服务器的证书，若不信任该证书同样抛出异常。通过自己实现该方法，可以使之信任我们指定的任何证书。
            //在实现该方法时，也可以简单的不做任何处理，即一个空的函数体，由于不会抛出异常，它就会信任任何证书。
            @Override
            public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) {
            }

            //返回受信任的X509证书数组。
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        return sslContext;
    }

    /**
     * 读取响应流
     *
     * @param response 响应
     * @return 响应数据
     */
    private static String toStringContent(HttpResponse response) {
        if (response == null) {
            return null;
        }
        StringBuilder temp = new StringBuilder();

        try (
                InputStreamReader re = new InputStreamReader(response.getEntity().getContent());
                BufferedReader bufferedReader = new BufferedReader(re);
        ) {
            String line;
            String newLine = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                temp.append(line).append(newLine);
            }
        } catch (IOException e) {
            logger.error("响应流读取失败", e);
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
