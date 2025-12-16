package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a move in the Xiangqi game.
 * Contains source position, target position, piece being moved, and any captured piece.
 */
public class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Position from;
    private final Position to;
    private final ChessPiece piece;
    private final ChessPiece capturedPiece;
    private final long timestamp;
    
    public Move(Position from, Position to, ChessPiece piece) {
        this(from, to, piece, null);
    }
    
    public Move(Position from, Position to, ChessPiece piece, ChessPiece capturedPiece) {
        this.from = Objects.requireNonNull(from, "From position cannot be null");
        this.to = Objects.requireNonNull(to, "To position cannot be null");
        this.piece = Objects.requireNonNull(piece, "Piece cannot be null");
        this.capturedPiece = capturedPiece; // Can be null if no capture
        this.timestamp = System.currentTimeMillis();
    }
    
    public Position getFrom() {
        return from;
    }
    
    public Position getTo() {
        return to;
    }
    
    public ChessPiece getPiece() {
        return piece;
    }
    
    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isCapture() {
        return capturedPiece != null;
    }
    
    /**
     * Returns the algebraic notation for this move (simplified version).
     */
    public String toAlgebraicNotation() {
        StringBuilder notation = new StringBuilder();
        notation.append(piece.getType().name().charAt(0));
        notation.append(from.getCol()).append(from.getRow());
        if (isCapture()) {
            notation.append("x");
        } else {
            notation.append("-");
        }
        notation.append(to.getCol()).append(to.getRow());
        return notation.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return Objects.equals(from, move.from) &&
               Objects.equals(to, move.to) &&
               Objects.equals(piece, move.piece) &&
               Objects.equals(capturedPiece, move.capturedPiece);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(from, to, piece, capturedPiece);
    }
    
    @Override
    public String toString() {
        return "Move{" +
                "from=" + from +
                ", to=" + to +
                ", piece=" + piece.getType() +
                (isCapture() ? ", captures=" + capturedPiece.getType() : "") +
                '}';
    }
}