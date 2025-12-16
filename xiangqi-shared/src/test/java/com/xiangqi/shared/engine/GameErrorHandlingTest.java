package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for game error handling and recovery mechanisms.
 */
public class GameErrorHandlingTest {
    
    private ChessEngine chessEngine;
    private Player redPlayer;
    private Player blackPlayer;
    private GameState gameState;
    
    @BeforeEach
    public void setUp() {
        redPlayer = new Player("red_player", "RedUser");
        blackPlayer = new Player("black_player", "BlackUser");
        
        chessEngine = new ChessEngine();
        chessEngine.initializeGame(redPlayer, blackPlayer);
        gameState = chessEngine.getCurrentState();
    }
    
    @Test
    public void testInvalidMoveErrorHandling() {
        // Test invalid move - no piece at source position
        Position emptyPosition = new Position(4, 4);
        Position targetPosition = new Position(5, 4);
        
        // Create a fake piece for the move (this should fail)
        ChessPiece fakePiece = PieceFactory.createPiece(PieceType.SOLDIER, redPlayer, emptyPosition);
        Move invalidMove = new Move(emptyPosition, targetPosition, fakePiece, null);
        
        // Attempt the invalid move
        boolean result = chessEngine.executeMove(invalidMove);
        
        // Should return false for invalid move
        assertFalse(result, "Invalid move should return false");
        
        // Game state should remain unchanged
        assertEquals(GameStatus.IN_PROGRESS, gameState.getStatus());
        assertEquals(redPlayer, gameState.getCurrentPlayer());
        assertEquals(0, gameState.getMoveHistory().size());
    }
    
    @Test
    public void testGameStateRecovery() {
        // Make a valid move first to establish a previous state
        Position soldierPos = new Position(6, 0); // Red soldier position
        Position targetPos = new Position(5, 0);
        ChessPiece soldier = gameState.getPiece(soldierPos);
        
        assertNotNull(soldier, "Should have a soldier at starting position");
        
        Move validMove = new Move(soldierPos, targetPos, soldier, null);
        boolean result = chessEngine.executeMove(validMove);
        
        assertTrue(result, "Valid move should succeed");
        assertEquals(1, gameState.getMoveHistory().size());
        
        // Now test recovery capability
        boolean recoveryResult = chessEngine.recoverGameState();
        
        // Recovery should work (though it will restore to the state before the last move)
        assertTrue(recoveryResult, "Game state recovery should succeed when previous state exists");
    }
    
    @Test
    public void testInvalidMoveReasonGeneration() {
        // Test various invalid move scenarios
        
        // 1. Move from empty position
        Position emptyPos = new Position(4, 4);
        Position targetPos = new Position(5, 4);
        ChessPiece fakePiece = PieceFactory.createPiece(PieceType.SOLDIER, redPlayer, emptyPos);
        Move emptySourceMove = new Move(emptyPos, targetPos, fakePiece, null);
        
        boolean result1 = chessEngine.executeMove(emptySourceMove);
        assertFalse(result1, "Move from empty position should fail");
        
        // 2. Move opponent's piece
        Position blackSoldierPos = new Position(3, 0); // Black soldier position
        ChessPiece blackSoldier = gameState.getPiece(blackSoldierPos);
        assertNotNull(blackSoldier, "Should have a black soldier");
        
        Move opponentPieceMove = new Move(blackSoldierPos, new Position(4, 0), blackSoldier, null);
        boolean result2 = chessEngine.executeMove(opponentPieceMove);
        assertFalse(result2, "Moving opponent's piece should fail");
        
        // Game should still be in progress with red player's turn
        assertEquals(GameStatus.IN_PROGRESS, gameState.getStatus());
        assertEquals(redPlayer, gameState.getCurrentPlayer());
    }
    
    @Test
    public void testErrorEventNotification() {
        // Create a test event listener to capture error events
        TestGameEventListener listener = new TestGameEventListener();
        chessEngine.addEventListener(listener);
        
        // Attempt an invalid move
        Position emptyPos = new Position(4, 4);
        Position targetPos = new Position(5, 4);
        ChessPiece fakePiece = PieceFactory.createPiece(PieceType.SOLDIER, redPlayer, emptyPos);
        Move invalidMove = new Move(emptyPos, targetPos, fakePiece, null);
        
        chessEngine.executeMove(invalidMove);
        
        // Check that error event was fired
        assertTrue(listener.invalidMoveAttempted, "Invalid move event should be fired");
        assertNotNull(listener.lastInvalidMove, "Last invalid move should be recorded");
        assertNotNull(listener.lastErrorReason, "Error reason should be provided");
        assertTrue(listener.lastErrorReason.contains("No piece at source position"), 
                  "Error reason should mention empty source position");
    }
    
    /**
     * Test event listener to capture game events.
     */
    private static class TestGameEventListener implements GameEventListener {
        boolean invalidMoveAttempted = false;
        boolean gameStateCorrupted = false;
        Move lastInvalidMove = null;
        String lastErrorReason = null;
        String lastCorruptionReason = null;
        
        @Override
        public void onMoveExecuted(Move move) {
            // Not needed for this test
        }
        
        @Override
        public void onGameStateChanged(GameState newState) {
            // Not needed for this test
        }
        
        @Override
        public void onPlayerJoined(Player player) {
            // Not needed for this test
        }
        
        @Override
        public void onPlayerLeft(Player player) {
            // Not needed for this test
        }
        
        @Override
        public void onGameEnded(GameResult result) {
            // Not needed for this test
        }
        
        @Override
        public void onInvalidMoveAttempted(Move move, String reason) {
            this.invalidMoveAttempted = true;
            this.lastInvalidMove = move;
            this.lastErrorReason = reason;
        }
        
        @Override
        public void onGameStateCorrupted(String reason) {
            this.gameStateCorrupted = true;
            this.lastCorruptionReason = reason;
        }
    }
}