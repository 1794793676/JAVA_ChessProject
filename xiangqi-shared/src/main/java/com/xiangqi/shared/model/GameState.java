package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the complete state of a Xiangqi game.
 * Contains the board configuration, current player, game status, and move history.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final ChessPiece[][] board;
    private Player currentPlayer;
    private GameStatus status;
    private final List<Move> moveHistory;
    private Player redPlayer;
    private Player blackPlayer;
    
    public GameState() {
        this.board = new ChessPiece[Position.BOARD_ROWS][Position.BOARD_COLS];
        this.moveHistory = new ArrayList<>();
        this.status = GameStatus.WAITING_FOR_PLAYERS;
    }
    
    public GameState(Player redPlayer, Player blackPlayer) {
        this();
        this.redPlayer = Objects.requireNonNull(redPlayer, "Red player cannot be null");
        this.blackPlayer = Objects.requireNonNull(blackPlayer, "Black player cannot be null");
        this.currentPlayer = redPlayer; // Red moves first in Xiangqi
        this.status = GameStatus.IN_PROGRESS;
        initializeBoard(); // Initialize the board with pieces
    }
    
    public ChessPiece getPiece(Position position) {
        if (!position.isValid()) {
            return null;
        }
        return board[position.getRow()][position.getCol()];
    }
    
    public void setPiece(Position position, ChessPiece piece) {
        if (!position.isValid()) {
            throw new IllegalArgumentException("Invalid position: " + position);
        }
        board[position.getRow()][position.getCol()] = piece;
        if (piece != null) {
            piece.setPosition(position);
        }
    }
    
    public void removePiece(Position position) {
        setPiece(position, null);
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = Objects.requireNonNull(currentPlayer, "Current player cannot be null");
    }
    
    public void switchPlayer() {
        if (currentPlayer.equals(redPlayer)) {
            currentPlayer = blackPlayer;
        } else {
            currentPlayer = redPlayer;
        }
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public void setStatus(GameStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }
    
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
    
    public void addMove(Move move) {
        moveHistory.add(Objects.requireNonNull(move, "Move cannot be null"));
    }
    
    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }
    
    public Player getRedPlayer() {
        return redPlayer;
    }
    
    public void setRedPlayer(Player redPlayer) {
        this.redPlayer = Objects.requireNonNull(redPlayer, "Red player cannot be null");
    }
    
    public Player getBlackPlayer() {
        return blackPlayer;
    }
    
    public void setBlackPlayer(Player blackPlayer) {
        this.blackPlayer = Objects.requireNonNull(blackPlayer, "Black player cannot be null");
    }
    
    public Player getOpponent(Player player) {
        if (player.equals(redPlayer)) {
            return blackPlayer;
        } else if (player.equals(blackPlayer)) {
            return redPlayer;
        } else {
            throw new IllegalArgumentException("Player is not part of this game");
        }
    }
    
    /**
     * Checks if the given player is the red player.
     */
    public boolean isRedPlayer(Player player) {
        return player != null && player.equals(redPlayer);
    }
    
    /**
     * Checks if the given player is the black player.
     */
    public boolean isBlackPlayer(Player player) {
        return player != null && player.equals(blackPlayer);
    }
    
    /**
     * Executes a move on the board and updates game state.
     * @param move The move to execute
     * @return true if the move was executed successfully, false otherwise
     */
    public boolean executeMove(Move move) {
        if (move == null) {
            return false;
        }
        
        Position from = move.getFrom();
        Position to = move.getTo();
        
        // Validate positions
        if (!from.isValid() || !to.isValid()) {
            return false;
        }
        
        ChessPiece piece = getPiece(from);
        if (piece == null) {
            return false;
        }
        
        // Verify it's the correct player's turn
        if (!piece.getOwner().equals(currentPlayer)) {
            return false;
        }
        
        // Execute the move
        ChessPiece capturedPiece = getPiece(to);
        
        // Remove piece from old position
        removePiece(from);
        
        // Place piece at new position
        setPiece(to, piece);
        
        // Update move with captured piece if any
        if (capturedPiece != null && move.getCapturedPiece() == null) {
            move = new Move(from, to, piece, capturedPiece);
        }
        
        // Add to move history
        addMove(move);
        
        // Switch to next player
        switchPlayer();
        
        return true;
    }
    
    /**
     * Creates a deep copy of the current game state.
     */
    public GameState copy() {
        GameState copy = new GameState(redPlayer, blackPlayer);
        copy.currentPlayer = this.currentPlayer;
        copy.status = this.status;
        
        // Copy board state
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                copy.board[row][col] = this.board[row][col];
            }
        }
        
        // Copy move history
        copy.moveHistory.addAll(this.moveHistory);
        
        return copy;
    }
    
    /**
     * Initialize the board with all pieces in their starting positions.
     */
    public void initializeBoard() {
        // Clear the board first
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                board[row][col] = null;
            }
        }
        
        // Red pieces (bottom side, rows 7-9)
        // Chariots
        createAndPlaceRedPiece(PieceType.CHARIOT, 9, 0);
        createAndPlaceRedPiece(PieceType.CHARIOT, 9, 8);
        
        // Horses
        createAndPlaceRedPiece(PieceType.HORSE, 9, 1);
        createAndPlaceRedPiece(PieceType.HORSE, 9, 7);
        
        // Elephants
        createAndPlaceRedPiece(PieceType.ELEPHANT, 9, 2);
        createAndPlaceRedPiece(PieceType.ELEPHANT, 9, 6);
        
        // Advisors
        createAndPlaceRedPiece(PieceType.ADVISOR, 9, 3);
        createAndPlaceRedPiece(PieceType.ADVISOR, 9, 5);
        
        // General
        createAndPlaceRedPiece(PieceType.GENERAL, 9, 4);
        
        // Cannons
        createAndPlaceRedPiece(PieceType.CANNON, 7, 1);
        createAndPlaceRedPiece(PieceType.CANNON, 7, 7);
        
        // Soldiers
        createAndPlaceRedPiece(PieceType.SOLDIER, 6, 0);
        createAndPlaceRedPiece(PieceType.SOLDIER, 6, 2);
        createAndPlaceRedPiece(PieceType.SOLDIER, 6, 4);
        createAndPlaceRedPiece(PieceType.SOLDIER, 6, 6);
        createAndPlaceRedPiece(PieceType.SOLDIER, 6, 8);
        
        // Black pieces (top side, rows 0-2)
        // Chariots
        createAndPlaceBlackPiece(PieceType.CHARIOT, 0, 0);
        createAndPlaceBlackPiece(PieceType.CHARIOT, 0, 8);
        
        // Horses
        createAndPlaceBlackPiece(PieceType.HORSE, 0, 1);
        createAndPlaceBlackPiece(PieceType.HORSE, 0, 7);
        
        // Elephants
        createAndPlaceBlackPiece(PieceType.ELEPHANT, 0, 2);
        createAndPlaceBlackPiece(PieceType.ELEPHANT, 0, 6);
        
        // Advisors
        createAndPlaceBlackPiece(PieceType.ADVISOR, 0, 3);
        createAndPlaceBlackPiece(PieceType.ADVISOR, 0, 5);
        
        // General
        createAndPlaceBlackPiece(PieceType.GENERAL, 0, 4);
        
        // Cannons
        createAndPlaceBlackPiece(PieceType.CANNON, 2, 1);
        createAndPlaceBlackPiece(PieceType.CANNON, 2, 7);
        
        // Soldiers
        createAndPlaceBlackPiece(PieceType.SOLDIER, 3, 0);
        createAndPlaceBlackPiece(PieceType.SOLDIER, 3, 2);
        createAndPlaceBlackPiece(PieceType.SOLDIER, 3, 4);
        createAndPlaceBlackPiece(PieceType.SOLDIER, 3, 6);
        createAndPlaceBlackPiece(PieceType.SOLDIER, 3, 8);
    }
    
    /**
     * Helper method to create and place a red piece on the board.
     */
    private void createAndPlaceRedPiece(PieceType type, int row, int col) {
        Position pos = new Position(row, col);
        ChessPiece piece = PieceFactory.createPiece(type, redPlayer, pos);
        piece.setRedSide(true);
        setPiece(pos, piece);
    }
    
    /**
     * Helper method to create and place a black piece on the board.
     */
    private void createAndPlaceBlackPiece(PieceType type, int row, int col) {
        Position pos = new Position(row, col);
        ChessPiece piece = PieceFactory.createPiece(type, blackPlayer, pos);
        piece.setRedSide(false);
        setPiece(pos, piece);
    }
    
    @Override
    public String toString() {
        return "GameState{" +
                "currentPlayer=" + (currentPlayer != null ? currentPlayer.getUsername() : "null") +
                ", status=" + status +
                ", moveCount=" + moveHistory.size() +
                '}';
    }
}