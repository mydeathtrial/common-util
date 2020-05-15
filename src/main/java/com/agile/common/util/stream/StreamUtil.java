package com.agile.common.util.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author 佟盟
 * 日期 2020/5/15 15:00
 * 描述 流工具
 * @version 1.0
 * @since 1.0
 */
public class StreamUtil {
    private StreamUtil() {
    }

    private static final Log log = LogFactory.getLog(StreamUtil.class);

    /**
     * 输入流转字符串
     * @param inputStream 输入流
     * @return 字符串
     * @throws UnsupportedEncodingException 不支持的编码
     */
    public static String toString(InputStream inputStream) throws UnsupportedEncodingException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        toOutputStream(inputStream,result);
        return result.toString(System.getProperty("sun.jnu.encoding"));
    }

    /**
     * 输入流转输出流
     * @param inputStream 输入流
     * @param outputStream 输出流
     */
    public static void toOutputStream(InputStream inputStream, OutputStream outputStream) {
        try {
            final int length = 1024;
            byte[] buffer = new byte[length];
            int r;
            while ((r = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, r);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (IOException e){
            log.error("InputStream convert to OutputStream error", e);
        }
    }
}
