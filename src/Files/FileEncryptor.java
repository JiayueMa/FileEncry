package Files;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class FileEncryptor extends JFrame {
    private JTextField inputFileField;
    private JTextField outputDirField;
    private JButton selectInputButton;
    private JButton selectOutputButton;
    private JButton encryptButton;
    private JButton decryptButton;
    private JPasswordField keyField;

    public FileEncryptor() {
        setTitle("文件加密工具");
        setSize(600, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置布局
        setLayout(new BorderLayout());

        // 输入文件选择区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("选择源文件或加密文件"));

        inputFileField = new JTextField();
        inputFileField.setEditable(false);
        selectInputButton = new JButton("选择文件");

        inputPanel.add(inputFileField, BorderLayout.CENTER);
        inputPanel.add(selectInputButton, BorderLayout.EAST);

        // 输出目录选择区域
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("选择输出文件保存位置"));

        outputDirField = new JTextField();
        outputDirField.setEditable(false);
        selectOutputButton = new JButton("选择目录");

        outputPanel.add(outputDirField, BorderLayout.CENTER);
        outputPanel.add(selectOutputButton, BorderLayout.EAST);

        // 密钥输入区域
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        keyField = new JPasswordField(20);
        keyPanel.add(new JLabel("加密密钥："));
        keyPanel.add(keyField);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        encryptButton = new JButton("加密");
        decryptButton = new JButton("解密");
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);

        // 组装界面
        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);
        add(keyPanel, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.SOUTH);

        // 绑定事件
        selectInputButton.addActionListener(e -> selectInputFile());
        selectOutputButton.addActionListener(e -> selectOutputDirectory());
        encryptButton.addActionListener(e -> encryptFile());
        decryptButton.addActionListener(e -> decryptFile());
    }

    private void selectInputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择源文件或加密文件");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            inputFileField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void selectOutputDirectory() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("选择输出文件保存位置");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = dirChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = dirChooser.getSelectedFile();
            outputDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void encryptFile() {
        String inputFilePath = inputFileField.getText();
        String outputDirPath = outputDirField.getText();
        String key = new String(keyField.getPassword());

        if (inputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择要加密的源文件！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (outputDirPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输出文件保存位置！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入加密密钥！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputDirPath, inputFile.getName() + ".enc");

        try {
            AESUtils.encrypt(inputFile, outputFile, key);
            JOptionPane.showMessageDialog(this, "加密完成！", "信息", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加密失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decryptFile() {
        String inputFilePath = inputFileField.getText();
        String outputDirPath = outputDirField.getText();
        String key = new String(keyField.getPassword());

        if (inputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择要解密的加密文件！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!inputFilePath.endsWith(".enc")) {
            JOptionPane.showMessageDialog(this, "请选择有效的加密文件（扩展名为.enc）！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (outputDirPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输出文件保存位置！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入加密密钥！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File inputFile = new File(inputFilePath);
        String outputFileName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".enc"));
        File outputFile = new File(outputDirPath, outputFileName);

        try {
            AESUtils.decrypt(inputFile, outputFile, key);
            JOptionPane.showMessageDialog(this, "解密完成！", "信息", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "解密失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // 设置外观样式，可选
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            FileEncryptor fe = new FileEncryptor();
            fe.setVisible(true);
        });
    }
}
