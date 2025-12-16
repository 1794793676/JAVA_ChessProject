package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Chariot (车) piece in Xiangqi.
 * The Chariot moves any number of points orthogonally (horizontally or vertically)
 * as long as the path is clear.
 */
public class Chariot extends ChessPiece {
    
    public Chariot(Player owner, Position position) {
        super(PieceType.CHARIOT, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // Chariot moves orthogonally in four directions
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            // Check all positions in this direction until blocked or edge of board
            for (int distance = 1; distance < Math.max(Position.BOARD_ROWS, Position.BOARD_COLS); distance++) {
                int newRow = currentPos.getRow() + dir[0] * distance;
                int newCol = currentPos.getCol() + dir[1] * distance;
                
                try {
                    Position newPos = new Position(newRow, newCol);
                    
                    ChessPiece pieceAtTarget = state.getPiece(newPos);
                    
                    if (pieceAtTarget == null) {
                        // Empty square - valid move
                        validMoves.add(new Move(currentPos, newPos, this));
                    } else if (!pieceAtTarget.getOwner().equals(getOwner())) {
                        // Enemy piece - can capture, but cannot move further
                        validMoves.add(new Move(currentPos, newPos, this, pieceAtTarget));
                        break;
                    } else {
                        // Own piece - cannot move here or further
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    // Out of bounds - stop in this direction
                    break;
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
        
        Position currentPos = getPosition();
        
        // Must move orthogonally (same row or same column)
        if (currentPos.getRow() != target.getRow() && currentPos.getCol() != target.getCol()) {
            return false;
        }
        
        // Cannot move to same position
        if (currentPos.equals(target)) {
            return false;
        }
        
        // Check if path is clear
        if (!isPathClear(currentPos, target, state)) {
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
     * Checks if the path between two positions is clear (no pieces blocking).
     */
    private boolean isPathClear(Position from, Position to, GameState state) {
        int rowStep = Integer.compare(to.getRow(), from.getRow());
        int colStep = Integer.compare(to.getCol(), from.getCol());
        
        int currentRow = from.getRow() + rowStep;
        int currentCol = from.getCol() + colStep;
        
        // Check each position along the path (excluding start and end)
        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            Position checkPos = new Position(currentRow, currentCol);
            if (state.getPiece(checkPos) != null) {
                return false; // Path is blocked
            }
            
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }
}