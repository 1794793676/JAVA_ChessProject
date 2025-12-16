package com.xiangqi.client.multimedia;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AudioManager handles loading and playing sound effects for the Xiangqi game.
 * It manages audio resources and provides methods to play sounds for different game events.
 */
public class AudioManager {
    private static AudioManager instance;
    private final Map<String, Clip> audioClips;
    private final String audioBasePath;
    private boolean soundEnabled;
    
    // Audio file constants
    public static final String SOUND_EAT = "eat.wav";      // Piece capture sound
    public static final String SOUND_GO = "go.wav";        // Move piece sound
    public static final String SOUND_JIANG = "jiang.wav";  // Check/General in danger sound
    public static final String SOUND_SEAT = "seat.wav";    // Player join/seat sound
    public static final String SOUND_SELECT = "select.wav"; // Piece selection sound
    
    private AudioManager() {
        this.audioClips = new ConcurrentHashMap<>();
        this.audioBasePath = "source/audio/";
        this.soundEnabled = true;
        loadAudioFiles();
    }
    
    /**
     * Gets the singleton instance of AudioManager
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Loads all audio files from the audio directory
     */
    private void loadAudioFiles() {
        String[] audioFiles = {SOUND_EAT, SOUND_GO, SOUND_JIANG, SOUND_SEAT, SOUND_SELECT};
        
        for (String fileName : audioFiles) {
            try {
                loadAudioFile(fileName);
            } catch (Exception e) {
                System.err.println("Failed to load audio file: " + fileName + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Loads a specific audio file into memory
     */
    private void loadAudioFile(String fileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(audioBasePath + fileName);
        if (!audioFile.exists()) {
            System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
            return;
        }
        
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        audioClips.put(fileName, clip);
        audioStream.close();
    }
    
    /**
     * Plays a sound effect by filename
     */
    public void playSound(String soundName) {
        if (!soundEnabled) {
            return;
        }
        
        Clip clip = audioClips.get(soundName);
        if (clip != null) {
            // Stop the clip if it's already playing
            if (clip.isRunning()) {
                clip.stop();
            }
            // Reset to beginning and play
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.err.println("Sound not found: " + soundName);
        }
    }
    
    /**
     * Plays sound when a piece is moved
     */
    public void playMoveSound() {
        playSound(SOUND_GO);
    }
    
    /**
     * Plays sound when a piece is captured
     */
    public void playCaptureSound() {
        playSound(SOUND_EAT);
    }
    
    /**
     * Plays sound when a piece is selected
     */
    public void playSelectSound() {
        playSound(SOUND_SELECT);
    }
    
    /**
     * Plays sound when the general is in check
     */
    public void playCheckSound() {
        playSound(SOUND_JIANG);
    }
    
    /**
     * Plays sound when a player joins the game
     */
    public void playPlayerJoinSound() {
        playSound(SOUND_SEAT);
    }
    
    /**
     * Enables or disables sound effects
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    /**
     * Returns whether sound is currently enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * Stops all currently playing sounds
     */
    public void stopAllSounds() {
        for (Clip clip : audioClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }
    
    /**
     * Releases all audio resources
     */
    public void cleanup() {
        stopAllSounds();
        for (Clip clip : audioClips.values()) {
            clip.close();
        }
        audioClips.clear();
    }
}