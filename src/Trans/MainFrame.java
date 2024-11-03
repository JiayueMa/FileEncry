package Trans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MainFrame extends JFrame {
    private ClientController controller;
    private JTextField uploadFileField;
    private JButton selectUploadFileButton;
    private JButton uploadButton;

    private JTextField downloadFileField;
    private JTextField saveDirField;
    private JButton selectSaveDirButton;
    private JButton downloadButton;

    private JProgressBar progressBar;
    private JLabel statusLabel;

    public MainFrame(ClientController controller) {
        this.controller = controller;

        setTitle("文件传输客户端");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 上传部分
        uploadFileField = new JTextField(20);
        uploadFileField.setEditable(false);
        selectUploadFileButton = new JButton("选择文件");
        uploadButton = new JButton("上传");

        JPanel uploadPanel = new JPanel();
        uploadPanel.setBorder(BorderFactory.createTitledBorder("上传文件"));
        uploadPanel.add(uploadFileField);
        uploadPanel.add(selectUploadFileButton);
        uploadPanel.add(uploadButton);

        // 下载部分
        downloadFileField = new JTextField(15);
        saveDirField = new JTextField(15);
        saveDirField.setEditable(false);
        selectSaveDirButton = new JButton("选择路径");
        downloadButton = new JButton("下载");

        JPanel downloadPanel = new JPanel();
        downloadPanel.setBorder(BorderFactory.createTitledBorder("下载文件"));
        downloadPanel.add(new JLabel("文件名："));
        downloadPanel.add(downloadFileField);
        downloadPanel.add(new JLabel("保存到："));
        downloadPanel.add(saveDirField);
        downloadPanel.add(selectSaveDirButton);
        downloadPanel.add(downloadButton);

        // 进度条和状态
        progressBar = new JProgressBar();
        statusLabel = new JLabel("就绪");

        // 布局
        setLayout(new BorderLayout());
        add(uploadPanel, BorderLayout.NORTH);
        add(downloadPanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.SOUTH);

        // 事件处理
        selectUploadFileButton.addActionListener(this::selectUploadFile);
        uploadButton.addActionListener(this::uploadFile);

        selectSaveDirButton.addActionListener(this::selectSaveDirectory);
        downloadButton.addActionListener(this::downloadFile);
    }

    private void selectUploadFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(MainFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            uploadFileField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void uploadFile(ActionEvent e) {
        String filePath = uploadFileField.getText();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "请选择要上传的文件", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new Thread(() -> {
            try {
                statusLabel.setText("正在上传...");
                progressBar.setIndeterminate(true);
                controller.uploadFile(filePath);
                progressBar.setIndeterminate(false);
                statusLabel.setText("上传完成");
                JOptionPane.showMessageDialog(MainFrame.this, "文件上传成功", "信息", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                progressBar.setIndeterminate(false);
                statusLabel.setText("上传失败");
                JOptionPane.showMessageDialog(MainFrame.this, "文件上传失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private void selectSaveDirectory(ActionEvent e) {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = dirChooser.showOpenDialog(MainFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = dirChooser.getSelectedFile();
            saveDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void downloadFile(ActionEvent e) {
        String fileName = downloadFileField.getText();
        String saveDir = saveDirField.getText();
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "请输入要下载的文件名", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (saveDir.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "请选择保存路径", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new Thread(() -> {
            try {
                statusLabel.setText("正在下载...");
                progressBar.setIndeterminate(true);
                controller.downloadFile(fileName, saveDir);
                progressBar.setIndeterminate(false);
                statusLabel.setText("下载完成");
                JOptionPane.showMessageDialog(MainFrame.this, "文件下载成功", "信息", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                progressBar.setIndeterminate(false);
                statusLabel.setText("下载失败");
                JOptionPane.showMessageDialog(MainFrame.this, "文件下载失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }
}
