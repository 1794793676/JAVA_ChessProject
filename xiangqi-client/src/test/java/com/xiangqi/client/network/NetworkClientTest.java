package com.xiangqi.client.network;

import com.xiangqi.shared.network.NetworkMessageHandler;
import com.xiangqi.shared.network.messages.ChatMessage;
import com.xiangqi.shared.network.messages.LoginMessage;
import com.xiangqi.shared.network.messages.MoveMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NetworkClient.
 */
public class NetworkClientTest {
    
    private NetworkClient networkClient;
    private TestMessageHandler messageHandler;
    
    @BeforeEach
    void setUp() {
        messageHandler = new TestMessageHandler();
        networkClient = new NetworkClient(messageHandler);
    }
    
    @Test
    void testClientInitialization() {
        assertNotNull(networkClient);
        assertFalse(networkClient.isConnected());
        assertNull(networkClient.getClientId());
    }
    
    @Test
    void testClientIdSetting() {
        String testClientId = "test-client-123";
        networkClient.setClientId(testClientId);
        assertEquals(testClientId, networkClient.getClientId());
    }
    
    @Test
    void testConnectionToInvalidServer() {
        // Try to connect to a non-existent server (using a valid port number but no server running)
        boolean connected = networkClient.connect("localhost", 65432);
        assertFalse(connected);
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testDisconnectWhenNotConnected() {
        // Should not throw exception when disconnecting while not connected
        assertDoesNotThrow(() -> networkClient.disconnect());
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testSendMessageWhenNotConnected() {
        // Should not throw exception when sending message while not connected
        LoginMessage message = new LoginMessage("testuser", "testpass");
        assertDoesNotThrow(() -> networkClient.sendMessage(message));
    }
    
    /**
     * Test implementation of NetworkMessageHandler for testing purposes.
     */
    private static class TestMessageHandler implements NetworkMessageHandler {
        
        @Override
        public void handleLoginRequest(LoginMessage message) {
            // Test implementation
        }
        
        @Override
        public void handleMoveMessage(MoveMessage message) {
            // Test implementation
        }
        
        @Override
        public void handleChatMessage(ChatMessage message) {
            // Test implementation
        }
        
        @Override
        public void handleDisconnection(String clientId) {
            // Test implementation
        }
    }
}