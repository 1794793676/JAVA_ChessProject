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
    
    @Override
    public String toString() {
        return "GameState{" +
                "currentPlayer=" + (currentPlayer != null ? currentPlayer.getUsername() : "null") +
                ", status=" + status +
                ", moveCount=" + moveHistory.size() +
                '}';
    }
}