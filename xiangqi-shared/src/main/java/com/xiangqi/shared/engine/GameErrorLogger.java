package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Centralized error logging utility for game-related exceptions and errors.
 * Provides structured logging for debugging and error analysis.
 */
public class GameErrorLogger {
    private static final Logger LOGGER = Logger.getLogger(GameErrorLogger.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static GameErrorLogger instance;
    private final String logDirectory;
    private final boolean enableFileLogging;
    
    private GameErrorLogger(String logDirectory, boolean enableFileLogging) {
        this.logDirectory = logDirectory;
        this.enableFileLogging = enableFileLogging;
        
        if (enableFileLogging) {
            createLogDirectory();
        }
    }
    
    /**
     * Gets the singleton instance of GameErrorLogger.
     */
    public static synchronized GameErrorLogger getInstance() {
        if (instance == null) {
            // Default configuration - logs to system temp directory
            String tempDir = System.getProperty("java.io.tmpdir");
            instance = new GameErrorLogger(tempDir + File.separator + "xiangqi-logs", true);
        }
        return instance;
    }
    
    /**
     * Initializes the error logger with custom configuration.
     */
    public static synchronized void initialize(String logDirectory, boolean enableFileLogging) {
        instance = new GameErrorLogger(logDirectory, enableFileLogging);
    }
    
    /**
     * Logs an invalid move attempt with detailed context.
     */
    public void logInvalidMove(Move move, GameState gameState, String reason, String playerId) {
        String logEntry = buildInvalidMoveLogEntry(move, gameState, reason, playerId);
        
        LOGGER.warning("Invalid move: " + logEntry);
        
        if (enableFileLogging) {
            writeToFile("invalid_moves.log", logEntry);
        }
    }
    
    /**
     * Logs a game state corruption incident.
     */
    public void logGameStateCorruption(GameState corruptedState, String reason, Exception cause) {
        String logEntry = buildCorruptionLogEntry(corruptedState, reason, cause);
        
        LOGGER.severe("Game state corruption: " + logEntry);
        
        if (enableFileLogging) {
            writeToFile("state_corruption.log", logEntry);
        }
    }
    
    /**
     * Logs a network error with context.
     */
    public void logNetworkError(String clientId, String operation, Exception error) {
        String logEntry = buildNetworkErrorLogEntry(clientId, operation, error);
        
        LOGGER.log(Level.SEVERE, "Network error: " + logEntry, error);
        
        if (enableFileLogging) {
            writeToFile("network_errors.log", logEntry);
        }
    }
    
    /**
     * Logs a game engine exception.
     */
    public void logEngineException(String operation, GameState gameState, Exception exception) {
        String logEntry = buildEngineExceptionLogEntry(operation, gameState, exception);
        
        LOGGER.log(Level.SEVERE, "Engine exception: " + logEntry, exception);
        
        if (enableFileLogging) {
            writeToFile("engine_exceptions.log", logEntry);
        }
    }
    
    /**
     * Logs a successful error recovery.
     */
    public void logErrorRecovery(String errorType, String recoveryAction, boolean successful) {
        String logEntry = buildRecoveryLogEntry(errorType, recoveryAction, successful);
        
        if (successful) {
            LOGGER.info("Error recovery successful: " + logEntry);
        } else {
            LOGGER.warning("Error recovery failed: " + logEntry);
        }
        
        if (enableFileLogging) {
            writeToFile("error_recovery.log", logEntry);
        }
    }
    
    /**
     * Builds a detailed log entry for invalid moves.
     */
    private String buildInvalidMoveLogEntry(Move move, GameState gameState, String reason, String playerId) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getCurrentTimestamp()).append("] ");
        sb.append("Player: ").append(playerId).append(" | ");
        sb.append("Move: ").append(move != null ? move.toString() : "null").append(" | ");
        sb.append("Reason: ").append(reason).append(" | ");
        
        if (gameState != null) {
            sb.append("Current Player: ").append(gameState.getCurrentPlayer() != null ? 
                gameState.getCurrentPlayer().getUsername() : "null").append(" | ");
            sb.append("Game Status: ").append(gameState.getStatus()).append(" | ");
            sb.append("Move Count: ").append(gameState.getMoveHistory().size());
        } else {
            sb.append("Game State: null");
        }
        
        return sb.toString();
    }
    
    /**
     * Builds a detailed log entry for game state corruption.
     */
    private String buildCorruptionLogEntry(GameState corruptedState, String reason, Exception cause) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getCurrentTimestamp()).append("] ");
        sb.append("Reason: ").append(reason).append(" | ");
        
        if (corruptedState != null) {
            sb.append("Red Player: ").append(corruptedState.getRedPlayer() != null ? 
                corruptedState.getRedPlayer().getUsername() : "unknown").append(" | ");
            sb.append("Black Player: ").append(corruptedState.getBlackPlayer() != null ? 
                corruptedState.getBlackPlayer().getUsername() : "unknown").append(" | ");
            sb.append("Status: ").append(corruptedState.getStatus()).append(" | ");
            sb.append("Move Count: ").append(corruptedState.getMoveHistory().size()).append(" | ");
        }
        
        if (cause != null) {
            sb.append("Exception: ").append(cause.getClass().getSimpleName()).append(" - ").append(cause.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * Builds a detailed log entry for network errors.
     */
    private String buildNetworkErrorLogEntry(String clientId, String operation, Exception error) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getCurrentTimestamp()).append("] ");
        sb.append("Client: ").append(clientId != null ? clientId : "unknown").append(" | ");
        sb.append("Operation: ").append(operation).append(" | ");
        sb.append("Error: ").append(error.getClass().getSimpleName()).append(" - ").append(error.getMessage());
        
        return sb.toString();
    }
    
    /**
     * Builds a detailed log entry for engine exceptions.
     */
    private String buildEngineExceptionLogEntry(String operation, GameState gameState, Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getCurrentTimestamp()).append("] ");
        sb.append("Operation: ").append(operation).append(" | ");
        
        if (gameState != null) {
            sb.append("Game Status: ").append(gameState.getStatus()).append(" | ");
            sb.append("Current Player: ").append(gameState.getCurrentPlayer() != null ? 
                gameState.getCurrentPlayer().getUsername() : "null").append(" | ");
        }
        
        sb.append("Exception: ").append(exception.getClass().getSimpleName()).append(" - ").append(exception.getMessage());
        
        return sb.toString();
    }
    
    /**
     * Builds a log entry for error recovery attempts.
     */
    private String buildRecoveryLogEntry(String errorType, String recoveryAction, boolean successful) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getCurrentTimestamp()).append("] ");
        sb.append("Error Type: ").append(errorType).append(" | ");
        sb.append("Recovery Action: ").append(recoveryAction).append(" | ");
        sb.append("Result: ").append(successful ? "SUCCESS" : "FAILED");
        
        return sb.toString();
    }
    
    /**
     * Gets the current timestamp as a formatted string.
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
    
    /**
     * Creates the log directory if it doesn't exist.
     */
    private void createLogDirectory() {
        try {
            File dir = new File(logDirectory);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    LOGGER.info("Created log directory: " + logDirectory);
                } else {
                    LOGGER.warning("Failed to create log directory: " + logDirectory);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating log directory: " + logDirectory, e);
        }
    }
    
    /**
     * Writes a log entry to the specified file.
     */
    private void writeToFile(String filename, String logEntry) {
        try {
            File logFile = new File(logDirectory, filename);
            
            try (FileWriter writer = new FileWriter(logFile, true);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                
                bufferedWriter.write(logEntry);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write to log file: " + filename, e);
        }
    }
}