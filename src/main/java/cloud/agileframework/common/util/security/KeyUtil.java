package cloud.agileframework.common.util.security;

import cloud.agileframework.common.util.bytes.ByteUtil;
import cloud.agileframework.common.util.file.FileUtil;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.function.Function;

/**
 * @author 佟盟
 * 日期 2020-12-02 15:36
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class KeyUtil {
    private static final String UNENCRYPTED_KEY_HEADER = "-----BEGIN KEY-----";

    private static final String UNENCRYPTED_KEY_FOOTER = "-----END KEY-----";

    private static final String ENCRYPTED_KEY_HEADER = "-----BEGIN ENCRYPTED KEY-----";

    private static final String ENCRYPTED_KEY_FOOTER = "-----END ENCRYPTED KEY-----";

    private static final String UNENCRYPTED_PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";

    private static final String UNENCRYPTED_PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";

    private static final String ENCRYPTED_PRIVATE_KEY_HEADER = "-----BEGIN ENCRYPTED PRIVATE KEY-----";

    private static final String ENCRYPTED_PRIVATE_KEY_FOOTER = "-----END ENCRYPTED PRIVATE KEY-----";

    private static final String UNENCRYPTED_PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";

    private static final String UNENCRYPTED_PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";

    private static final String ENCRYPTED_PUBLIC_KEY_HEADER = "-----BEGIN ENCRYPTED PUBLIC KEY-----";

    private static final String ENCRYPTED_PUBLIC_KEY_FOOTER = "-----END ENCRYPTED PUBLIC KEY-----";


    /**
     * key转为字符串
     *
     * @param key     要转换的key
     * @param encrypt 是否加密
     * @return 密钥的字符串
     */
    public static String toString(Key key, boolean encrypt) {
        byte[] bytes = key.getEncoded();

        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (encrypt) {
            return ByteUtil.encryptionToString(bytes);
        } else {
            return ByteUtil.toBase64(bytes);
        }
    }

    /**
     * 读密钥文件
     *
     * @param file 公钥或私钥或公私钥在一起的文件
     * @return 解析出对应的密钥或密钥对
     */
    @SneakyThrows
    public static Key readFile(File file, Function<byte[], Key> byteToKey) {
        String content = FileUtil.readFileToString(file, StandardCharsets.UTF_8);
        return contentToKey(content, byteToKey);
    }

    /**
     * 翻译密钥文件内容
     *
     * @param content 等待翻译的内容
     * @return 翻译后的内容
     */
    public static Key contentToKey(String content, Function<byte[], Key> byteToKey) {
        byte[] contentByte;
        if (content.contains(UNENCRYPTED_PRIVATE_KEY_HEADER)) {
            String privateContent = getContent(content, UNENCRYPTED_PRIVATE_KEY_HEADER, UNENCRYPTED_PRIVATE_KEY_FOOTER);
            contentByte = ByteUtil.toByte(privateContent);
        } else if (content.contains(ENCRYPTED_PRIVATE_KEY_HEADER)) {
            String privateContent = getContent(content, ENCRYPTED_PRIVATE_KEY_HEADER, ENCRYPTED_PRIVATE_KEY_FOOTER);
            contentByte = ByteUtil.decrypt(privateContent);
        } else if (content.contains(UNENCRYPTED_PUBLIC_KEY_HEADER)) {
            String publicContent = getContent(content, UNENCRYPTED_PUBLIC_KEY_HEADER, UNENCRYPTED_PUBLIC_KEY_FOOTER);
            contentByte = ByteUtil.toByte(publicContent);
        } else if (content.contains(ENCRYPTED_PUBLIC_KEY_HEADER)) {
            String publicContent = getContent(content, ENCRYPTED_PUBLIC_KEY_HEADER, ENCRYPTED_PUBLIC_KEY_FOOTER);
            contentByte = ByteUtil.decrypt(publicContent);
        } else if (content.contains(UNENCRYPTED_KEY_HEADER)) {
            String publicContent = getContent(content, UNENCRYPTED_KEY_HEADER, UNENCRYPTED_KEY_FOOTER);
            contentByte = ByteUtil.toByte(publicContent);
        } else if (content.contains(ENCRYPTED_KEY_HEADER)) {
            String publicContent = getContent(content, ENCRYPTED_KEY_HEADER, ENCRYPTED_KEY_FOOTER);
            contentByte = ByteUtil.decrypt(publicContent);
        } else {
            return null;
        }
        return byteToKey.apply(contentByte);
    }

    /**
     * 从一段字符串内容中，以start开头以end结尾，截取中间的部分
     *
     * @param content 内容
     * @param start   开始标识
     * @param end     结束标识
     * @return 开始标识和结束标识中间的字符串内容
     */
    private static String getContent(String content, String start, String end) {
        return content.substring(content.indexOf(start) + start.length(), content.indexOf(end))
                .replaceAll("\\s", "");
    }

    /**
     * 根据密钥创建密钥文件中的内容
     *
     * @param key     密钥
     * @param encrypt 是否加密
     * @return 密钥转换为可记录的字符串
     */
    public static String toContent(Key key, boolean encrypt) {
        String content = toString(key, encrypt);
        StringBuilder builder = new StringBuilder();
        if (key instanceof PrivateKey && encrypt) {
            format(content, builder, ENCRYPTED_PRIVATE_KEY_HEADER, ENCRYPTED_PRIVATE_KEY_FOOTER);
        } else if (key instanceof PrivateKey) {
            format(content, builder, UNENCRYPTED_PRIVATE_KEY_HEADER, UNENCRYPTED_PRIVATE_KEY_FOOTER);
        } else if (key instanceof PublicKey && encrypt) {
            format(content, builder, ENCRYPTED_PUBLIC_KEY_HEADER, ENCRYPTED_PUBLIC_KEY_FOOTER);
        } else if (key instanceof PublicKey) {
            format(content, builder, UNENCRYPTED_PUBLIC_KEY_HEADER, UNENCRYPTED_PUBLIC_KEY_FOOTER);
        } else if (encrypt) {
            format(content, builder, ENCRYPTED_KEY_HEADER, ENCRYPTED_KEY_FOOTER);
        } else {
            format(content, builder, UNENCRYPTED_KEY_HEADER, UNENCRYPTED_KEY_FOOTER);
        }
        return builder.toString();
    }

    /**
     * 格式化
     *
     * @param content 中间内容
     * @param builder 容器
     * @param start   开始
     * @param end     结束
     */
    private static void format(String content, StringBuilder builder, String start, String end) {
        builder.append(start);
        builder.append("\n");
        builder.append(content);
        builder.append("\n");
        builder.append(end);
    }


    /**
     * 加密
     *
     * @param text      明文
     * @param key       密钥
     * @param algorithm 算法
     * @return 密文
     */
    @SneakyThrows
    public static byte[] encrypt(byte[] text, Key key, String algorithm) {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(text);
    }

    /**
     * 解密
     *
     * @param text      密文
     * @param key       密钥
     * @param algorithm 算法
     * @return 明文
     */
    @SneakyThrows
    public static byte[] decrypt(byte[] text, Key key, String algorithm) {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(text);
    }
}
