package com.xiangqi.client.ui;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.model.Player;
import com.xiangqi.shared.model.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LobbyFrame
 */
public class LobbyFrameTest {
    private LobbyFrame lobbyFrame;
    
    @BeforeEach
    public void setUp() {
        lobbyFrame = new LobbyFrame();
        
        // Set up test listener
        lobbyFrame.setLobbyListener(new LobbyFrame.LobbyListener() {
            @Override
            public void onGameInvitation(Player targetPlayer) {
                // Test implementation - just verify no exceptions
            }
            
            @Override
            public void onRefreshRequest() {
                // Test implementation - just verify no exceptions
            }
            
            @Override
            public void onLogout() {
                // Test implementation - just verify no exceptions
            }
            
            @Override
            public void onJoinGame(GameSession gameSession) {
                // Test implementation - just verify no exceptions
            }
        });
    }
    
    @Test
    public void testLobbyFrameInitialization() {
        assertNotNull(lobbyFrame);
        assertEquals("象棋游戏 - 大厅", lobbyFrame.getTitle());
        assertFalse(lobbyFrame.isVisible());
    }
    
    @Test
    public void testSetCurrentPlayer() {
        Player testPlayer = new Player("test1", "TestUser");
        testPlayer.setRating(1200);
        
        lobbyFrame.setCurrentPlayer(testPlayer);
        // Should update the current player label
        // This is mainly testing that no exceptions are thrown
    }
    
    @Test
    public void testUpdatePlayerList() {
        Player player1 = new Player("p1", "Player1");
        player1.setStatus(PlayerStatus.ONLINE);
        Player player2 = new Player("p2", "Player2");
        player2.setStatus(PlayerStatus.IN_GAME);
        
        List<Player> players = Arrays.asList(player1, player2);
        
        lobbyFrame.updatePlayerList(players);
        // Should update the player list without throwing exceptions
    }
    
    @Test
    public void testUpdateGameList() {
        Player redPlayer = new Player("red", "RedPlayer");
        Player blackPlayer = new Player("black", "BlackPlayer");
        GameSession game = new GameSession(redPlayer, blackPlayer);
        
        List<GameSession> games = Arrays.asList(game);
        
        lobbyFrame.updateGameList(games);
        // Should update the game list without throwing exceptions
    }
    
    @Test
    public void testShowStatus() {
        lobbyFrame.showStatus("Test status message");
        // Should display status without throwing exceptions
    }
    
    @Test
    public void testShowError() {
        lobbyFrame.showError("Test error message");
        // Should display error without throwing exceptions
    }
    
    @Test
    public void testAutoRefreshControl() {
        lobbyFrame.startAutoRefresh();
        // Should start auto refresh without throwing exceptions
        
        lobbyFrame.stopAutoRefresh();
        // Should stop auto refresh without throwing exceptions
    }
    
    @Test
    public void testShowAndHideLobby() {
        lobbyFrame.showLobby();
        assertTrue(lobbyFrame.isVisible());
        
        lobbyFrame.hideLobby();
        assertFalse(lobbyFrame.isVisible());
    }
}