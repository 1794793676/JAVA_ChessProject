package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.model.pieces.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main chess engine that handles game logic, move execution, and state management.
 * Coordinates between rule validation, game state updates, and event notifications.
 */
public class ChessEngine {
    
    private GameState currentState;
    private final RuleValidator ruleValidator;
    private final List<GameEventListener> eventListeners;
    
    public ChessEngine() {
        this.ruleValidator = new RuleValidator();
        this.eventListeners = new ArrayList<>();
    }
    
    public ChessEngine(GameState initialState) {
        this();
        this.currentState = initialState;
    }
    
    /**
     * Initializes a new game with the standard Xiangqi starting position.
     */
    public void initializeGame(Player redPlayer, Player blackPlayer) {
        currentState = new GameState(redPlayer, blackPlayer);
        setupInitialBoard();
        currentState.setStatus(GameStatus.IN_PROGRESS);
        
        notifyGameStateChanged();
    }
    
    /**
     * Attempts to execute a move. Returns true if successful, false if invalid.
     */
    public boolean executeMove(Move move) {
        if (!isValidMove(move)) {
            notifyInvalidMoveAttempted(move, "Move violates game rules");
            return false;
        }
        
        // Apply the move to the game state
        applyMove(move);
        
        // Add move to history
        currentState.addMove(move);
        
        // Switch to the other player
        currentState.switchPlayer();
        
        // Check for game end conditions
        checkGameEndConditions();
        
        // Notify listeners
        notifyMoveExecuted(move);
        notifyGameStateChanged();
        
        return true;
    }
    
    /**
     * Checks if a move is valid according to all game rules.
     */
    public boolean isValidMove(Move move) {
        if (currentState == null) {
            return false;
        }
        return ruleValidator.isValidMove(move, currentState);
    }
    
    /**
     * Checks if the specified player is currently in check.
     */
    public boolean isInCheck(Player player) {
        if (currentState == null) {
            return false;
        }
        return ruleValidator.isInCheck(player, currentState);
    }
    
    /**
     * Checks if the specified player is in checkmate.
     */
    public boolean isCheckmate(Player player) {
        if (currentState == null) {
            return false;
        }
        return ruleValidator.isCheckmate(player, currentState);
    }
    
    /**
     * Checks if the game is in stalemate.
     */
    public boolean isStalemate(Player player) {
        if (currentState == null) {
            return false;
        }
        return ruleValidator.isStalemate(player, currentState);
    }
    
    /**
     * Gets all valid moves for a piece at the specified position.
     */
    public List<Move> getValidMoves(Position position) {
        if (currentState == null) {
            return new ArrayList<>();
        }
        
        ChessPiece piece = currentState.getPiece(position);
        if (piece == null) {
            return new ArrayList<>();
        }
        
        List<Move> allMoves = piece.getValidMoves(currentState);
        List<Move> validMoves = new ArrayList<>();
        
        // Filter moves that are actually legal (don't leave general in check, etc.)
        for (Move move : allMoves) {
            if (ruleValidator.isValidMove(move, currentState)) {
                validMoves.add(move);
            }
        }
        
        return validMoves;
    }
    
    /**
     * Gets the current game state.
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Sets the current game state (for loading saved games).
     */
    public void setCurrentState(GameState state) {
        this.currentState = state;
        notifyGameStateChanged();
    }
    
    /**
     * Adds an event listener to receive game events.
     */
    public void addEventListener(GameEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * Removes an event listener.
     */
    public void removeEventListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }
    
    /**
     * Sets up the initial board configuration for a standard Xiangqi game.
     */
    private void setupInitialBoard() {
        Player redPlayer = currentState.getRedPlayer();
        Player blackPlayer = currentState.getBlackPlayer();
        
        // Clear the board first
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                currentState.setPiece(new Position(row, col), null);
            }
        }
        
        // Set up black pieces (top of board, rows 0-2)
        setupPiecesForPlayer(blackPlayer, true);
        
        // Set up red pieces (bottom of board, rows 7-9)
        setupPiecesForPlayer(redPlayer, false);
    }
    
    /**
     * Sets up pieces for a specific player.
     */
    private void setupPiecesForPlayer(Player player, boolean isBlack) {
        int backRow = isBlack ? 0 : 9;
        int soldierRow = isBlack ? 3 : 6;
        int cannonRow = isBlack ? 2 : 7;
        
        // Back row pieces
        currentState.setPiece(new Position(backRow, 0), PieceFactory.createPiece(PieceType.CHARIOT, player, new Position(backRow, 0)));
        currentState.setPiece(new Position(backRow, 1), PieceFactory.createPiece(PieceType.HORSE, player, new Position(backRow, 1)));
        currentState.setPiece(new Position(backRow, 2), PieceFactory.createPiece(PieceType.ELEPHANT, player, new Position(backRow, 2)));
        currentState.setPiece(new Position(backRow, 3), PieceFactory.createPiece(PieceType.ADVISOR, player, new Position(backRow, 3)));
        currentState.setPiece(new Position(backRow, 4), PieceFactory.createPiece(PieceType.GENERAL, player, new Position(backRow, 4)));
        currentState.setPiece(new Position(backRow, 5), PieceFactory.createPiece(PieceType.ADVISOR, player, new Position(backRow, 5)));
        currentState.setPiece(new Position(backRow, 6), PieceFactory.createPiece(PieceType.ELEPHANT, player, new Position(backRow, 6)));
        currentState.setPiece(new Position(backRow, 7), PieceFactory.createPiece(PieceType.HORSE, player, new Position(backRow, 7)));
        currentState.setPiece(new Position(backRow, 8), PieceFactory.createPiece(PieceType.CHARIOT, player, new Position(backRow, 8)));
        
        // Cannons
        currentState.setPiece(new Position(cannonRow, 1), PieceFactory.createPiece(PieceType.CANNON, player, new Position(cannonRow, 1)));
        currentState.setPiece(new Position(cannonRow, 7), PieceFactory.createPiece(PieceType.CANNON, player, new Position(cannonRow, 7)));
        
        // Soldiers
        for (int col = 0; col < Position.BOARD_COLS; col += 2) {
            currentState.setPiece(new Position(soldierRow, col), PieceFactory.createPiece(PieceType.SOLDIER, player, new Position(soldierRow, col)));
        }
    }
    
    /**
     * Applies a move to the current game state.
     */
    private void applyMove(Move move) {
        // Remove piece from source position
        currentState.setPiece(move.getFrom(), null);
        
        // Place piece at target position
        currentState.setPiece(move.getTo(), move.getPiece());
    }
    
    /**
     * Checks for game end conditions and updates game status accordingly.
     */
    private void checkGameEndConditions() {
        Player currentPlayer = currentState.getCurrentPlayer();
        Player opponent = currentState.getOpponent(currentPlayer);
        
        if (isCheckmate(currentPlayer)) {
            currentState.setStatus(GameStatus.FINISHED);
            GameResult result = new GameResult(opponent, currentPlayer, GameResult.ResultType.CHECKMATE);
            notifyGameEnded(result);
        } else if (isStalemate(currentPlayer)) {
            currentState.setStatus(GameStatus.FINISHED);
            GameResult result = new GameResult(null, null, GameResult.ResultType.STALEMATE);
            notifyGameEnded(result);
        } else if (ruleValidator.violatesFlyingGeneralRule(currentState)) {
            // The player who created the flying general situation loses
            Player previousPlayer = currentState.getOpponent(currentPlayer);
            currentState.setStatus(GameStatus.FINISHED);
            GameResult result = new GameResult(currentPlayer, previousPlayer, GameResult.ResultType.RULE_VIOLATION);
            notifyGameEnded(result);
        }
    }
    
    // Event notification methods
    private void notifyMoveExecuted(Move move) {
        for (GameEventListener listener : eventListeners) {
            listener.onMoveExecuted(move);
        }
    }
    
    private void notifyGameStateChanged() {
        for (GameEventListener listener : eventListeners) {
            listener.onGameStateChanged(currentState);
        }
    }
    
    private void notifyGameEnded(GameResult result) {
        for (GameEventListener listener : eventListeners) {
            listener.onGameEnded(result);
        }
    }
    
    private void notifyInvalidMoveAttempted(Move move, String reason) {
        for (GameEventListener listener : eventListeners) {
            listener.onInvalidMoveAttempted(move, reason);
        }
    }
}