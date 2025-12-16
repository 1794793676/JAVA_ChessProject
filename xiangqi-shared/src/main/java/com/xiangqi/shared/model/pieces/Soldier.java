package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Soldier (兵/卒) piece in Xiangqi.
 * The Soldier can only move forward before crossing the river.
 * After crossing the river, it can also move sideways but never backward.
 */
public class Soldier extends ChessPiece {
    
    public Soldier(Player owner, Position position) {
        super(PieceType.SOLDIER, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // Determine possible moves based on whether soldier has crossed the river
        List<int[]> possibleMoves = new ArrayList<>();
        
        // Forward move is always possible
        if (isRed()) {
            possibleMoves.add(new int[]{-1, 0}); // Red moves up (decreasing row)
        } else {
            possibleMoves.add(new int[]{1, 0});  // Black moves down (increasing row)
        }
        
        // Sideways moves only after crossing the river
        if (hasCrossedRiver()) {
            possibleMoves.add(new int[]{0, -1}); // Left
            possibleMoves.add(new int[]{0, 1});  // Right
        }
        
        for (int[] move : possibleMoves) {
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
        
        // Can only move one point at a time
        if (Math.abs(rowDiff) + Math.abs(colDiff) != 1) {
            return false;
        }
        
        // Check movement direction constraints
        if (isRed()) {
            // Red soldiers move up (decreasing row) or sideways after crossing river
            if (rowDiff > 0) {
                return false; // Cannot move backward (down)
            }
            if (rowDiff == 0 && !hasCrossedRiver()) {
                return false; // Cannot move sideways before crossing river
            }
        } else {
            // Black soldiers move down (increasing row) or sideways after crossing river
            if (rowDiff < 0) {
                return false; // Cannot move backward (up)
            }
            if (rowDiff == 0 && !hasCrossedRiver()) {
                return false; // Cannot move sideways before crossing river
            }
        }
        
        // Cannot capture own pieces
        ChessPiece targetPiece = state.getPiece(target);
        if (targetPiece != null && targetPiece.getOwner().equals(getOwner())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if this soldier has crossed the river (middle line of the board).
     */
    private boolean hasCrossedRiver() {
        int row = getPosition().getRow();
        
        if (isRed()) {
            // Red soldiers cross the river when they reach rows 0-4 (top half)
            return row <= 4;
        } else {
            // Black soldiers cross the river when they reach rows 5-9 (bottom half)
            return row >= 5;
        }
    }
}