package com.xiangqi.client;

import com.xiangqi.client.multimedia.AudioManager;
import com.xiangqi.client.multimedia.ResourceManager;
import com.xiangqi.client.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 客户端主启动类
 * 作为象棋游戏客户端的入口点，处理命令行参数、初始化和资源加载
 */
public class ClientMain {
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());
    
    // 默认配置
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String DEFAULT_RESOURCE_PATH = "source";
    
    // 命令行参数
    private static String serverHost = DEFAULT_SERVER_HOST;
    private static int serverPort = DEFAULT_SERVER_PORT;
    private static String resourcePath = DEFAULT_RESOURCE_PATH;
    private static boolean debugMode = false;
    
    public static void main(String[] args) {
        try {
            // 解析命令行参数
            parseCommandLineArguments(args);
            
            // 设置日志级别
            if (debugMode) {
                Logger.getGlobal().setLevel(Level.ALL);
                logger.info("Debug mode enabled");
            }
            
            // 设置系统外观
            setSystemLookAndFeel();
            
            // 初始化资源管理器
            initializeResourceManager();
            
            // 初始化音频管理器
            initializeAudioManager();
            
            // 启动客户端应用程序
            SwingUtilities.invokeLater(() -> {
                try {
                    startApplication();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to start application", e);
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize client", e);
            System.exit(1);
        }
    }
    
    /**
     * 解析命令行参数
     */
    private static void parseCommandLineArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                case "--host":
                    if (i + 1 < args.length) {
                        serverHost = args[++i];
                    } else {
                        printUsageAndExit();
                    }
                    break;
                    
                case "-p":
                case "--port":
                    if (i + 1 < args.length) {
                        try {
                            serverPort = Integer.parseInt(args[++i]);
                            if (serverPort < 1 || serverPort > 65535) {
                                throw new NumberFormatException("Port out of range");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port number: " + args[i]);
                            printUsageAndExit();
                        }
                    } else {
                        printUsageAndExit();
                    }
                    break;
                    
                case "-r":
                case "--resources":
                    if (i + 1 < args.length) {
                        resourcePath = args[++i];
                    } else {
                        printUsageAndExit();
                    }
                    break;
                    
                case "-d":
                case "--debug":
                    debugMode = true;
                    break;
                    
                case "--help":
                    printUsageAndExit();
                    break;
                    
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printUsageAndExit();
            }
        }
        
        logger.info(String.format("Configuration: host=%s, port=%d, resources=%s, debug=%b", 
                   serverHost, serverPort, resourcePath, debugMode));
    }
    
    /**
     * 打印使用说明并退出
     */
    private static void printUsageAndExit() {
        System.out.println("象棋游戏客户端");
        System.out.println("用法: java -jar xiangqi-client.jar [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -h, --host <主机>     服务器主机地址 (默认: localhost)");
        System.out.println("  -p, --port <端口>     服务器端口号 (默认: 8888)");
        System.out.println("  -r, --resources <路径> 资源文件路径 (默认: source)");
        System.out.println("  -d, --debug          启用调试模式");
        System.out.println("  --help               显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar xiangqi-client.jar");
        System.out.println("  java -jar xiangqi-client.jar -h 192.168.1.100 -p 9999");
        System.out.println("  java -jar xiangqi-client.jar --debug");
        System.exit(0);
    }
    
    /**
     * 设置系统外观
     */
    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.info("System look and feel set successfully");
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.log(Level.WARNING, "Failed to set system look and feel", e);
        }
    }
    
    /**
     * 初始化资源管理器
     */
    private static void initializeResourceManager() {
        try {
            // ResourceManager自动初始化，只需获取实例
            ResourceManager.getInstance();
            logger.info("Resource manager initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize resource manager", e);
            throw new RuntimeException("Resource initialization failed", e);
        }
    }
    
    /**
     * 初始化音频管理器
     */
    private static void initializeAudioManager() {
        try {
            // AudioManager自动初始化，只需获取实例
            AudioManager.getInstance();
            logger.info("Audio manager initialized successfully");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to initialize audio manager", e);
            // 音频初始化失败不应该阻止应用程序启动
        }
    }
    
    /**
     * 启动应用程序
     */
    private static void startApplication() {
        logger.info("Starting Xiangqi client application");
        
        // 创建游戏客户端实例
        GameClient gameClient = new GameClient(serverHost, serverPort);
        
        // 创建并显示登录界面
        LoginFrame loginFrame = new LoginFrame();
        
        // 启动游戏客户端（它会自动设置登录监听器并显示登录界面）
        gameClient.start();
        
        // 登录界面将由GameClient.start()方法显示
        
        logger.info("Application started successfully");
    }
    
    /**
     * 获取服务器主机地址
     */
    public static String getServerHost() {
        return serverHost;
    }
    
    /**
     * 获取服务器端口
     */
    public static int getServerPort() {
        return serverPort;
    }
    
    /**
     * 获取资源路径
     */
    public static String getResourcePath() {
        return resourcePath;
    }
    
    /**
     * 是否为调试模式
     */
    public static boolean isDebugMode() {
        return debugMode;
    }
}