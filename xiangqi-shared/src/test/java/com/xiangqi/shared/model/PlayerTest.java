package com.xiangqi.shared.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify the Player class functionality and project setup.
 */
class PlayerTest {
    
    @Test
    void testPlayerCreation() {
        Player player = new Player("player1", "TestUser");
        
        assertEquals("player1", player.getPlayerId());
        assertEquals("TestUser", player.getUsername());
        assertEquals(PlayerStatus.OFFLINE, player.getStatus());
        assertEquals(1000, player.getRating());
        assertNotNull(player.getStatistics());
    }
    
    @Test
    void testPlayerEquality() {
        Player player1 = new Player("player1", "TestUser1");
        Player player2 = new Player("player1", "TestUser2"); // Same ID, different username
        Player player3 = new Player("player2", "TestUser1"); // Different ID, same username
        
        assertEquals(player1, player2); // Should be equal based on ID
        assertNotEquals(player1, player3); // Should not be equal
    }
    
    @Test
    void testPlayerStatusChange() {
        Player player = new Player("player1", "TestUser");
        
        player.setStatus(PlayerStatus.ONLINE);
        assertEquals(PlayerStatus.ONLINE, player.getStatus());
        
        player.setStatus(PlayerStatus.IN_GAME);
        assertEquals(PlayerStatus.IN_GAME, player.getStatus());
    }
    
    @Test
    void testNullValidation() {
        assertThrows(NullPointerException.class, () -> new Player(null, "TestUser"));
        assertThrows(NullPointerException.class, () -> new Player("player1", null));
        
        Player player = new Player("player1", "TestUser");
        assertThrows(NullPointerException.class, () -> player.setStatus(null));
        assertThrows(NullPointerException.class, () -> player.setStatistics(null));
    }
}