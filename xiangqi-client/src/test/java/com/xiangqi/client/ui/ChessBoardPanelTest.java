package com.xiangqi.client.ui;

import com.xiangqi.shared.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ChessBoardPanel functionality.
 */
public class ChessBoardPanelTest {
    
    private ChessBoardPanel boardPanel;
    private GameState gameState;
    private Player redPlayer;
    private Player blackPlayer;
    
    @BeforeEach
    public void setUp() {
        // Create test players
        redPlayer = new Player("red_player", "Red Player");
        blackPlayer = new Player("black_player", "Black Player");
        
        // Create game state
        gameState = new GameState(redPlayer, blackPlayer);
        
        // Create board panel
        boardPanel = new ChessBoardPanel();
    }
    
    @Test
    public void testBoardPanelCreation() {
        assertNotNull(boardPanel);
        assertTrue(boardPanel instanceof JPanel);
        
        Dimension preferredSize = boardPanel.getPreferredSize();
        assertTrue(preferredSize.width > 0);
        assertTrue(preferredSize.height > 0);
    }
    
    @Test
    public void testUpdateGameState() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            boardPanel.updateGameState(gameState);
        });
    }
    
    @Test
    public void testClearSelection() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            boardPanel.clearSelection();
        });
    }
    
    @Test
    public void testSetBoardEventListener() {
        ChessBoardPanel.BoardEventListener listener = new ChessBoardPanel.BoardEventListener() {
            @Override
            public void onMoveAttempted(Position from, Position to) {
                // Test listener
            }
            
            @Override
            public void onPieceSelected(Position position) {
                // Test listener
            }
        };
        
        assertDoesNotThrow(() -> {
            boardPanel.setBoardEventListener(listener);
        });
    }
}