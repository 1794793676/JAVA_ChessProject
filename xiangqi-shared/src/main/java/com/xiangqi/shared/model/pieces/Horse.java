package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Horse (马) piece in Xiangqi.
 * The Horse moves in an L-shape (one point orthogonally then one point diagonally).
 * Its movement can be blocked by a piece at the intermediate orthogonal position.
 */
public class Horse extends ChessPiece {
    
    public Horse(Player owner, Position position) {
        super(PieceType.HORSE, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // Horse moves in L-shape: one orthogonal + one diagonal
        // 8 possible moves
        int[][] moves = {
            {-2, -1}, {-2, 1},  // Up 2, left/right 1
            {-1, -2}, {-1, 2},  // Up 1, left/right 2
            {1, -2}, {1, 2},    // Down 1, left/right 2
            {2, -1}, {2, 1}     // Down 2, left/right 1
        };
        
        for (int[] move : moves) {
            int newRow = currentPos.getRow() + move[0];
            int newCol = currentPos.getCol() + move[1];
            
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
        
        Position currentPos = getPosition();
        int rowDiff = target.getRow() - currentPos.getRow();
        int colDiff = target.getCol() - currentPos.getCol();
        
        // Check if it's a valid L-shaped move
        boolean validLShape = (Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 1) ||
                             (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 2);
        
        if (!validLShape) {
            return false;
        }
        
        // Check if the horse is blocked at the intermediate orthogonal position
        Position blockingPos = getBlockingPosition(currentPos, target);
        if (blockingPos != null && state.getPiece(blockingPos) != null) {
            return false; // Horse is blocked
        }
        
        // Cannot capture own pieces
        ChessPiece targetPiece = state.getPiece(target);
        if (targetPiece != null && targetPiece.getOwner().equals(getOwner())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the position that would block the horse's movement.
     * The horse moves one point orthogonally first, then one point diagonally.
     */
    private Position getBlockingPosition(Position from, Position to) {
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();
        
        // Determine the intermediate orthogonal position
        int blockRow, blockCol;
        
        if (Math.abs(rowDiff) == 2) {
            // Moving 2 rows, 1 column - blocked at 1 row movement
            blockRow = from.getRow() + (rowDiff > 0 ? 1 : -1);
            blockCol = from.getCol();
        } else {
            // Moving 1 row, 2 columns - blocked at 1 column movement
            blockRow = from.getRow();
            blockCol = from.getCol() + (colDiff > 0 ? 1 : -1);
        }
        
        try {
            return new Position(blockRow, blockCol);
        } catch (IllegalArgumentException e) {
            return null; // Invalid blocking position
        }
    }
}