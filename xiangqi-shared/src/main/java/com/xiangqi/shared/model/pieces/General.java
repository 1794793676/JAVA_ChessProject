package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the General (将/帅) piece in Xiangqi.
 * The General can only move one point orthogonally and must stay within the palace.
 */
public class General extends ChessPiece {
    
    public General(Player owner, Position position) {
        super(PieceType.GENERAL, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // General moves one point orthogonally (up, down, left, right)
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newRow = currentPos.getRow() + dir[0];
            int newCol = currentPos.getCol() + dir[1];
            
            // Check if new position is within board bounds before creating Position object
            if (newRow >= 0 && newRow < Position.BOARD_ROWS && 
                newCol >= 0 && newCol < Position.BOARD_COLS) {
                Position newPos = new Position(newRow, newCol);
                
                if (canMoveTo(newPos, state)) {
                    ChessPiece capturedPiece = state.getPiece(newPos);
                    validMoves.add(new Move(currentPos, newPos, this, capturedPiece));
                }
            }
        }
        
        return validMoves;
    }
    
    @Override
    public boolean canMoveTo(Position target, GameState state) {
        if (!target.isValid()) {
            return false;
        }
        
        // Must stay within palace
        if (!isInPalace(target)) {
            return false;
        }
        
        // Can only move one point orthogonally
        Position currentPos = getPosition();
        int rowDiff = Math.abs(target.getRow() - currentPos.getRow());
        int colDiff = Math.abs(target.getCol() - currentPos.getCol());
        
        if (!((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1))) {
            return false;
        }
        
        // Cannot capture own pieces
        ChessPiece targetPiece = state.getPiece(target);
        if (targetPiece != null && targetPiece.getOwner().equals(getOwner())) {
            return false;
        }
        
        // Check if this move would leave the general in check or create flying general situation
        return !wouldBeInCheckAfterMove(target, state) && !wouldCreateFlyingGeneral(target, state);
    }
    
    /**
     * Checks if the position is within the palace (3x3 area in the center).
     */
    private boolean isInPalace(Position pos) {
        int row = pos.getRow();
        int col = pos.getCol();
        
        // Red palace: rows 7-9, cols 3-5
        // Black palace: rows 0-2, cols 3-5
        if (isRed()) {
            return row >= 7 && row <= 9 && col >= 3 && col <= 5;
        } else {
            return row >= 0 && row <= 2 && col >= 3 && col <= 5;
        }
    }
    
    /**
     * Checks if moving to the target would leave this general in check.
     */
    private boolean wouldBeInCheckAfterMove(Position target, GameState state) {
        // Create a temporary state with the move applied
        GameState tempState = state.copy();
        tempState.setPiece(getPosition(), null);
        tempState.setPiece(target, this);
        
        // Check if any opponent piece can attack the general at the new position
        Player opponent = state.getOpponent(getOwner());
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                Position pos = new Position(row, col);
                ChessPiece piece = tempState.getPiece(pos);
                if (piece != null && piece.getOwner().equals(opponent)) {
                    // For opponent general, check basic move rules without recursive check validation
                    if (piece instanceof General) {
                        if (canBasicMoveTo(piece, target, tempState)) {
                            return true;
                        }
                    } else {
                        if (piece.canMoveTo(target, tempState)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Check if a general can move to target based on basic rules only (no check/flying general validation).
     */
    private boolean canBasicMoveTo(ChessPiece general, Position target, GameState state) {
        Position currentPos = general.getPosition();
        
        // Check if target is valid
        if (!target.isValid()) {
            return false;
        }
        
        // Must stay within palace
        boolean isRedGeneral = general.isRed();
        int row = target.getRow();
        int col = target.getCol();
        boolean inPalace;
        if (isRedGeneral) {
            inPalace = row >= 7 && row <= 9 && col >= 3 && col <= 5;
        } else {
            inPalace = row >= 0 && row <= 2 && col >= 3 && col <= 5;
        }
        if (!inPalace) {
            return false;
        }
        
        // Can only move one point orthogonally
        int rowDiff = Math.abs(target.getRow() - currentPos.getRow());
        int colDiff = Math.abs(target.getCol() - currentPos.getCol());
        if (!((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1))) {
            return false;
        }
        
        // Cannot capture own pieces
        ChessPiece targetPiece = state.getPiece(target);
        if (targetPiece != null && targetPiece.getOwner().equals(general.getOwner())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if moving to the target would create a "flying general" situation
     * (both generals facing each other with no pieces between them).
     */
    private boolean wouldCreateFlyingGeneral(Position target, GameState state) {
        // Find the opponent's general
        General opponentGeneral = findOpponentGeneral(state);
        if (opponentGeneral == null) {
            return false;
        }
        
        Position opponentPos = opponentGeneral.getPosition();
        
        // Check if they would be in the same column
        if (target.getCol() != opponentPos.getCol()) {
            return false;
        }
        
        // Create a temporary state with the move applied to check pieces between
        GameState tempState = state.copy();
        tempState.setPiece(getPosition(), null);
        tempState.setPiece(target, this);
        
        // Check if there are any pieces between them in the temp state
        int minRow = Math.min(target.getRow(), opponentPos.getRow());
        int maxRow = Math.max(target.getRow(), opponentPos.getRow());
        
        for (int row = minRow + 1; row < maxRow; row++) {
            Position between = new Position(row, target.getCol());
            if (tempState.getPiece(between) != null) {
                return false; // There's a piece between them
            }
        }
        
        return true; // Flying general situation would occur
    }
    
    /**
     * Finds the opponent's general on the board.
     */
    private General findOpponentGeneral(GameState state) {
        Player opponent = state.getOpponent(getOwner());
        for (int row = 0; row < Position.BOARD_ROWS; row++) {
            for (int col = 0; col < Position.BOARD_COLS; col++) {
                Position pos = new Position(row, col);
                ChessPiece piece = state.getPiece(pos);
                if (piece instanceof General && piece.getOwner().equals(opponent)) {
                    return (General) piece;
                }
            }
        }
        return null;
    }
}