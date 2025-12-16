package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Advisor (士/仕) piece in Xiangqi.
 * The Advisor can only move one point diagonally and must stay within the palace.
 */
public class Advisor extends ChessPiece {
    
    public Advisor(Player owner, Position position) {
        super(PieceType.ADVISOR, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // Advisor moves one point diagonally
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            int newRow = currentPos.getRow() + dir[0];
            int newCol = currentPos.getCol() + dir[1];
            
            try {
                Position newPos = new Position(newRow, newCol);
                if (canMoveTo(newPos, state)) {
                    ChessPiece capturedPiece = state.getPiece(newPos);
                    validMoves.add(new Move(currentPos, newPos, this, capturedPiece));
                }
            } catch (IllegalArgumentException e) {
                // Invalid position, skip
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
        
        // Can only move one point diagonally
        Position currentPos = getPosition();
        int rowDiff = Math.abs(target.getRow() - currentPos.getRow());
        int colDiff = Math.abs(target.getCol() - currentPos.getCol());
        
        if (rowDiff != 1 || colDiff != 1) {
            return false;
        }
        
        // Cannot capture own pieces
        ChessPiece targetPiece = state.getPiece(target);
        if (targetPiece != null && targetPiece.getOwner().equals(getOwner())) {
            return false;
        }
        
        return true;
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
}