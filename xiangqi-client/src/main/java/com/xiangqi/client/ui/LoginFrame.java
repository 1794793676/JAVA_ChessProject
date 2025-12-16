package com.xiangqi.client.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录界面 - 提供用户认证功能
 * 实现需求 1.1, 1.2, 1.3
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private JLabel statusLabel;
    
    // 事件监听器接口
    public interface LoginListener {
        void onLoginAttempt(String username, String password);
    }
    
    private LoginListener loginListener;
    
    public LoginFrame() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("象棋游戏 - 登录");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        // 创建更大的输入框
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        
        // 设置输入框的大小 - 显著增大
        usernameField.setPreferredSize(new Dimension(350, 35));
        passwordField.setPreferredSize(new Dimension(350, 35));
        usernameField.setMinimumSize(new Dimension(350, 35));
        passwordField.setMinimumSize(new Dimension(350, 35));
        
        // 确保输入框可编辑
        usernameField.setEditable(true);
        passwordField.setEditable(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        
        // 设置边框使输入框更明显
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        
        // 添加提示文字
        usernameField.setToolTipText("输入任意用户名（不能为空）");
        passwordField.setToolTipText("输入任意密码（不能为空）");
        
        // 设置更大的字体
        Font inputFont = new Font("微软雅黑", Font.PLAIN, 16);
        usernameField.setFont(inputFont);
        passwordField.setFont(inputFont);
        
        // 设置背景色为白色
        usernameField.setBackground(Color.WHITE);
        passwordField.setBackground(Color.WHITE);
        
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(140, 40));
        loginButton.setFocusPainted(false);
        
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        
        statusLabel = new JLabel("<html><center>提示：用户名和密码可以是任意值<br>但都不能为空</center></html>");
        statusLabel.setForeground(new Color(0, 100, 200));
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
    }
    
    /**
     * 设置界面布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 标题
        JLabel titleLabel = new JLabel("象棋游戏登录", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 30, 10);
        mainPanel.add(titleLabel, gbc);
        
        // 用户名标签和输入框
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(10, 30, 10, 15);
        gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        mainPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 15, 10, 30);
        mainPanel.add(usernameField, gbc);
        
        // 密码标签和输入框
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 30, 10, 15);
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        mainPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 15, 10, 30);
        mainPanel.add(passwordField, gbc);
        
        // 登录按钮
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 20, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);
        
        // 状态标签
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 20, 5, 20);
        mainPanel.add(statusLabel, gbc);
        
        // 错误标签
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 20, 25, 20);
        mainPanel.add(errorLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // 回车键登录
        Action loginAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        };
        
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
    }
    
    /**
     * 执行登录操作
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 基本输入验证
        if (username.isEmpty()) {
            showError("请输入用户名");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            passwordField.requestFocus();
            return;
        }
        
        // 清除之前的错误信息
        clearError();
        setLoginEnabled(false);
        showStatus("正在登录...");
        
        // 通知监听器
        if (loginListener != null) {
            loginListener.onLoginAttempt(username, password);
        }
    }
    
    /**
     * 设置登录监听器
     */
    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }
    
    /**
     * 显示错误消息
     */
    public void showError(String message) {
        errorLabel.setText(message);
        statusLabel.setText(" ");
        setLoginEnabled(true);
    }
    
    /**
     * 清除错误消息
     */
    public void clearError() {
        errorLabel.setText(" ");
    }
    
    /**
     * 显示状态消息
     */
    public void showStatus(String message) {
        statusLabel.setText(message);
        errorLabel.setText(" ");
    }
    
    /**
     * 登录成功处理
     */
    public void onLoginSuccess() {
        showStatus("登录成功！");
        // 界面将被关闭或隐藏，由调用者处理
    }
    
    /**
     * 登录失败处理
     */
    public void onLoginFailure(String errorMessage) {
        showError(errorMessage != null ? errorMessage : "登录失败，请检查用户名和密码");
        setLoginEnabled(true);
        passwordField.setText("");
        passwordField.requestFocus();
    }
    
    /**
     * 设置登录按钮和输入框的启用状态
     */
    private void setLoginEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }
    
    /**
     * 获取输入的用户名
     */
    public String getUsername() {
        return usernameField.getText().trim();
    }
    
    /**
     * 获取输入的密码
     */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    /**
     * 清空输入字段
     */
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        clearError();
        showStatus("请输入用户名和密码");
        setLoginEnabled(true);
    }
    
    /**
     * 显示登录对话框
     */
    public void showLoginDialog() {
        clearFields();
        setVisible(true);
        usernameField.requestFocus();
    }
}