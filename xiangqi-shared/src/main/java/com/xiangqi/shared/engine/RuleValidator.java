package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.model.pieces.*;

/**
 * Validates Xiangqi game rules and move legality.
 * Centralizes all rule checking logic for the game.
 */
public class RuleValidator {
    
    /**
     * Validates if a move is legal according to Xiangqi rules.
     */
    public boolean isValidMove(Move move, GameState state) {
        if (move == null || state == null) {
            return false;
        }
        
        Position from = move.getFrom();
        Position to = move.getTo();
        ChessPiece piece = move.getPiece();
        
        // Basic validation
        if (!from.isValid() || !to.isValid()) {
            return false;
        }
        
        // Check if piece exists at source position
        ChessPiece pieceAtSource = state.getPiece(from);
        if (pieceAtSource == null || !pieceAtSource.equals(piece)) {
            return false;
        }
        
        // Check if it's the correct player's turn
        if (!piece.getOwner().equals(state.getCurrentPlayer())) {
            return false;
        }
        
        // Check if the piece can make this move
        if (!piece.canMoveTo(to, state)) {
            return false;
        }
        
        // Check if this move would leave own general in check
        if (wouldLeaveGeneralInCheck(move, state)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a player's general is currently in check.
     */
    public boolean isInCheck(Player player, GameState state) {
        General general = findGeneral(player, state);
        if (general == null) {
            return false; // No general found (shouldn't happen in normal game)
        }
        
        Position generalPos = general.getPosition();
        Player opponent = state.getOpponent(player);
        
        // Check if any opponent piece can attack the general
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                Position pos = new Position(row, col);
                ChessPiece piece = state.getPiece(pos);
                
                if (piece != null && piece.getOwner().equals(opponent)) {
                    if (piece.canMoveTo(generalPos, state)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a player is in checkmate.
     */
    public boolean isCheckmate(Player player, GameState state) {
        if (!isInCheck(player, state)) {
            return false; // Not in check, so not checkmate
        }
        
        // Check if player has any legal moves to escape check
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                Position pos = new Position(row, col);
                ChessPiece piece = state.getPiece(pos);
                
                if (piece != null && piece.getOwner().equals(player)) {
                    // Try all possible moves for this piece
                    for (Move possibleMove : piece.getValidMoves(state)) {
                        if (isValidMove(possibleMove, state)) {
                            return false; // Found a legal move, not checkmate
                        }
                    }
                }
            }
        }
        
        return true; // No legal moves found, it's checkmate
    }
    
    /**
     * Checks if the game is in stalemate (no legal moves but not in check).
     */
    public boolean isStalemate(Player player, GameState state) {
        if (isInCheck(player, state)) {
            return false; // In check, so not stalemate
        }
        
        // Check if player has any legal moves
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                Position pos = new Position(row, col);
                ChessPiece piece = state.getPiece(pos);
                
                if (piece != null && piece.getOwner().equals(player)) {
                    // Try all possible moves for this piece
                    for (Move possibleMove : piece.getValidMoves(state)) {
                        if (isValidMove(possibleMove, state)) {
                            return false; // Found a legal move, not stalemate
                        }
                    }
                }
            }
        }
        
        return true; // No legal moves found and not in check, it's stalemate
    }
    
    /**
     * Checks if making a move would leave the player's own general in check.
     */
    private boolean wouldLeaveGeneralInCheck(Move move, GameState state) {
        // Create a temporary state with the move applied
        GameState tempState = state.copy();
        
        // Apply the move to the temporary state
        ChessPiece capturedPiece = tempState.getPiece(move.getTo());
        tempState.setPiece(move.getFrom(), null);
        tempState.setPiece(move.getTo(), move.getPiece());
        
        // Check if the player's general is in check in this new state
        return isInCheck(move.getPiece().getOwner(), tempState);
    }
    
    /**
     * Finds a player's general on the board.
     */
    private General findGeneral(Player player, GameState state) {
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                Position pos = new Position(row, col);
                ChessPiece piece = state.getPiece(pos);
                
                if (piece instanceof General && piece.getOwner().equals(player)) {
                    return (General) piece;
                }
            }
        }
        return null;
    }
    
    /**
     * Validates the flying general rule - generals cannot face each other
     * with no pieces between them on the same column.
     */
    public boolean violatesFlyingGeneralRule(GameState state) {
        General redGeneral = findGeneral(state.getRedPlayer(), state);
        General blackGeneral = findGeneral(state.getBlackPlayer(), state);
        
        if (redGeneral == null || blackGeneral == null) {
            return false;
        }
        
        Position redPos = redGeneral.getPosition();
        Position blackPos = blackGeneral.getPosition();
        
        // Check if they are in the same column
        if (redPos.getCol() != blackPos.getCol()) {
            return false;
        }
        
        // Check if there are any pieces between them
        int minRow = Math.min(redPos.getRow(), blackPos.getRow());
        int maxRow = Math.max(redPos.getRow(), blackPos.getRow());
        
        for (int row = minRow + 1; row < maxRow; row++) {
            Position between = new Position(row, redPos.getCol());
            if (state.getPiece(between) != null) {
                return false; // There's a piece between them
            }
        }
        
        return true; // Flying general rule is violated
    }
}