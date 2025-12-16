package com.xiangqi.shared.model;

import com.xiangqi.shared.model.pieces.*;

/**
 * Factory class for creating Xiangqi chess pieces.
 */
public class PieceFactory {
    
    /**
     * Creates a chess piece of the specified type for the given owner at the given position.
     */
    public static ChessPiece createPiece(PieceType type, Player owner, Position position) {
        switch (type) {
            case GENERAL:
                return new General(owner, position);
            case ADVISOR:
                return new Advisor(owner, position);
            case ELEPHANT:
                return new Elephant(owner, position);
            case HORSE:
                return new Horse(owner, position);
            case CHARIOT:
                return new Chariot(owner, position);
            case CANNON:
                return new Cannon(owner, position);
            case SOLDIER:
                return new Soldier(owner, position);
            default:
                throw new IllegalArgumentException("Unknown piece type: " + type);
        }
    }
}