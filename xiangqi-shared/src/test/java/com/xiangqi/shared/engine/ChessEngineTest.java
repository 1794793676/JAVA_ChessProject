package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.model.pieces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ChessEngine functionality.
 */
class ChessEngineTest {
    
    private ChessEngine engine;
    private Player redPlayer;
    private Player blackPlayer;
    
    @BeforeEach
    void setUp() {
        engine = new ChessEngine();
        redPlayer = new Player("red_player", "RedUser");
        blackPlayer = new Player("black_player", "BlackUser");
    }
    
    @Test
    void testGameInitialization() {
        engine.initializeGame(redPlayer, blackPlayer);
        
        GameState state = engine.getCurrentState();
        assertNotNull(state);
        assertEquals(GameStatus.IN_PROGRESS, state.getStatus());
        assertEquals(redPlayer, state.getCurrentPlayer()); // Red moves first
        assertEquals(redPlayer, state.getRedPlayer());
        assertEquals(blackPlayer, state.getBlackPlayer());
    }
    
    @Test
    void testInitialBoardSetup() {
        engine.initializeGame(redPlayer, blackPlayer);
        GameState state = engine.getCurrentState();
        
        // Check that generals are in correct positions
        ChessPiece redGeneral = state.getPiece(new Position(9, 4));
        ChessPiece blackGeneral = state.getPiece(new Position(0, 4));
        
        assertNotNull(redGeneral);
        assertNotNull(blackGeneral);
        assertTrue(redGeneral instanceof General);
        assertTrue(blackGeneral instanceof General);
        assertEquals(redPlayer, redGeneral.getOwner());
        assertEquals(blackPlayer, blackGeneral.getOwner());
        
        // Check that some soldiers are in correct positions
        ChessPiece redSoldier = state.getPiece(new Position(6, 0));
        ChessPiece blackSoldier = state.getPiece(new Position(3, 0));
        
        assertNotNull(redSoldier);
        assertNotNull(blackSoldier);
        assertTrue(redSoldier instanceof Soldier);
        assertTrue(blackSoldier instanceof Soldier);
    }
    
    @Test
    void testValidMoveExecution() {
        engine.initializeGame(redPlayer, blackPlayer);
        GameState state = engine.getCurrentState();
        
        // Try to move a red soldier forward
        Position from = new Position(6, 0);
        Position to = new Position(5, 0);
        ChessPiece soldier = state.getPiece(from);
        
        assertNotNull(soldier);
        assertTrue(soldier instanceof Soldier);
        
        Move move = new Move(from, to, soldier);
        boolean result = engine.executeMove(move);
        
        assertTrue(result);
        assertNull(state.getPiece(from)); // Original position should be empty
        assertEquals(soldier, state.getPiece(to)); // Piece should be at new position
        assertEquals(blackPlayer, state.getCurrentPlayer()); // Should switch to black player
    }
    
    @Test
    void testInvalidMoveRejection() {
        engine.initializeGame(redPlayer, blackPlayer);
        GameState state = engine.getCurrentState();
        
        // Try to move a piece that doesn't exist
        Position from = new Position(4, 4); // Empty po~sition
        Position to = new Position(5, 4);
        ChessPiece nonExistentPiece = new Soldier(redPlayer, from);
        
        Move invalidMove = new Move(from, to, nonExistentPiece);
        boolean result = engine.executeMove(invalidMove);
        
        assertFalse(result);
        assertEquals(redPlayer, state.getCurrentPlayer()); // Should not switch players
    }
    
    @Test
    void testGetValidMoves() {
        engine.initializeGame(redPlayer, blackPlayer);
        
        // Get valid moves for a red soldier
        Position soldierPos = new Position(6, 0);
        var validMoves = engine.getValidMoves(soldierPos);
        
        assertFalse(validMoves.isEmpty());
        
        // Soldier should be able to move forward
        boolean canMoveForward = validMoves.stream()
            .anyMatch(move -> move.getTo().equals(new Position(5, 0)));
        assertTrue(canMoveForward);
    }
    
    @Test
    void testCheckDetection() {
        // Create a simple scenario where black general is in check
        GameState state = new GameState(redPlayer, blackPlayer);
        
        // Place black general
        General blackGeneral = new General(blackPlayer, new Position(0, 4));
        state.setPiece(new Position(0, 4), blackGeneral);
        
        // Place red chariot that can attack the general
        Chariot redChariot = new Chariot(redPlayer, new Position(0, 0));
        state.setPiece(new Position(0, 0), redChariot);
        
        engine.setCurrentState(state);
        
        assertTrue(engine.isInCheck(blackPlayer));
        assertFalse(engine.isInCheck(redPlayer));
    }
}