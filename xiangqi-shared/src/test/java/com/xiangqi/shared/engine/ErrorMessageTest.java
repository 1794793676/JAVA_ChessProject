package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for user-friendly error message generation.
 */
public class ErrorMessageTest {
    
    private ChessEngine chessEngine;
    private Player redPlayer;
    private Player blackPlayer;
    
    @BeforeEach
    public void setUp() {
        redPlayer = new Player("red_player", "RedUser");
        blackPlayer = new Player("black_player", "BlackUser");
        
        chessEngine = new ChessEngine();
        chessEngine.initializeGame(redPlayer, blackPlayer);
    }
    
    @Test
    public void testErrorMessageGeneration() {
        // Create a test event listener to capture error messages
        TestErrorListener listener = new TestErrorListener();
        chessEngine.addEventListener(listener);
        
        // Test 1: No piece at source position
        Position emptyPos = new Position(4, 4);
        ChessPiece fakePiece = PieceFactory.createPiece(PieceType.SOLDIER, redPlayer, emptyPos);
        Move emptySourceMove = new Move(emptyPos, new Position(5, 4), fakePiece, null);
        
        chessEngine.executeMove(emptySourceMove);
        assertTrue(listener.lastErrorReason.contains("No piece at source position"));
        
        // Test 2: Wrong player's turn
        listener.reset();
        Position blackSoldierPos = new Position(3, 0);
        GameState gameState = chessEngine.getCurrentState();
        ChessPiece blackSoldier = gameState.getPiece(blackSoldierPos);
        Move wrongTurnMove = new Move(blackSoldierPos, new Position(4, 0), blackSoldier, null);
        
        chessEngine.executeMove(wrongTurnMove);
        assertTrue(listener.lastErrorReason.contains("Not your turn"));
        
        // Test 3: Piece cannot move to target (invalid piece movement)
        listener.reset();
        Position redSoldierPos = new Position(6, 0);
        ChessPiece redSoldier = gameState.getPiece(redSoldierPos);
        Position invalidTarget = new Position(6, 8); // Too far for a soldier to move in one turn
        Move invalidTargetMove = new Move(redSoldierPos, invalidTarget, redSoldier, null);
        
        chessEngine.executeMove(invalidTargetMove);
        assertTrue(listener.lastErrorReason.contains("Piece cannot move to target position"));
        
        System.out.println("All error message tests passed successfully!");
    }
    
    private static class TestErrorListener implements GameEventListener {
        String lastErrorReason = null;
        
        public void reset() {
            lastErrorReason = null;
        }
        
        @Override
        public void onMoveExecuted(Move move) {}
        
        @Override
        public void onGameStateChanged(GameState newState) {}
        
        @Override
        public void onPlayerJoined(Player player) {}
        
        @Override
        public void onPlayerLeft(Player player) {}
        
        @Override
        public void onGameEnded(GameResult result) {}
        
        @Override
        public void onInvalidMoveAttempted(Move move, String reason) {
            this.lastErrorReason = reason;
        }
    }
}