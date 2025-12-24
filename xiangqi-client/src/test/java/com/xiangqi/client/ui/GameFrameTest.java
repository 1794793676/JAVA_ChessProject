package com.xiangqi.client.ui;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.network.messages.*;
import com.xiangqi.client.network.NetworkClient;
import com.xiangqi.shared.network.NetworkMessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GameFrame functionality.
 */
public class GameFrameTest {
    
    private GameFrame gameFrame;
    private Player localPlayer;
    private NetworkClient networkClient;
    private GameState gameState;
    
    @BeforeEach
    public void setUp() {
        // Create test player
        localPlayer = new Player("test_player", "Test Player");
        
        // Create mock network client with a dummy message handler
        NetworkMessageHandler dummyHandler = new NetworkMessageHandler() {
            @Override
            public void handleLoginRequest(LoginMessage message) {}
            
            @Override
            public void handleMoveMessage(MoveMessage message) {}
            
            @Override
            public void handleChatMessage(ChatMessage message) {}
            
            @Override
            public void handleDisconnection(String clientId) {}
        };
        networkClient = new NetworkClient(dummyHandler);
        
        // Create game state
        Player redPlayer = new Player("red_player", "Red Player");
        Player blackPlayer = new Player("black_player", "Black Player");
        gameState = new GameState(redPlayer, blackPlayer);
        
        // Create game frame
        gameFrame = new GameFrame(localPlayer, networkClient);
    }
    
    @Test
    public void testGameFrameCreation() {
        assertNotNull(gameFrame);
        assertTrue(gameFrame instanceof JFrame);
        
        // Check that the frame has a title
        String title = gameFrame.getTitle();
        assertNotNull(title);
        assertTrue(title.contains(localPlayer.getUsername()));
    }
    
    @Test
    public void testUpdateGameState() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            gameFrame.updateGameState(gameState);
        });
    }
    
    @Test
    public void testAppendChatMessage() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            gameFrame.appendChatMessage("Test User", "Hello World");
        });
    }
    
    @Test
    public void testTimerMethods() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            gameFrame.startTimer();
            gameFrame.stopTimer();
            gameFrame.resetTimers();
        });
    }
    
    @Test
    public void testSetGameEventListener() {
        GameFrame.GameEventListener listener = new GameFrame.GameEventListener() {
            @Override
            public void onMoveAttempted(Move move) {
                // Test listener
            }
            
            @Override
            public void onChatMessageSent(String message) {
                // Test listener
            }
            
            @Override
            public void onResignRequested() {
                // Test listener
            }
            
            @Override
            public void onDrawOfferRequested() {
                // Test listener
            }
            
            @Override
            public void onNewGameRequested() {
                // Test listener
            }
            
            @Override
            public void onReturnToLobbyRequested() {
                // Test listener
            }
        };
        
        assertDoesNotThrow(() -> {
            gameFrame.setGameEventListener(listener);
        });
    }
    
    @Test
    public void testShowGameEndDialog() {
        Player winner = new Player("winner", "Winner");
        Player loser = new Player("loser", "Loser");
        GameResult result = GameResult.checkmate(winner, loser);
        
        // This test just ensures the method doesn't throw an exception
        // In a real environment, this would show a dialog
        assertDoesNotThrow(() -> {
            // We can't easily test the dialog in a headless environment
            // but we can test that the method exists and can be called
            gameFrame.showGameEndDialog(result);
        });
    }
}