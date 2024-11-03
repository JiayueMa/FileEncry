package Trans;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class ClientController {
    private final String serverAddress = "127.0.0.1"; // 服务器地址
    private final int port = 5000; // 服务器端口
    private SSLSocket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;
    private String password;

    public ClientController() {
    }

    public boolean login(String username, String password) {
        this.username = username;
        this.password = password;
        try {
            if (!connect()) {
                return false;
            }
            socket.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean connect() throws Exception {
        // 加载信任库
        KeyStore trustStore = KeyStore.getInstance("JKS");
        FileInputStream trustStoreFile = new FileInputStream("clienttruststore.jks");
        trustStore.load(trustStoreFile, "password".toCharArray());

        // 初始化信任管理器
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);

        // 创建 SSL 上下文
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // 创建 SSL 套接字工厂
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        socket = (SSLSocket) ssf.createSocket(serverAddress, port);

        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        // 用户认证
        return authenticate();
    }

    private boolean authenticate() throws IOException {
        String serverMessage = dis.readUTF();
        if (serverMessage.equals("USERNAME")) {
            dos.writeUTF(username);
        }

        serverMessage = dis.readUTF();
        if (serverMessage.equals("PASSWORD")) {
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

    public void uploadFile(String filePath) throws Exception {
        if (!connect()) {
            throw new Exception("连接服务器失败");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            socket.close();
            throw new FileNotFoundException("文件不存在：" + filePath);
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

    public void downloadFile(String fileName, String saveDir) throws Exception {
        if (!connect()) {
            throw new Exception("连接服务器失败");
        }

        dos.writeUTF("DOWNLOAD");
        dos.writeUTF(fileName);

        String response = dis.readUTF();
        if (response.equals("ERROR")) {
            socket.close();
            throw new FileNotFoundException("服务器上不存在文件：" + fileName);
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
}
