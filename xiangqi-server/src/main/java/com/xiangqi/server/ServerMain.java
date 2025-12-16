package com.xiangqi.server;

import com.xiangqi.server.network.GameServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务器主启动类
 * 作为象棋游戏服务器的入口点，支持配置文件和服务器监控管理
 */
public class ServerMain {
    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    
    // 默认配置
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_MAX_CONNECTIONS = 100;
    private static final int DEFAULT_THREAD_POOL_SIZE = 20;
    private static final String DEFAULT_CONFIG_FILE = "server.properties";
    
    // 服务器配置
    private static int serverPort = DEFAULT_PORT;
    private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private static int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private static String configFile = DEFAULT_CONFIG_FILE;
    private static boolean debugMode = false;
    private static boolean monitoringEnabled = true;
    
    // 服务器实例和监控
    private static GameServer gameServer;
    private static ScheduledExecutorService monitoringService;
    
    public static void main(String[] args) {
        try {
            // 解析命令行参数
            parseCommandLineArguments(args);
            
            // 设置日志级别
            if (debugMode) {
                Logger.getGlobal().setLevel(Level.ALL);
                logger.info("Debug mode enabled");
            }
            
            // 加载配置文件
            loadConfiguration();
            
            // 添加关闭钩子
            addShutdownHook();
            
            // 启动服务器
            startServer();
            
            // 启动监控服务
            if (monitoringEnabled) {
                startMonitoringService();
            }
            
            logger.info("Xiangqi server started successfully");
            
            // 保持主线程运行
            keepServerRunning();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start server", e);
            System.exit(1);
        }
    }
    
    /**
     * 解析命令行参数
     */
    private static void parseCommandLineArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
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
                    
                case "-c":
                case "--config":
                    if (i + 1 < args.length) {
                        configFile = args[++i];
                    } else {
                        printUsageAndExit();
                    }
                    break;
                    
                case "-m":
                case "--max-connections":
                    if (i + 1 < args.length) {
                        try {
                            maxConnections = Integer.parseInt(args[++i]);
                            if (maxConnections < 1) {
                                throw new NumberFormatException("Max connections must be positive");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid max connections: " + args[i]);
                            printUsageAndExit();
                        }
                    } else {
                        printUsageAndExit();
                    }
                    break;
                    
                case "-t":
                case "--threads":
                    if (i + 1 < args.length) {
                        try {
                            threadPoolSize = Integer.parseInt(args[++i]);
                            if (threadPoolSize < 1) {
                                throw new NumberFormatException("Thread pool size must be positive");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid thread pool size: " + args[i]);
                            printUsageAndExit();
                        }
                    } else {
                        printUsageAndExit();
                    }
                    break;
                    
                case "-d":
                case "--debug":
                    debugMode = true;
                    break;
                    
                case "--no-monitoring":
                    monitoringEnabled = false;
                    break;
                    
                case "--help":
                    printUsageAndExit();
                    break;
                    
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printUsageAndExit();
            }
        }
        
        logger.info(String.format("Configuration: port=%d, maxConnections=%d, threads=%d, config=%s, debug=%b", 
                   serverPort, maxConnections, threadPoolSize, configFile, debugMode));
    }
    
    /**
     * 打印使用说明并退出
     */
    private static void printUsageAndExit() {
        System.out.println("象棋游戏服务器");
        System.out.println("用法: java -jar xiangqi-server.jar [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -p, --port <端口>           服务器端口号 (默认: 8888)");
        System.out.println("  -c, --config <文件>         配置文件路径 (默认: server.properties)");
        System.out.println("  -m, --max-connections <数量> 最大连接数 (默认: 100)");
        System.out.println("  -t, --threads <数量>        线程池大小 (默认: 20)");
        System.out.println("  -d, --debug                启用调试模式");
        System.out.println("  --no-monitoring            禁用监控服务");
        System.out.println("  --help                     显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar xiangqi-server.jar");
        System.out.println("  java -jar xiangqi-server.jar -p 9999 -m 200");
        System.out.println("  java -jar xiangqi-server.jar --debug --no-monitoring");
        System.exit(0);
    }
    
    /**
     * 加载配置文件
     */
    private static void loadConfiguration() {
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            logger.info("Configuration loaded from: " + configFile);
            
            // 从配置文件读取设置（命令行参数优先）
            if (serverPort == DEFAULT_PORT && props.containsKey("server.port")) {
                serverPort = Integer.parseInt(props.getProperty("server.port"));
            }
            
            if (maxConnections == DEFAULT_MAX_CONNECTIONS && props.containsKey("server.maxConnections")) {
                maxConnections = Integer.parseInt(props.getProperty("server.maxConnections"));
            }
            
            if (threadPoolSize == DEFAULT_THREAD_POOL_SIZE && props.containsKey("server.threadPoolSize")) {
                threadPoolSize = Integer.parseInt(props.getProperty("server.threadPoolSize"));
            }
            
            if (props.containsKey("server.debug")) {
                debugMode = debugMode || Boolean.parseBoolean(props.getProperty("server.debug"));
            }
            
            if (props.containsKey("server.monitoring")) {
                monitoringEnabled = monitoringEnabled && Boolean.parseBoolean(props.getProperty("server.monitoring"));
            }
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load configuration file: " + configFile + ", using defaults", e);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid number format in configuration file", e);
        }
    }
    
    /**
     * 启动服务器
     */
    private static void startServer() throws IOException {
        gameServer = new GameServer();
        gameServer.startServer(serverPort);
        logger.info("Game server started on port " + serverPort);
    }
    
    /**
     * 启动监控服务
     */
    private static void startMonitoringService() {
        monitoringService = Executors.newScheduledThreadPool(1);
        
        // 每30秒输出服务器状态
        monitoringService.scheduleAtFixedRate(() -> {
            try {
                logServerStatus();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error in monitoring service", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
        
        logger.info("Monitoring service started");
    }
    
    /**
     * 记录服务器状态
     */
    private static void logServerStatus() {
        if (gameServer != null) {
            int activeConnections = gameServer.getClientCount();
            int activeGames = gameServer.getGameCount();
            
            logger.info(String.format("Server Status - Connections: %d/%d, Active Games: %d", 
                       activeConnections, maxConnections, activeGames));
            
            // 如果连接数接近上限，发出警告
            if (activeConnections > maxConnections * 0.8) {
                logger.warning("High connection load: " + activeConnections + "/" + maxConnections);
            }
        }
    }
    
    /**
     * 添加关闭钩子
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            
            // 停止监控服务
            if (monitoringService != null && !monitoringService.isShutdown()) {
                monitoringService.shutdown();
                try {
                    if (!monitoringService.awaitTermination(5, TimeUnit.SECONDS)) {
                        monitoringService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    monitoringService.shutdownNow();
                }
            }
            
            // 停止游戏服务器
            if (gameServer != null) {
                gameServer.stopServer();
            }
            
            logger.info("Server shutdown complete");
        }));
    }
    
    /**
     * 保持服务器运行
     */
    private static void keepServerRunning() {
        try {
            // 等待服务器线程结束
            while (gameServer != null && gameServer.isRunning()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.info("Server interrupted, shutting down...");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取服务器端口
     */
    public static int getServerPort() {
        return serverPort;
    }
    
    /**
     * 获取最大连接数
     */
    public static int getMaxConnections() {
        return maxConnections;
    }
    
    /**
     * 获取线程池大小
     */
    public static int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    /**
     * 是否为调试模式
     */
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * 是否启用监控
     */
    public static boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }
}