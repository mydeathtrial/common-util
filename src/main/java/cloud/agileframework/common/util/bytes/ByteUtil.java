package cloud.agileframework.common.util.bytes;

import java.util.Base64;

/**
 * @author 佟盟
 * 日期 2020-12-02 10:44
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ByteUtil {
    /**
     * 转base64
     *
     * @param key 比特数组
     * @return base64字符串
     */
    public static String toBase64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * base64转比特数组
     *
     * @param base64 base64字符串
     * @return 比特数组
     */
    public static byte[] toByte(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * byte数组加密
     *
     * @param key 要加密的byte数组
     * @return 密文
     */
    public static String encryptionToString(byte[] key) {
        return toBase64(encryption(key));
    }

    /**
     * 密文解密成byte数组
     *
     * @param text 要解密的密文
     * @return byte数组
     */
    public static byte[] decrypt(String text) {
        return decrypt(toByte(text));
    }

    /**
     * byte数组加密
     *
     * @param key 要被加密的byte数组
     * @return 加密后的byte数组
     */
    public static byte[] encryption(byte[] key) {
        byte[] newKey = new byte[key.length];
        for (int i = 0; i < key.length; i++) {
            byte node = key[i];
            newKey[i] = (byte) (node + i);
        }
        return newKey;
    }

    /**
     * byte数组解密
     *
     * @param key 加密的byte数组
     * @return 解密后的byte数组
     */
    public static byte[] decrypt(byte[] key) {
        byte[] newKey = new byte[key.length];
        for (int i = 0; i < key.length; i++) {
            byte node = key[i];
            newKey[i] = (byte) (node - i);
        }
        return newKey;
    }

}
