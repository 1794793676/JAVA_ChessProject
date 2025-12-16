package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Elephant (象/相) piece in Xiangqi.
 * The Elephant moves exactly two points diagonally and cannot cross the river.
 * Its movement can be blocked by a piece at the intermediate diagonal position.
 */
public class Elephant extends ChessPiece {
    
    public Elephant(Player owner, Position position) {
        super(PieceType.ELEPHANT, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // Elephant moves exactly two points diagonally
        int[][] directions = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        
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
        
        // Cannot cross the river
        if (!isOnCorrectSideOfRiver(target)) {
            return false;
        }
        
        Position currentPos = getPosition();
        int rowDiff = target.getRow() - currentPos.getRow();
        int colDiff = target.getCol() - currentPos.getCol();
        
        // Must move exactly two points diagonally
        if (Math.abs(rowDiff) != 2 || Math.abs(colDiff) != 2) {
            return false;
        }
        
        // Check if the intermediate diagonal position is blocked
        int midRow = currentPos.getRow() + rowDiff / 2;
        int midCol = currentPos.getCol() + colDiff / 2;
        Position midPos = new Position(midRow, midCol);
        
        if (state.getPiece(midPos) != null) {
            return false; // Blocked by piece at intermediate position
        }
        
        // Cannot capture own pieces
        ChessPiece targetPiece = state.getPiece(target);
        if (targetPiece != null && targetPiece.getOwner().equals(getOwner())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the position is on the correct side of the river for this elephant.
     * Elephants cannot cross the river (middle line of the board).
     */
    private boolean isOnCorrectSideOfRiver(Position pos) {
        int row = pos.getRow();
        
        if (isRed()) {
            // Red elephants stay on rows 5-9 (bottom half)
            return row >= 5;
        } else {
            // Black elephants stay on rows 0-4 (top half)
            return row <= 4;
        }
    }
}