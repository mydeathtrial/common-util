package cloud.agileframework.common.util.http;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.map.MapUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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

    private static final String HTTPS_PREFIX = "https://";
    private static final String HTTP_PREFIX = "http://";

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url) {
        return send(Protocol.extract(url), RequestMethod.GET, url, null, null);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url, Object header) {
        return send(Protocol.extract(url), RequestMethod.GET, url, header, null);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String get(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.GET, url, header, param);
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String post(String url) {
        return send(Protocol.extract(url), RequestMethod.POST, url, null, null);
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String post(String url, Object param) {
        return send(Protocol.extract(url), RequestMethod.POST, url, null, param);
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String post(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.POST, url, header, param);
    }

    /**
     * put请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String put(String url) {
        return send(Protocol.extract(url), RequestMethod.PUT, url, null, null);
    }

    /**
     * put请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String put(String url, Object param) {
        return send(Protocol.extract(url), RequestMethod.PUT, url, null, param);
    }

    /**
     * put请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String put(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.PUT, url, header, param);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String delete(String url) {
        return send(Protocol.extract(url), RequestMethod.DELETE, url, null, null);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String delete(String url, Object param) {
        return send(Protocol.extract(url), RequestMethod.DELETE, url, null, param);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 返回的字符串
     */
    public static String delete(String url, Object header, Object param) {
        return send(Protocol.extract(url), RequestMethod.DELETE, url, header, param);
    }

    private static final Map<String, InetAddress> MAPPING = Maps.newHashMap();

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
    public static CloseableHttpResponse originalSend(Protocol protocol, String var0, RequestMethod method, String url, Object header, Object param) {
        final URI uri = URI.create(url);
        int max = 10;
        int count = max;
        while (count > 0) {
            try {
                CloseableHttpClient httpClient = getHttpClient(protocol, var0);

                HttpRequestBase httpRequestBase = getHttpRequestBase(method);

                if (!MAPPING.isEmpty()) {
                    httpRequestBase.setConfig(RequestConfig.custom().setLocalAddress(MAPPING.get(uri.getHost())).build());
                }

                parseHeader(header, httpRequestBase);

                url = parseParam(url, param, httpRequestBase);

                url = parseUrl(protocol, url);

                httpRequestBase.setURI(uri);

                return httpClient.execute(httpRequestBase);

            } catch (NotFoundRequestMethodException e) {
                logger.error("第二个参数 method 未成功分析出请求方式", e);
                return null;
            } catch (ConnectException e) {
                List<InetAddress> localIpAddress = getLocalIpAddress();
                final int index = max - count;
                if (index > localIpAddress.size() - 1) {
                    logger.error("网络连接异常", e);
                    return null;
                }
                MAPPING.put(uri.getHost(), localIpAddress.get(index));
                count--;
            } catch (Exception e) {
                logger.error("请求失败", e);
                return null;
            }
        }
        return null;
    }

    /**
     * 获取本机的所有ip
     *
     * @return 本机ip列表
     */
    public static List<InetAddress> getLocalIpAddress() {
        List<InetAddress> ipList = new ArrayList<>();
        Enumeration<?> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return ipList;
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
            Enumeration<?> ipAddressEnum = ni.getInetAddresses();
            while (ipAddressEnum.hasMoreElements()) {
                InetAddress address = (InetAddress) ipAddressEnum.nextElement();
                if (address.isLoopbackAddress() || address.isLinkLocalAddress()) {
                    continue;
                }
                ipList.add(address);
            }
        }


        return ipList;
    }

    public static String send(Protocol protocol, RequestMethod method, String url, Object header, Object param) {
        return send(protocol, SSLConnectionSocketFactory.SSL, method, url, header, param);
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
    public static String send(Protocol protocol, String var0, RequestMethod method, String url, Object header, Object param) {
        try (CloseableHttpResponse response = originalSend(protocol, var0, method, url, header, param)) {
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
            String content = param instanceof String ? (String) param : JSON.toJSONString(param);

            Header contentType = httpRequestBase.getLastHeader(HTTP.CONTENT_TYPE);
            if (contentType == null) {
                contentType = new BasicHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                httpRequestBase.setHeader(contentType);
            }

            Header connection = httpRequestBase.getLastHeader(HTTP.CONN_DIRECTIVE);
            if (connection == null) {
                connection = new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
                httpRequestBase.setHeader(connection);
            }

            Header contentEncoding = httpRequestBase.getLastHeader(HTTP.CONTENT_ENCODING);
            if (contentEncoding == null) {
                contentEncoding = new BasicHeader(HTTP.CONTENT_ENCODING, ContentType.APPLICATION_JSON.getCharset().name());
                httpRequestBase.setHeader(contentEncoding);
            }

            StringEntity entity = new StringEntity(content, ContentType.parse(contentType.getValue()));
            entity.setContentType(contentType);
            entity.setContentEncoding(contentEncoding);
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

        if (header instanceof Header) {
            httpRequestBase.setHeader((Header) header);
        } else if (header != null) {
            Map<String, Object> map = ObjectUtil.to(header, new TypeReference<Map<String, Object>>() {
            });
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                httpRequestBase.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
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
            case GET:
                httpRequestBase = new HttpGet();
                break;
            case PUT:
                httpRequestBase = new HttpPut();
                break;
            case POST:
                httpRequestBase = new HttpPost();
                break;
            case DELETE:
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
        if (Protocol.Https == protocol && !lowerCase.startsWith(HTTPS_PREFIX)) {
            url = HTTPS_PREFIX + url;
        } else if (Protocol.Http == protocol && !lowerCase.startsWith(HTTP_PREFIX)) {
            url = HTTP_PREFIX + url;
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
    private static CloseableHttpClient getHttpClient(Protocol protocol, String var0) throws KeyManagementException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient;
        if (protocol == Protocol.Https) {
            httpClient = getHttpsClient(var0);
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
    public static CloseableHttpClient getHttpsClient(String var0) throws KeyManagementException, NoSuchAlgorithmException {
        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(createIgnoreVerifySSL(var0), NoopHostnameVerifier.INSTANCE))
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
    public static SSLContext createIgnoreVerifySSL(String var0) throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance(var0);
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
        String temp = null;

        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                temp = EntityUtils.toString(entity, "UTF-8");
            }

        } catch (IOException e) {
            logger.error("响应流读取失败", e);
        } finally {
            if (response instanceof CloseableHttpResponse) {
                try {
                    ((CloseableHttpResponse) response).close();
                } catch (IOException e) {
                    logger.error("响应流读取失败", e);
                }
            }
        }
        return temp;
    }

    /**
     * 是不是ipv4地址
     *
     * @param str 判断的字符串
     * @return true是
     */
    public static boolean isIPv4(String str) {
        final String ipRegex = "[0-9]*[.][0-9]*[.][0-9]*[.][0-9]*";
        if (!Pattern.matches(ipRegex, str)) {
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
