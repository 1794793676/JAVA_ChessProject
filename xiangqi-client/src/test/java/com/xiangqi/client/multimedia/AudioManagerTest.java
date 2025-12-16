package com.xiangqi.client.multimedia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AudioManager functionality.
 */
public class AudioManagerTest {
    
    private AudioManager audioManager;
    
    @BeforeEach
    void setUp() {
        audioManager = AudioManager.getInstance();
        // Reset to default state for each test
        audioManager.setSoundEnabled(true);
    }
    
    @Test
    void testSingletonInstance() {
        AudioManager instance1 = AudioManager.getInstance();
        AudioManager instance2 = AudioManager.getInstance();
        assertSame(instance1, instance2, "AudioManager should be a singleton");
    }
    
    @Test
    void testSoundEnabledByDefault() {
        assertTrue(audioManager.isSoundEnabled(), "Sound should be enabled by default");
    }
    
    @Test
    void testSetSoundEnabled() {
        audioManager.setSoundEnabled(false);
        assertFalse(audioManager.isSoundEnabled(), "Sound should be disabled after setting to false");
        
        audioManager.setSoundEnabled(true);
        assertTrue(audioManager.isSoundEnabled(), "Sound should be enabled after setting to true");
    }
    
    @Test
    void testPlaySoundMethods() {
        // These methods should not throw exceptions even if audio files are not found
        assertDoesNotThrow(() -> audioManager.playMoveSound(), "playMoveSound should not throw exception");
        assertDoesNotThrow(() -> audioManager.playCaptureSound(), "playCaptureSound should not throw exception");
        assertDoesNotThrow(() -> audioManager.playSelectSound(), "playSelectSound should not throw exception");
        assertDoesNotThrow(() -> audioManager.playCheckSound(), "playCheckSound should not throw exception");
        assertDoesNotThrow(() -> audioManager.playPlayerJoinSound(), "playPlayerJoinSound should not throw exception");
    }
    
    @Test
    void testStopAllSounds() {
        assertDoesNotThrow(() -> audioManager.stopAllSounds(), "stopAllSounds should not throw exception");
    }
    
    @Test
    void testCleanup() {
        assertDoesNotThrow(() -> audioManager.cleanup(), "cleanup should not throw exception");
    }
    
    @Test
    void testPlaySoundWhenDisabled() {
        audioManager.setSoundEnabled(false);
        // Should not throw exceptions even when sound is disabled
        assertDoesNotThrow(() -> audioManager.playMoveSound(), "playMoveSound should not throw exception when disabled");
        assertDoesNotThrow(() -> audioManager.playCaptureSound(), "playCaptureSound should not throw exception when disabled");
    }
}