package com.xiangqi.client.multimedia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ResourceManager functionality.
 */
public class ResourceManagerTest {
    
    private ResourceManager resourceManager;
    
    @BeforeEach
    void setUp() {
        resourceManager = ResourceManager.getInstance();
    }
    
    @Test
    void testSingletonInstance() {
        ResourceManager instance1 = ResourceManager.getInstance();
        ResourceManager instance2 = ResourceManager.getInstance();
        assertSame(instance1, instance2, "ResourceManager should be a singleton");
    }
    
    @Test
    void testGetAudioManager() {
        AudioManager audioManager = resourceManager.getAudioManager();
        assertNotNull(audioManager, "AudioManager should not be null");
        assertSame(AudioManager.getInstance(), audioManager, "Should return the same AudioManager instance");
    }
    
    @Test
    void testGetPieceImage() {
        // Test valid piece numbers
        for (int i = 1; i <= 32; i++) {
            final int pieceNumber = i; // Make variable effectively final for lambda
            assertDoesNotThrow(() -> resourceManager.getPieceImage(pieceNumber), 
                "getPieceImage should not throw exception for valid piece number: " + pieceNumber);
        }
    }
    
    @Test
    void testGetPieceImageInvalidNumbers() {
        // Test invalid piece numbers
        assertNull(resourceManager.getPieceImage(0), "Should return null for piece number 0");
        assertNull(resourceManager.getPieceImage(-1), "Should return null for negative piece number");
        assertNull(resourceManager.getPieceImage(33), "Should return null for piece number > 32");
    }
    
    @Test
    void testGetBoardImage() {
        assertDoesNotThrow(() -> resourceManager.getBoardImage(), "getBoardImage should not throw exception");
    }
    
    @Test
    void testGetSelectImage() {
        assertDoesNotThrow(() -> resourceManager.getSelectImage(), "getSelectImage should not throw exception");
    }
    
    @Test
    void testGetLogoImage() {
        assertDoesNotThrow(() -> resourceManager.getLogoImage(), "getLogoImage should not throw exception");
    }
    
    @Test
    void testGetPlayerAvatar() {
        assertDoesNotThrow(() -> resourceManager.getPlayerAvatar(), "getPlayerAvatar should not throw exception");
    }
    
    @Test
    void testGetNoPlayerImage() {
        assertDoesNotThrow(() -> resourceManager.getNoPlayerImage(), "getNoPlayerImage should not throw exception");
    }
    
    @Test
    void testGetScaledImage() {
        // Test scaling with a known image name
        assertDoesNotThrow(() -> resourceManager.getScaledImage("1.gif", 50, 50), 
            "getScaledImage should not throw exception");
    }
    
    @Test
    void testAddResourcePath() {
        String testResource = "test_resource.gif";
        String testPath = "test/path/test_resource.gif";
        
        assertDoesNotThrow(() -> resourceManager.addResourcePath(testResource, testPath), 
            "addResourcePath should not throw exception");
    }
    
    @Test
    void testResourceExists() {
        // Test with a resource that should exist (even if file doesn't exist, method should not throw)
        assertDoesNotThrow(() -> resourceManager.resourceExists("1.gif"), 
            "resourceExists should not throw exception");
        
        // Test with non-existent resource
        assertFalse(resourceManager.resourceExists("non_existent_resource.gif"), 
            "Should return false for non-existent resource");
    }
    
    @Test
    void testGetCacheStats() {
        String stats = resourceManager.getCacheStats();
        assertNotNull(stats, "Cache stats should not be null");
        assertTrue(stats.contains("Images cached:"), "Stats should contain image cache info");
        assertTrue(stats.contains("Resource paths:"), "Stats should contain resource path info");
    }
    
    @Test
    void testClearCache() {
        assertDoesNotThrow(() -> resourceManager.clearCache(), "clearCache should not throw exception");
    }
    
    @Test
    void testCleanup() {
        assertDoesNotThrow(() -> resourceManager.cleanup(), "cleanup should not throw exception");
    }
}