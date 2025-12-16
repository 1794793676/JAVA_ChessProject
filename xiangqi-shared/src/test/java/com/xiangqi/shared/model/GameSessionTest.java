package com.xiangqi.shared.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for GameSession class to verify game session management.
 */
class GameSessionTest {
    
    private Player redPlayer;
    private Player blackPlayer;
    
    @BeforeEach
    void setUp() {
        redPlayer = new Player("red_player", "RedUser");
        blackPlayer = new Player("black_player", "BlackUser");
    }
    
    @Test
    void testGameSessionCreation() {
        GameSession session = new GameSession(redPlayer, blackPlayer);
        
        assertNotNull(session.getSessionId());
        assertEquals(redPlayer, session.getRedPlayer());
        assertEquals(blackPlayer, session.getBlackPlayer());
        assertNotNull(session.getGameState());
        assertTrue(session.isActive());
        assertFalse(session.isEnded());
    }
    
    @Test
    void testPlayerMembership() {
        GameSession session = new GameSession(redPlayer, blackPlayer);
        
        assertTrue(session.hasPlayer(redPlayer));
        assertTrue(session.hasPlayer(blackPlayer));
        
        Player outsidePlayer = new Player("outside", "OutsideUser");
        assertFalse(session.hasPlayer(outsidePlayer));
    }
    
    @Test
    void testOpponentRetrieval() {
        GameSession session = new GameSession(redPlayer, blackPlayer);
        
        assertEquals(blackPlayer, session.getOpponent(redPlayer));
        assertEquals(redPlayer, session.getOpponent(blackPlayer));
        
        Player outsidePlayer = new Player("outside", "OutsideUser");
        assertThrows(IllegalArgumentException.class, () -> session.getOpponent(outsidePlayer));
    }
    
    @Test
    void testActivityTracking() {
        GameSession session = new GameSession(redPlayer, blackPlayer);
        long initialTime = session.getLastActivityTime();
        
        // Simulate some delay
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        session.updateLastActivity();
        assertTrue(session.getLastActivityTime() > initialTime);
    }
    
    @Test
    void testGameStateUpdate() {
        GameSession session = new GameSession(redPlayer, blackPlayer);
        GameState newState = new GameState(redPlayer, blackPlayer);
        newState.setStatus(GameStatus.CHECK);
        
        long beforeUpdate = session.getLastActivityTime();
        session.setGameState(newState);
        
        assertEquals(GameStatus.CHECK, session.getGameState().getStatus());
        assertTrue(session.getLastActivityTime() >= beforeUpdate);
    }
    
    @Test
    void testSessionEquality() {
        GameSession session1 = new GameSession("session1", redPlayer, blackPlayer);
        GameSession session2 = new GameSession("session1", redPlayer, blackPlayer);
        GameSession session3 = new GameSession("session2", redPlayer, blackPlayer);
        
        assertEquals(session1, session2); // Same session ID
        assertNotEquals(session1, session3); // Different session ID
    }
}