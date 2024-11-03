package Trans;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileTransferClient {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final String serverAddress = "127.0.0.1"; // 服务器地址
    private final int port = 5000; // 服务器端口

    public FileTransferClient() {
    }

    private boolean connect() throws IOException {
        socket = new Socket(serverAddress, port);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        // 用户认证
        return authenticate();
    }

    private boolean authenticate() throws IOException {
        String serverMessage = dis.readUTF();
        if (serverMessage.equals("USERNAME")) {
            // 输入用户名
            String username = getUserInput("请输入用户名：");
            dos.writeUTF(username);
        }

        serverMessage = dis.readUTF();
        if (serverMessage.equals("PASSWORD")) {
            // 输入密码
            String password = getUserInput("请输入密码：");
            dos.writeUTF(password);
        }

        serverMessage = dis.readUTF();
        if (serverMessage.equals("AUTH_SUCCESS")) {
            System.out.println("认证成功");
            return true;
        } else {
            System.out.println("认证失败");
            socket.close();
            return false;
        }
    }

    public void uploadFile(String filePath) throws IOException {
        if (!connect()) {
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("文件不存在：" + filePath);
            socket.close();
            return;
        }
        dos.writeUTF("UPLOAD");
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int read;
        while ((read = fis.read(buffer)) > 0) {
            dos.write(buffer, 0, read);
        }
        fis.close();
        System.out.println("文件上传完成：" + file.getName());
        socket.close();
    }

    public void downloadFile(String fileName, String saveDir) throws IOException {
        if (!connect()) {
            return;
        }

        dos.writeUTF("DOWNLOAD");
        dos.writeUTF(fileName);

        String response = dis.readUTF();
        if (response.equals("ERROR")) {
            System.out.println("服务器上不存在文件：" + fileName);
            socket.close();
            return;
        }

        long fileSize = dis.readLong();
        File file = new File(saveDir + "/" + fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int read;
        long remaining = fileSize;
        while ((read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
            fos.write(buffer, 0, read);
            remaining -= read;
        }
        fos.close();
        System.out.println("文件下载完成：" + fileName);
        socket.close();
    }

    // 获取用户输入（控制台）
    private String getUserInput(String prompt) {
        System.out.print(prompt);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 密码哈希函数（如果需要在客户端对密码进行哈希，可以使用）
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            FileTransferClient client = new FileTransferClient();

            // 示例：上传文件
            client.uploadFile("path/to/local/file.txt");

            // 示例：下载文件
            // client.downloadFile("file.txt", "path/to/save/dir");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
