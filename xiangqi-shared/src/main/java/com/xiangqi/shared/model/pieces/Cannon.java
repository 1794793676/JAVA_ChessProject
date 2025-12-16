package com.xiangqi.shared.model.pieces;

import com.xiangqi.shared.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Cannon (炮/砲) piece in Xiangqi.
 * The Cannon moves like a Chariot when not capturing, but needs exactly one piece
 * to jump over when capturing (the "platform" piece).
 */
public class Cannon extends ChessPiece {
    
    public Cannon(Player owner, Position position) {
        super(PieceType.CANNON, owner, position);
    }
    
    @Override
    public List<Move> getValidMoves(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        Position currentPos = getPosition();
        
        // Cannon moves orthogonally in four directions
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            boolean foundPlatform = false;
            
            // Check all positions in this direction
            for (int distance = 1; distance < Math.max(Position.BOARD_ROWS, Position.BOARD_COLS); distance++) {
                int newRow = currentPos.getRow() + dir[0] * distance;
                int newCol = currentPos.getCol() + dir[1] * distance;
                
                try {
                    Position newPos = new Position(newRow, newCol);
                    ChessPiece pieceAtTarget = state.getPiece(newPos);
                    
                    if (!foundPlatform) {
                        // Before finding a platform piece
                        if (pieceAtTarget == null) {
                            // Empty square - can move here (non-capturing move)
                            validMoves.add(new Move(currentPos, newPos, this));
                        } else {
                            // Found a piece - this becomes our platform
                            foundPlatform = true;
                        }
                    } else {
                        // After finding a platform piece
                        if (pieceAtTarget == null) {
                            // Empty square after platform - cannot move here
                            continue;
                        } else if (!pieceAtTarget.getOwner().equals(getOwner())) {
                            // Enemy piece after platform - can capture
                            validMoves.add(new Move(currentPos, newPos, this, pieceAtTarget));
                            break; // Cannot move further after capture
                        } else {
                            // Own piece after platform - cannot capture, stop here
                            break;
                        }
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
        
        ChessPiece targetPiece = state.getPiece(target);
        
        // Cannot capture own pieces
        if (targetPiece != null && targetPiece.getOwner().equals(getOwner())) {
            return false;
        }
        
        // Count pieces between current position and target
        int piecesBetween = countPiecesBetween(currentPos, target, state);
        
        if (targetPiece == null) {
            // Non-capturing move - path must be clear (0 pieces between)
            return piecesBetween == 0;
        } else {
            // Capturing move - must have exactly 1 piece between (the platform)
            return piecesBetween == 1;
        }
    }
    
    /**
     * Counts the number of pieces between two positions on the same row or column.
     */
    private int countPiecesBetween(Position from, Position to, GameState state) {
        int rowStep = Integer.compare(to.getRow(), from.getRow());
        int colStep = Integer.compare(to.getCol(), from.getCol());
        
        int currentRow = from.getRow() + rowStep;
        int currentCol = from.getCol() + colStep;
        int count = 0;
        
        // Count pieces along the path (excluding start and end)
        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            Position checkPos = new Position(currentRow, currentCol);
            if (state.getPiece(checkPos) != null) {
                count++;
            }
            
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return count;
    }
}