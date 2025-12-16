package com.xiangqi.server;

import com.xiangqi.server.network.GameServer;
import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.model.Player;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * Main server controller that starts and manages the game server.
 * Handles server configuration, logging, and administrative functions.
 * 实现需求 5.4, 8.1, 8.4
 */
public class GameServerMain {
    private static final Logger LOGGER = Logger.getLogger(GameServerMain.class.getName());
    private static final String CONFIG_FILE = "server.properties";
    private static final String LOG_FILE = "server.log";
    
    // Default configuration values
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_MAX_CLIENTS = 100;
    private static final boolean DEFAULT_ENABLE_LOGGING = true;
    private static final int DEFAULT_STATS_INTERVAL = 60; // seconds
    
    // Server components
    private GameServer gameServer;
    private Properties config;
    private ScheduledExecutorService scheduledExecutor;
    private volatile boolean running = false;
    
    // Configuration properties
    private int serverPort;
    private int maxClients;
    private boolean enableFileLogging;
    private int statsInterval;
    private String logLevel;
    
    /**
     * Main entry point for the server application.
     */
    public static void main(String[] args) {
        GameServerMain serverMain = new GameServerMain();
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered - stopping server gracefully");
            serverMain.shutdown();
        }));
        
        try {
            serverMain.initialize();
            serverMain.start();
            serverMain.runConsole();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error in server main", e);
            System.exit(1);
        }
    }
    
    /**
     * Initialize the server with configuration and logging.
     */
    public void initialize() throws IOException {
        System.out.println("Initializing Xiangqi Game Server...");
        
        // Load configuration
        loadConfiguration();
        
        // Setup logging
        setupLogging();
        
        // Initialize server components
        gameServer = new GameServer();
        scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        LOGGER.info("Server initialization completed");
        LOGGER.info("Configuration: port=" + serverPort + ", maxClients=" + maxClients + 
                   ", logging=" + enableFileLogging + ", statsInterval=" + statsInterval);
    }
    
    /**
     * Start the game server and monitoring services.
     */
    public void start() {
        if (running) {
            LOGGER.warning("Server is already running");
            return;
        }
        
        try {
            // Start the game server
            gameServer.startServer(serverPort);
            running = true;
            
            // Start monitoring and statistics
            startMonitoring();
            
            LOGGER.info("Xiangqi Game Server started successfully on port " + serverPort);
            System.out.println("Server is running on port " + serverPort);
            System.out.println("Type 'help' for available commands");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start server", e);
            throw new RuntimeException("Server startup failed", e);
        }
    }
    
    /**
     * Stop the server gracefully.
     */
    public void shutdown() {
        if (!running) {
            return;
        }
        
        LOGGER.info("Shutting down server...");
        running = false;
        
        // Stop scheduled tasks
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop game server
        if (gameServer != null) {
            gameServer.stopServer();
        }
        
        LOGGER.info("Server shutdown completed");
        System.out.println("Server stopped");
    }
    
    /**
     * Load server configuration from properties file.
     */
    private void loadConfiguration() {
        config = new Properties();
        
        // Set default values
        config.setProperty("server.port", String.valueOf(DEFAULT_PORT));
        config.setProperty("server.maxClients", String.valueOf(DEFAULT_MAX_CLIENTS));
        config.setProperty("logging.enabled", String.valueOf(DEFAULT_ENABLE_LOGGING));
        config.setProperty("logging.level", "INFO");
        config.setProperty("stats.interval", String.valueOf(DEFAULT_STATS_INTERVAL));
        
        // Try to load from file
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
                LOGGER.info("Configuration loaded from " + CONFIG_FILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load configuration file, using defaults", e);
            }
        } else {
            // Create default configuration file
            try {
                createDefaultConfigFile();
                LOGGER.info("Created default configuration file: " + CONFIG_FILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to create default configuration file", e);
            }
        }
        
        // Parse configuration values
        serverPort = Integer.parseInt(config.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
        maxClients = Integer.parseInt(config.getProperty("server.maxClients", String.valueOf(DEFAULT_MAX_CLIENTS)));
        enableFileLogging = Boolean.parseBoolean(config.getProperty("logging.enabled", String.valueOf(DEFAULT_ENABLE_LOGGING)));
        logLevel = config.getProperty("logging.level", "INFO");
        statsInterval = Integer.parseInt(config.getProperty("stats.interval", String.valueOf(DEFAULT_STATS_INTERVAL)));
    }
    
    /**
     * Create a default configuration file.
     */
    private void createDefaultConfigFile() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "Xiangqi Game Server Configuration");
        }
    }
    
    /**
     * Setup logging configuration.
     */
    private void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        
        // Set log level
        Level level = Level.INFO;
        try {
            level = Level.parse(logLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid log level: " + logLevel + ", using INFO");
        }
        rootLogger.setLevel(level);
        
        // Setup console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        consoleHandler.setFormatter(new SimpleFormatter());
        
        // Clear existing handlers and add console handler
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.addHandler(consoleHandler);
        
        // Setup file handler if enabled
        if (enableFileLogging) {
            try {
                FileHandler fileHandler = new FileHandler(LOG_FILE, true);
                fileHandler.setLevel(level);
                fileHandler.setFormatter(new SimpleFormatter());
                rootLogger.addHandler(fileHandler);
                LOGGER.info("File logging enabled: " + LOG_FILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to setup file logging", e);
            }
        }
    }
    
    /**
     * Start monitoring and statistics collection.
     */
    private void startMonitoring() {
        // Schedule periodic statistics logging
        scheduledExecutor.scheduleAtFixedRate(
            this::logServerStatistics,
            statsInterval,
            statsInterval,
            TimeUnit.SECONDS
        );
        
        // Schedule periodic cleanup tasks
        scheduledExecutor.scheduleAtFixedRate(
            this::performCleanupTasks,
            300, // Start after 5 minutes
            300, // Run every 5 minutes
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Log server statistics.
     */
    private void logServerStatistics() {
        if (gameServer != null && gameServer.isRunning()) {
            int clientCount = gameServer.getClientCount();
            int gameCount = gameServer.getGameCount();
            
            LOGGER.info(String.format("Server Stats - Clients: %d, Active Games: %d", 
                       clientCount, gameCount));
            
            // Log memory usage
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            LOGGER.fine(String.format("Memory Usage - Used: %d MB, Free: %d MB, Total: %d MB",
                       usedMemory / 1024 / 1024, freeMemory / 1024 / 1024, totalMemory / 1024 / 1024));
        }
    }
    
    /**
     * Perform periodic cleanup tasks.
     */
    private void performCleanupTasks() {
        LOGGER.fine("Performing cleanup tasks");
        
        // Force garbage collection
        System.gc();
        
        // Additional cleanup tasks can be added here
        // For example: cleaning up old log files, temporary files, etc.
    }
    
    /**
     * Run the interactive console for server administration.
     */
    private void runConsole() {
        Scanner scanner = new Scanner(System.in);
        
        while (running) {
            System.out.print("server> ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+");
            String command = parts[0];
            
            try {
                switch (command) {
                    case "help":
                        showHelp();
                        break;
                    case "status":
                        showStatus();
                        break;
                    case "stats":
                        showDetailedStats();
                        break;
                    case "clients":
                        showClientList();
                        break;
                    case "games":
                        showGameList();
                        break;
                    case "config":
                        showConfiguration();
                        break;
                    case "reload":
                        reloadConfiguration();
                        break;
                    case "gc":
                        System.gc();
                        System.out.println("Garbage collection requested");
                        break;
                    case "stop":
                    case "shutdown":
                    case "quit":
                    case "exit":
                        shutdown();
                        break;
                    default:
                        System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
                LOGGER.log(Level.WARNING, "Error in console command: " + command, e);
            }
        }
        
        scanner.close();
    }
    
    /**
     * Show available console commands.
     */
    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  help      - Show this help message");
        System.out.println("  status    - Show server status");
        System.out.println("  stats     - Show detailed server statistics");
        System.out.println("  clients   - List connected clients");
        System.out.println("  games     - List active games");
        System.out.println("  config    - Show current configuration");
        System.out.println("  reload    - Reload configuration from file");
        System.out.println("  gc        - Request garbage collection");
        System.out.println("  stop      - Stop the server");
    }
    
    /**
     * Show server status.
     */
    private void showStatus() {
        if (gameServer != null && gameServer.isRunning()) {
            System.out.println("Server Status: RUNNING");
            System.out.println("Port: " + serverPort);
            System.out.println("Connected Clients: " + gameServer.getClientCount());
            System.out.println("Active Games: " + gameServer.getGameCount());
        } else {
            System.out.println("Server Status: STOPPED");
        }
    }
    
    /**
     * Show detailed server statistics.
     */
    private void showDetailedStats() {
        showStatus();
        
        // Memory statistics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        System.out.println("\nMemory Statistics:");
        System.out.println("  Used Memory: " + (usedMemory / 1024 / 1024) + " MB");
        System.out.println("  Free Memory: " + (freeMemory / 1024 / 1024) + " MB");
        System.out.println("  Total Memory: " + (totalMemory / 1024 / 1024) + " MB");
        System.out.println("  Max Memory: " + (maxMemory / 1024 / 1024) + " MB");
        
        // Thread statistics
        System.out.println("\nThread Statistics:");
        System.out.println("  Active Threads: " + Thread.activeCount());
    }
    
    /**
     * Show list of connected clients.
     */
    private void showClientList() {
        if (gameServer != null) {
            int clientCount = gameServer.getClientCount();
            System.out.println("Connected Clients: " + clientCount);
            // In a real implementation, we'd iterate through clients and show details
        }
    }
    
    /**
     * Show list of active games.
     */
    private void showGameList() {
        if (gameServer != null) {
            int gameCount = gameServer.getGameCount();
            System.out.println("Active Games: " + gameCount);
            // In a real implementation, we'd iterate through games and show details
        }
    }
    
    /**
     * Show current configuration.
     */
    private void showConfiguration() {
        System.out.println("Current Configuration:");
        System.out.println("  Server Port: " + serverPort);
        System.out.println("  Max Clients: " + maxClients);
        System.out.println("  File Logging: " + enableFileLogging);
        System.out.println("  Log Level: " + logLevel);
        System.out.println("  Stats Interval: " + statsInterval + " seconds");
    }
    
    /**
     * Reload configuration from file.
     */
    private void reloadConfiguration() {
        try {
            loadConfiguration();
            setupLogging();
            System.out.println("Configuration reloaded successfully");
            LOGGER.info("Configuration reloaded");
        } catch (Exception e) {
            System.out.println("Failed to reload configuration: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Failed to reload configuration", e);
        }
    }
    
    /**
     * Get the game server instance.
     */
    public GameServer getGameServer() {
        return gameServer;
    }
    
    /**
     * Check if the server is running.
     */
    public boolean isRunning() {
        return running && gameServer != null && gameServer.isRunning();
    }
    
    /**
     * Get server configuration.
     */
    public Properties getConfiguration() {
        return new Properties(config);
    }
}