package cloud.agileframework.common.util.rsa;

import cloud.agileframework.common.util.bytes.ByteUtil;
import cloud.agileframework.common.util.security.KeyUtil;
import junit.framework.TestCase;
import lombok.SneakyThrows;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

public class RSAUtilTest extends TestCase {
    private static final KeyPair keyPair;

    static {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(1024);
        keyPair = keyPairGen.generateKeyPair();
    }

    @SneakyThrows
    public void testDecrypt() {

        if (RSAUtil.toKeyPair(RSAUtil.toString(keyPair)) == null) {
            throw new RuntimeException("解密密钥对失败了");
        }

    }

    public void testEncryption() {
        System.out.println(RSAUtil.toString(keyPair));
    }

    public void testTestEncryption() {
        System.out.println(KeyUtil.toContent(keyPair.getPublic(), true));
        System.out.println(KeyUtil.toContent(keyPair.getPublic(), false));
        System.out.println(KeyUtil.toContent(keyPair.getPrivate(), true));
        System.out.println(KeyUtil.toContent(keyPair.getPrivate(), false));
    }

    public void testTranslation() {
        if (!(KeyUtil.contentToKey(KeyUtil.toContent(keyPair.getPublic(), true), RSAUtil::toPublicKey) instanceof PublicKey) ||
                !(KeyUtil.contentToKey(KeyUtil.toContent(keyPair.getPublic(), false), RSAUtil::toPublicKey) instanceof PublicKey) ||
                !(KeyUtil.contentToKey(KeyUtil.toContent(keyPair.getPrivate(), true), RSAUtil::toPrivateKey) instanceof PrivateKey) ||
                !(KeyUtil.contentToKey(KeyUtil.toContent(keyPair.getPrivate(), false), RSAUtil::toPrivateKey) instanceof PrivateKey)) {
            throw new RuntimeException("翻译密钥失败了");
        }
    }

    public void testCreateFileContent() {
        System.out.println(KeyUtil.toContent(keyPair.getPublic(), true));
        System.out.println(KeyUtil.toContent(keyPair.getPublic(), false));
        System.out.println(KeyUtil.toContent(keyPair.getPrivate(), true));
        System.out.println(KeyUtil.toContent(keyPair.getPrivate(), false));
    }

    public void test1() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {

        Map<String, Object> keyMap;
        String cipherText;
        // 原始明文
        String content = "春江潮水连海平，海上明月共潮生。滟滟随波千万里，何处春江无月明。";

        // 生成密钥对
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        PrivateKey privateKey;
        PublicKey publicKey;

//        KeyPair keyPair = keyPairGen.generateKeyPair();
//
//        PrivateKey privateKey = keyPair.getPrivate();
//        PublicKey publicKey = keyPair.getPublic();

//        System.out.println(KeyUtil.toString(privateKey,false));
//        System.out.println(KeyUtil.toString(publicKey,false));
//        privateKey = RSAUtil.toPrivateKey(ByteUtil.toByte(KeyUtil.toString(privateKey,false)));
//        publicKey = RSAUtil.toPublicKey(ByteUtil.toByte(KeyUtil.toString(publicKey,false)));

        privateKey = RSAUtil.toPrivateKey(ByteUtil.toByte("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCdyz0w6BCaxuR6Ovir+kn++JC7TfBidXxU66zr6ZVJltnHNp9VjZaZ8yBQttzEpKoOTcILb+4XOOHS1cwhgeYvMeKsxYyZ7nJAF9qluF7+PkMVz/acV027kUgOyQLvuCah+hFLdGFvh463ECv707YmvtxdComk0jYlj5lgDusLgAKSz0GGXkgd5HklmYhTBj5zEWxNYQOsGzq3Jwkc6TADBO9DnA2WWam4WQn4h4MXRe4AAQKZpZ4vpcTXz2rCykPVWkjP+8ti+0j698RWzsFOrirrCD6mjHdxO3LQbXuhTa4zgP2gUwBbL3akkKVcAH+t8HLAL/KAy6CSlDvhF9NZAgMBAAECggEACUAa7D0OjCRQzKGcZmib4eisg26EQyFkEo5masYczF2dksIvARL12zyXjmPJ+XU1yvTgBU+gg5gAFR4Xg3dcCTRBd6N1JKkH5Z4AYWq3luwLfsHcUToFApP3x3YdR/Rhv2krjmLaO3GU+kJmcbLtoMPbo1C01QbFydnrQehd+ySEbsP+WJWAhrr0PCcu1sN8Z2tvxVMCzUP/KcGXJMqwhjTghWXF6CopT+zTcmlN/xcGOMEmR5xDLpYQ0jthww+KSx2dEh4y39gzRe+RGRLOTjPCCDlpCo+lWS/h0vYDlpegk8Yt+BtpC1K7f/5MhIM2D0KHGpOOPX2+bBP0yAYJSQKBgQDPAeFClG8DY/6xA9Dyu73mAizDMU3C7DGkqIj0HmDH13yJmj5EEHoOOY36+kqXUiuzIoIEv+c1Pw2HkkmiBTmEpl87GFoCRQnIdpyJ5nhcYGS8M2V+basiZ0TZVsnoU7Z1Gm5d4igBcBueW14EK34ZVOpoKNr00MRtohVEq/0QawKBgQDDI5/2YK/k5NfbpeMJqtnKuJmNvWfd+4+82nYK+Vi+dyIinI2s4BauJX9F8txHB4h8LeVnFBPHgCuJFDOPinBGAL6KEgiZZ9VPESkNWlr3dDZ+T3XjzjkovWNGh2A9kvCyzstYS1X2m2aMArbtOqfPD9KTx4eTrEw8R6AXU00MSwKBgQDDHxSDO4KAgaq6k8xKYGiY+4fR3RFdIGGSwUGGSXQUKqu60GbMpXNHIT0RSwcwJ0YFrgs8Ct8Ws34/QcaiL6aNwHyXKVb+OLEGAbd/zHu7JykMtK2ARENBRHIDIuliUjXzn5xUZi4rXM7Lr3epGo3btszUrV3l0Tq11EeT2UHKTQKBgQC0T53Gng7J1T80IbOJQkqejDavBv4L2yA1JDQA7eg3joOHMdujiyjJ37Ib403xgB94hzw7JA0mweL4dicyknwt4xAtKn6xqYUr2zJwuTXnA4Y67peDWRHt+BncmMiv/K/WuUiiHHKhPXTjGLsIt7NJnFg63xsCtCl5ZcGi7AD8wwKBgEP/6cwitD6AQKyXoxgQR8KzUU7/RZHwSMRqLbL2LtdaLz/g/PJDnKaRtmV7IHc6XD0zM3pUFrGRxUlxhLH9FeaHGVYw5srh0xQfSUW5uTuojufpGhcxije+Uh3RHFcnk5GTWUHMm6AW7ntCesv9I0WYsDXXUbIgEeqcbEUy97Z6"));
        publicKey = RSAUtil.toPublicKey(ByteUtil.toByte("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAncs9MOgQmsbkejr4q/pJ/viQu03wYnV8VOus6+mVSZbZxzafVY2WmfMgULbcxKSqDk3CC2/uFzjh0tXMIYHmLzHirMWMme5yQBfapbhe/j5DFc/2nFdNu5FIDskC77gmofoRS3Rhb4eOtxAr+9O2Jr7cXQqJpNI2JY+ZYA7rC4ACks9Bhl5IHeR5JZmIUwY+cxFsTWEDrBs6tycJHOkwAwTvQ5wNllmpuFkJ+IeDF0XuAAECmaWeL6XE189qwspD1VpIz/vLYvtI+vfEVs7BTq4q6wg+pox3cTty0G17oU2uM4D9oFMAWy92pJClXAB/rfBywC/ygMugkpQ74RfTWQIDAQAB"));


        // 加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] tempBytes = cipher.doFinal(content.getBytes("UTF-8"));
        String miwen = ByteUtil.toBase64(tempBytes);
        System.out.println(miwen);

        // 解密
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        String plainText = new String(cipher.doFinal(ByteUtil.toByte(miwen)));
        System.out.println(plainText);
    }
}