package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.model.pieces.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main chess engine that handles game logic, move execution, and state management.
 * Coordinates between rule validation, game state updates, and event notifications.
 */
public class ChessEngine {
    private static final Logger LOGGER = Logger.getLogger(ChessEngine.class.getName());
    
    private GameState currentState;
    private GameState previousState; // For game state recovery
    private final RuleValidator ruleValidator;
    private final List<GameEventListener> eventListeners;
    private final GameErrorLogger errorLogger;
    
    public ChessEngine() {
        this.ruleValidator = new RuleValidator();
        this.eventListeners = new ArrayList<>();
        this.errorLogger = GameErrorLogger.getInstance();
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
        try {
            // Save current state for potential recovery
            saveCurrentState();
            
            if (!isValidMove(move)) {
                String reason = getInvalidMoveReason(move);
                String playerId = move.getPiece() != null && move.getPiece().getOwner() != null ? 
                    move.getPiece().getOwner().getPlayerId() : "unknown";
                
                LOGGER.warning("Invalid move attempted: " + move + " - " + reason);
                errorLogger.logInvalidMove(move, currentState, reason, playerId);
                notifyInvalidMoveAttempted(move, reason);
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
            
            LOGGER.info("Move executed successfully: " + move);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occurred during move execution: " + move, e);
            errorLogger.logEngineException("executeMove", currentState, e);
            
            // Attempt to recover to previous state
            if (recoverGameState()) {
                LOGGER.info("Game state recovered successfully after error");
                errorLogger.logErrorRecovery("move_execution_error", "restore_previous_state", true);
                notifyInvalidMoveAttempted(move, "Internal error occurred, game state recovered");
            } else {
                LOGGER.severe("Failed to recover game state after error");
                errorLogger.logErrorRecovery("move_execution_error", "restore_previous_state", false);
                errorLogger.logGameStateCorruption(currentState, "Move execution failure", e);
                notifyGameStateCorrupted("Game state corrupted during move execution");
            }
            return false;
        }
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
        boolean isRed = !isBlack;
        
        // Back row pieces
        setPieceWithSide(new Position(backRow, 0), PieceFactory.createPiece(PieceType.CHARIOT, player, new Position(backRow, 0)), isRed);
        setPieceWithSide(new Position(backRow, 1), PieceFactory.createPiece(PieceType.HORSE, player, new Position(backRow, 1)), isRed);
        setPieceWithSide(new Position(backRow, 2), PieceFactory.createPiece(PieceType.ELEPHANT, player, new Position(backRow, 2)), isRed);
        setPieceWithSide(new Position(backRow, 3), PieceFactory.createPiece(PieceType.ADVISOR, player, new Position(backRow, 3)), isRed);
        setPieceWithSide(new Position(backRow, 4), PieceFactory.createPiece(PieceType.GENERAL, player, new Position(backRow, 4)), isRed);
        setPieceWithSide(new Position(backRow, 5), PieceFactory.createPiece(PieceType.ADVISOR, player, new Position(backRow, 5)), isRed);
        setPieceWithSide(new Position(backRow, 6), PieceFactory.createPiece(PieceType.ELEPHANT, player, new Position(backRow, 6)), isRed);
        setPieceWithSide(new Position(backRow, 7), PieceFactory.createPiece(PieceType.HORSE, player, new Position(backRow, 7)), isRed);
        setPieceWithSide(new Position(backRow, 8), PieceFactory.createPiece(PieceType.CHARIOT, player, new Position(backRow, 8)), isRed);
        
        // Cannons
        setPieceWithSide(new Position(cannonRow, 1), PieceFactory.createPiece(PieceType.CANNON, player, new Position(cannonRow, 1)), isRed);
        setPieceWithSide(new Position(cannonRow, 7), PieceFactory.createPiece(PieceType.CANNON, player, new Position(cannonRow, 7)), isRed);
        
        // Soldiers
        for (int col = 0; col < Position.BOARD_COLS; col += 2) {
            setPieceWithSide(new Position(soldierRow, col), PieceFactory.createPiece(PieceType.SOLDIER, player, new Position(soldierRow, col)), isRed);
        }
    }
    
    /**
     * Helper method to set a piece on the board and configure its side.
     */
    private void setPieceWithSide(Position position, ChessPiece piece, boolean isRed) {
        piece.setRedSide(isRed);
        currentState.setPiece(position, piece);
    }
    
    /**
     * Applies a move to the current game state.
     */
    private void applyMove(Move move) {
        // Remove piece from source position
        currentState.setPiece(move.getFrom(), null);
        
        // Update piece position and place at target position
        move.getPiece().setPosition(move.getTo());
        currentState.setPiece(move.getTo(), move.getPiece());
    }
    
    /**
     * Checks for game end conditions and updates game status accordingly.
     */
    private void checkGameEndConditions() {
        Player currentPlayer = currentState.getCurrentPlayer();
        Player opponent = currentState.getOpponent(currentPlayer);
        
        if (isCheckmate(currentPlayer)) {
            currentState.setStatus(GameStatus.CHECKMATE);
            GameResult result = GameResult.checkmate(opponent, currentPlayer);
            notifyGameEnded(result);
        } else if (isStalemate(currentPlayer)) {
            currentState.setStatus(GameStatus.STALEMATE);
            GameResult result = GameResult.draw(currentPlayer, opponent, "Stalemate");
            notifyGameEnded(result);
        } else if (ruleValidator.violatesFlyingGeneralRule(currentState)) {
            // The player who created the flying general situation loses
            Player previousPlayer = currentState.getOpponent(currentPlayer);
            currentState.setStatus(GameStatus.DRAW);
            GameResult result = new GameResult(currentPlayer, previousPlayer, GameStatus.DRAW, "Flying general rule violation");
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
    
    /**
     * Saves the current game state for potential recovery.
     */
    private void saveCurrentState() {
        if (currentState != null) {
            try {
                previousState = currentState.copy();
                LOGGER.fine("Game state saved for recovery");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to save game state for recovery", e);
                previousState = null;
            }
        }
    }
    
    /**
     * Recovers the game state to the previous saved state.
     * @return true if recovery was successful, false otherwise
     */
    public boolean recoverGameState() {
        if (previousState != null) {
            try {
                currentState = previousState.copy();
                notifyGameStateChanged();
                LOGGER.info("Game state recovered successfully");
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to recover game state", e);
            }
        }
        LOGGER.warning("No previous state available for recovery");
        return false;
    }
    
    /**
     * Gets a detailed reason why a move is invalid.
     */
    private String getInvalidMoveReason(Move move) {
        if (move == null) {
            return "Move is null";
        }
        
        if (currentState == null) {
            return "Game not initialized";
        }
        
        Position from = move.getFrom();
        Position to = move.getTo();
        ChessPiece piece = move.getPiece();
        
        if (!from.isValid()) {
            return "Invalid source position: " + from;
        }
        
        if (!to.isValid()) {
            return "Invalid target position: " + to;
        }
        
        ChessPiece pieceAtSource = currentState.getPiece(from);
        if (pieceAtSource == null) {
            return "No piece at source position: " + from;
        }
        
        if (!pieceAtSource.equals(piece)) {
            return "Piece mismatch at source position";
        }
        
        if (!piece.getOwner().equals(currentState.getCurrentPlayer())) {
            return "Not your turn - current player is " + currentState.getCurrentPlayer().getUsername();
        }
        
        if (!piece.canMoveTo(to, currentState)) {
            return "Piece cannot move to target position according to movement rules";
        }
        
        // Check if move would leave general in check
        if (ruleValidator.wouldLeaveGeneralInCheck(move, currentState)) {
            return "Move would leave your general in check";
        }
        
        return "Move violates game rules";
    }
    
    /**
     * Notifies listeners that the game state has been corrupted.
     */
    private void notifyGameStateCorrupted(String reason) {
        LOGGER.severe("Game state corrupted: " + reason);
        for (GameEventListener listener : eventListeners) {
            listener.onGameStateCorrupted(reason);
        }
    }
}