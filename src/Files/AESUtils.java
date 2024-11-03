package Files;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class AESUtils {
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128; // 位
    private static final int ITERATION_COUNT = 65536;

    // 生成密钥
    public static SecretKey generateKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    // 加密方法
    public static void encrypt(File inputFile, File outputFile, String password) throws Exception {
        // 生成随机盐值
        byte[] salt = SecureRandom.getSeed(16);

        SecretKey secretKey = generateKey(password, salt);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 将盐值写入输出文件开头
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(salt);
            try (FileInputStream fis = new FileInputStream(inputFile);
                 CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, read);
                }
            }
        }
    }

    // 解密方法
    public static void decrypt(File inputFile, File outputFile, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            // 读取盐值
            byte[] salt = new byte[16];
            fis.read(salt);

            SecretKey secretKey = generateKey(password, salt);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
        }
    }
}

