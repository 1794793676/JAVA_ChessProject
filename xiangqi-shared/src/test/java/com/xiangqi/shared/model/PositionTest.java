package com.xiangqi.shared.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Position class to verify board coordinate handling.
 */
class PositionTest {
    
    @Test
    void testValidPositionCreation() {
        Position position = new Position(0, 0);
        assertEquals(0, position.getRow());
        assertEquals(0, position.getCol());
        assertTrue(position.isValid());
    }
    
    @Test
    void testBoundaryPositions() {
        // Test valid boundary positions
        Position topLeft = new Position(0, 0);
        Position bottomRight = new Position(9, 8);
        
        assertTrue(topLeft.isValid());
        assertTrue(bottomRight.isValid());
    }
    
    @Test
    void testInvalidPositions() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Position(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, 9));
    }
    
    @Test
    void testPositionEquality() {
        Position pos1 = new Position(3, 4);
        Position pos2 = new Position(3, 4);
        Position pos3 = new Position(3, 5);
        
        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }
    
    @Test
    void testDistanceCalculation() {
        Position pos1 = new Position(0, 0);
        Position pos2 = new Position(3, 4);
        
        assertEquals(7, pos1.distanceTo(pos2)); // Manhattan distance: |0-3| + |0-4| = 7
        assertEquals(7, pos2.distanceTo(pos1)); // Should be symmetric
        assertEquals(0, pos1.distanceTo(pos1)); // Distance to self is 0
    }
}