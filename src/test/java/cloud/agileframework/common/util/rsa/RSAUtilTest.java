package cloud.agileframework.common.util.rsa;

import cloud.agileframework.common.util.security.KeyUtil;
import junit.framework.TestCase;
import lombok.SneakyThrows;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

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
}