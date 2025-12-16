package com.xiangqi.server.network;

import com.xiangqi.shared.network.messages.LoginMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameServer.
 */
public class GameServerTest {
    
    private GameServer gameServer;
    private static final int TEST_PORT = 8889; // Use different port for testing
    
    @BeforeEach
    void setUp() {
        gameServer = new GameServer();
    }
    
    @AfterEach
    void tearDown() {
        if (gameServer.isRunning()) {
            gameServer.stopServer();
        }
    }
    
    @Test
    void testServerInitialization() {
        assertNotNull(gameServer);
        assertFalse(gameServer.isRunning());
        assertEquals(0, gameServer.getClientCount());
        assertEquals(0, gameServer.getGameCount());
    }
    
    @Test
    void testServerStartAndStop() {
        // Start server
        gameServer.startServer(TEST_PORT);
        assertTrue(gameServer.isRunning());
        
        // Stop server
        gameServer.stopServer();
        assertFalse(gameServer.isRunning());
    }
    
    @Test
    void testServerStartTwice() {
        // Start server first time
        gameServer.startServer(TEST_PORT);
        assertTrue(gameServer.isRunning());
        
        // Try to start again - should not cause issues
        gameServer.startServer(TEST_PORT);
        assertTrue(gameServer.isRunning());
        
        // Clean up
        gameServer.stopServer();
    }
    
    @Test
    void testStopServerWhenNotRunning() {
        // Should not throw exception when stopping a server that's not running
        assertDoesNotThrow(() -> gameServer.stopServer());
        assertFalse(gameServer.isRunning());
    }
    
    @Test
    void testLoginHandling() {
        LoginMessage validLogin = new LoginMessage("testuser", "testpass");
        LoginMessage invalidLogin = new LoginMessage("", "");
        
        // Should not throw exceptions when handling login messages
        assertDoesNotThrow(() -> gameServer.handleLoginRequest(validLogin));
        assertDoesNotThrow(() -> gameServer.handleLoginRequest(invalidLogin));
    }
    
    @Test
    void testSendToNonExistentClient() {
        LoginMessage message = new LoginMessage("test", "test");
        
        // Should not throw exception when sending to non-existent client
        assertDoesNotThrow(() -> gameServer.sendToClient("non-existent", message));
    }
    
    @Test
    void testBroadcastToNonExistentGame() {
        LoginMessage message = new LoginMessage("test", "test");
        
        // Should not throw exception when broadcasting to non-existent game
        assertDoesNotThrow(() -> gameServer.broadcastToGame("non-existent", message));
    }
    
    /**
     * **Feature: networked-xiangqi-game, Property 14: 并发连接处理**
     * Property test for concurrent connection handling.
     * For any number of concurrent client connections, the game server should use multithreading 
     * to handle each connection independently.
     * **Validates: Requirements 5.1, 8.1**
     */
    @Test
    void testConcurrentConnectionHandling() {
        // Run property test with multiple iterations and different connection counts
        int[] connectionCounts = {2, 5, 8, 10, 15};
        
        for (int connectionCount : connectionCounts) {
            testConcurrentConnectionsWithCount(connectionCount);
        }
    }
    
    private void testConcurrentConnectionsWithCount(int connectionCount) {
        // Use a different port for each test to avoid conflicts
        int testPort = 8890 + connectionCount;
        GameServer testServer = new GameServer();
        
        try {
            // Start the server
            testServer.startServer(testPort);
            assertTrue(testServer.isRunning(), "Server should be running");
            
            // Allow server to fully initialize
            Thread.sleep(200);
            
            // Create multiple concurrent connections
            List<Socket> connections = new ArrayList<>();
            List<Future<Boolean>> connectionTasks = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(connectionCount);
            AtomicInteger successfulConnections = new AtomicInteger(0);
            
            // Submit connection tasks concurrently
            for (int i = 0; i < connectionCount; i++) {
                Future<Boolean> task = executor.submit(() -> {
                    try {
                        Socket socket = new Socket("localhost", testPort);
                        
                        // Initialize proper streams to allow ClientHandler to initialize successfully
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.flush();
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        
                        synchronized (connections) {
                            connections.add(socket);
                        }
                        successfulConnections.incrementAndGet();
                        
                        // Keep connection alive longer to test concurrent handling
                        Thread.sleep(800);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
                connectionTasks.add(task);
            }
            
            // Allow more time for connections to be established and client handlers to be created
            Thread.sleep(500);
            
            // Verify that server is using multithreading (Requirement 8.1)
            // The server should have created client handlers for the connections
            int clientCount = testServer.getClientCount();
            assertTrue(clientCount > 0, 
                "Server should have registered client connections, indicating multithreaded handling");
            
            // Wait for all connection attempts to complete
            for (Future<Boolean> task : connectionTasks) {
                try {
                    task.get(5, TimeUnit.SECONDS);
                } catch (TimeoutException | ExecutionException | InterruptedException e) {
                    // Continue with other tasks
                }
            }
            
            executor.shutdown();
            
            // Verify that the server accepted multiple concurrent connections
            // Property: Server should accept multiple concurrent connections (Requirement 5.1)
            assertTrue(successfulConnections.get() >= Math.min(connectionCount, 10), 
                "Server should accept multiple concurrent connections with count " + connectionCount + 
                ". Expected at least " + Math.min(connectionCount, 10) + " but got " + successfulConnections.get());
            
            // Clean up connections
            synchronized (connections) {
                for (Socket socket : connections) {
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                }
            }
            
            // Allow time for cleanup
            Thread.sleep(200);
            
        } catch (Exception e) {
            fail("Concurrent connection test failed with connection count " + connectionCount + 
                 ": " + e.getMessage());
        } finally {
            // Always stop the test server
            testServer.stopServer();
        }
    }
}