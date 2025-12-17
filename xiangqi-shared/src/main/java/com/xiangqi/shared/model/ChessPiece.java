package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all Xiangqi chess pieces.
 * Defines common properties and behavior for all pieces.
 */
public abstract class ChessPiece implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected final PieceType type;
    protected final Player owner;
    protected Position position;
    protected boolean isRedSide; // Explicitly track if piece is on red side
    
    protected ChessPiece(PieceType type, Player owner, Position position) {
        this.type = Objects.requireNonNull(type, "Piece type cannot be null");
        this.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.isRedSide = false; // Default, will be set during board initialization
    }
    
    public PieceType getType() {
        return type;
    }
    
    public Player getOwner() {
        return owner;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null");
    }
    
    /**
     * Returns all valid moves for this piece given the current game state.
     * Subclasses must implement this method according to their movement rules.
     */
    public abstract List<Move> getValidMoves(GameState state);
    
    /**
     * Checks if this piece can move to the target position given the current game state.
     * Subclasses must implement this method according to their movement rules.
     */
    public abstract boolean canMoveTo(Position target, GameState state);
    
    /**
     * Checks if this piece belongs to the red side (bottom of board).
     */
    public boolean isRed() {
        return isRedSide;
    }
    
    /**
     * Sets whether this piece is on the red side.
     * Should be called during board initialization.
     */
    public void setRedSide(boolean isRedSide) {
        this.isRedSide = isRedSide;
    }
    
    /**
     * Checks if this piece belongs to the black side (top of board).
     */
    public boolean isBlack() {
        return !isRed();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessPiece that = (ChessPiece) obj;
        return type == that.type && 
               Objects.equals(owner, that.owner) && 
               Objects.equals(position, that.position);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, owner, position);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "type=" + type +
                ", owner=" + owner.getUsername() +
                ", position=" + position +
                '}';
    }
}