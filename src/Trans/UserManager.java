package Trans;

import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class UserManager {
    public static void main(String[] args) {
        String username = "user1";
        String password = "password123";

        // 生成随机盐值
        String salt = generateSalt();
        // 计算密码哈希
        String hashedPassword = hashPassword(password, salt);

        // 将用户名、哈希和盐值写入 users.txt
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("users.txt", true))) {
            bw.write(username + ":" + hashedPassword + ":" + salt);
            bw.newLine();
            System.out.println("用户添加成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 生成随机盐值
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    // 计算密码哈希
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes("UTF-8")); // 添加盐值
            byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
            return bytesToHex(hashedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
