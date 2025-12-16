package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a position on the Xiangqi board.
 * Uses 0-based indexing: row 0-9 (top to bottom), column 0-8 (left to right).
 */
public class Position implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final int BOARD_ROWS = 10;
    public static final int BOARD_COLS = 9;
    
    private final int row;
    private final int col;
    
    public Position(int row, int col) {
        if (row < 0 || row >= BOARD_ROWS) {
            throw new IllegalArgumentException("Row must be between 0 and " + (BOARD_ROWS - 1));
        }
        if (col < 0 || col >= BOARD_COLS) {
            throw new IllegalArgumentException("Column must be between 0 and " + (BOARD_COLS - 1));
        }
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    /**
     * Checks if this position is valid on the Xiangqi board.
     */
    public boolean isValid() {
        return row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS;
    }
    
    /**
     * Calculates the Manhattan distance to another position.
     */
    public int distanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    
    @Override
    public String toString() {
        return "Position{" + row + "," + col + "}";
    }
}