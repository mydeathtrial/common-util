package cloud.agileframework.common.util.rsa;

import cloud.agileframework.common.util.bytes.ByteUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author 佟盟
 * 日期 2020-12-02 10:28
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class RSAUtil {
    private static final String RSA = "RSA";

    /**
     * 公钥长度分隔符
     */
    private static final String SPLIT = "$=ACs13fz/";

    /**
     * 密钥对密文解密
     *
     * @param text 密钥对密文
     * @return 密钥对
     */
    public static KeyPair toKeyPair(String text) {
        if (text == null || !text.contains(SPLIT)) {
            return null;
        }
        //密钥对密文
        String content = text.substring(0, text.indexOf(SPLIT));

        //公钥长度密文
        String publicLengthStr = text.substring(text.indexOf(SPLIT) + SPLIT.length());
        int publicLength = Integer.parseInt(publicLengthStr);

        //提取加密的密钥对字节数组
        byte[] keyPairBytes = ByteUtil.toByte(content);

        //解密后的公钥字节数组
        byte[] publicKeyBytes = ByteUtil.decrypt(ArrayUtils.subarray(
                keyPairBytes,
                keyPairBytes.length - publicLength,
                keyPairBytes.length));

        //解密后的私钥字节数组
        byte[] privateKeyBytes = ByteUtil.decrypt(ArrayUtils.subarray(
                keyPairBytes,
                0,
                keyPairBytes.length - publicLength));

        //创建密钥对
        return new KeyPair(toPublicKey(publicKeyBytes), toPrivateKey(privateKeyBytes));
    }

    /**
     * 密钥对加密
     *
     * @param keyPair 密钥对
     * @return 密文
     */
    public static String toString(KeyPair keyPair) {
        //加密公钥
        byte[] publicKeyBytes = ByteUtil.encryption(keyPair.getPublic().getEncoded());
        //加密私钥
        byte[] privateKeyBytes = ByteUtil.encryption(keyPair.getPrivate().getEncoded());

        //密钥对容器
        byte[] keyPairBytes = new byte[publicKeyBytes.length + privateKeyBytes.length];
        int index = 0;
        for (byte b : privateKeyBytes) {
            keyPairBytes[index++] = b;
        }
        for (byte b : publicKeyBytes) {
            keyPairBytes[index++] = b;
        }
        //编译密文字符串，拼接公钥长度，用于解密使用
        return ByteUtil.toBase64(keyPairBytes) + SPLIT + publicKeyBytes.length;
    }

    /**
     * 未经加密的公钥byte数组转换为公钥
     *
     * @param text 未经加密的公钥byte数组
     * @return 公钥
     */
    @SneakyThrows
    public static RSAPublicKey toPublicKey(byte[] text) {
        return (RSAPublicKey) KeyFactory.getInstance(RSA)
                .generatePublic(
                        new X509EncodedKeySpec(
                                text
                        )
                );
    }

    /**
     * 未经加密的私钥byte数组转换为私钥
     *
     * @param text 未经加密的私钥byte数组
     * @return 私钥
     */
    @SneakyThrows
    public static RSAPrivateKey toPrivateKey(byte[] text) {
        return (RSAPrivateKey) KeyFactory.getInstance(RSA)
                .generatePrivate(
                        new PKCS8EncodedKeySpec(
                                text
                        )
                );
    }
}
