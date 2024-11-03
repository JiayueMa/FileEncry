package Trans;
import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class FileTransferServer {
    private SSLServerSocket serverSocket;
    private final int port = 5000; // 服务器监听的端口
    private Map<String, String> userMap; // 存储用户名和密码哈希及盐值

    public FileTransferServer() throws Exception {
        // 加载密钥库
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream keyStoreFile = new FileInputStream("serverkeystore.jks");
        keyStore.load(keyStoreFile, "password".toCharArray());

        // 初始化密钥管理器
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "password".toCharArray());

        // 创建 SSL 上下文
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // 创建 SSL 服务器套接字工厂
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

        System.out.println("SSL 服务器已启动，监听端口：" + port);

        // 加载用户数据
        loadUserData();
    }

    private void loadUserData() {
        userMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    userMap.put(parts[0], parts[1] + ":" + parts[2]); // 存储哈希和盐值
                }
            }
            System.out.println("用户数据加载完成");
        } catch (IOException e) {
            System.out.println("用户数据加载失败：" + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("客户端已连接：" + clientSocket.getInetAddress());
                // 为每个客户端创建一个处理线程
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 客户端处理类
    private class ClientHandler implements Runnable {
        private SSLSocket socket;
        private DataInputStream dis;
        private DataOutputStream dos;

        public ClientHandler(SSLSocket socket) throws IOException {
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                // 用户认证
                if (!authenticateUser()) {
                    socket.close();
                    return;
                }

                // 读取客户端发送的命令
                String command = dis.readUTF();
                if (command.equals("UPLOAD")) {
                    receiveFile();
                } else if (command.equals("DOWNLOAD")) {
                    sendFile();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean authenticateUser() throws IOException {
            dos.writeUTF("USERNAME");
            String username = dis.readUTF();

            dos.writeUTF("PASSWORD");
            String password = dis.readUTF();

            String storedData = userMap.get(username);
            if (storedData == null) {
                dos.writeUTF("AUTH_FAILED");
                System.out.println("认证失败：用户不存在");
                return false;
            }

            String[] parts = storedData.split(":");
            String hashedPassword = parts[0];
            String salt = parts[1];

            String hashedInputPassword = hashPassword(password, salt);
            if (hashedPassword.equals(hashedInputPassword)) {
                dos.writeUTF("AUTH_SUCCESS");
                System.out.println("用户 " + username + " 认证成功");
                return true;
            } else {
                dos.writeUTF("AUTH_FAILED");
                System.out.println("认证失败：密码错误");
                return false;
            }
        }

        private void receiveFile() throws IOException {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            File file = new File("server_files/" + fileName);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int read;
            long remaining = fileSize;
            while ((read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            fos.close();
            System.out.println("文件接收完成：" + fileName);
        }

        private void sendFile() throws IOException {
            String fileName = dis.readUTF();
            File file = new File("server_files/" + fileName);
            if (!file.exists()) {
                dos.writeUTF("ERROR");
                System.out.println("文件不存在：" + fileName);
                return;
            } else {
                dos.writeUTF("OK");
            }
            FileInputStream fis = new FileInputStream(file);
            long fileSize = file.length();
            dos.writeLong(fileSize);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }
            fis.close();
            System.out.println("文件发送完成：" + fileName);
        }
    }

    // 密码哈希函数，使用盐值
    private String hashPassword(String password, String salt) {
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
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    public static void main(String[] args) {
        try {
            new FileTransferServer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

