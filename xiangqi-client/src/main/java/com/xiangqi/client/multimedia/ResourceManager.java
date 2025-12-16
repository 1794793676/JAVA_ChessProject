package com.xiangqi.client.multimedia;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResourceManager handles loading and caching of graphical and audio resources.
 * It provides centralized resource management with preloading and caching mechanisms.
 */
public class ResourceManager {
    private static ResourceManager instance;
    private final Map<String, ImageIcon> imageCache;
    private final Map<String, String> resourcePaths;
    private final AudioManager audioManager;
    
    // Resource path constants
    public static final String QIZI_PATH = "source/qizi/";
    public static final String IMG_PATH = "source/img/";
    public static final String FACE_PATH = "source/face/";
    public static final String AUDIO_PATH = "source/audio/";
    
    // Chess piece image constants (based on typical xiangqi numbering)
    public static final String BOARD_IMAGE = "xqboard.gif";
    public static final String SELECT_IMAGE = "select.gif";
    
    // General game images
    public static final String XIANGQI_LOGO = "xiangqi.gif";
    public static final String START_IMAGE = "start.gif";
    public static final String PLAYER_AVATAR = "boy1.gif";
    public static final String NO_PLAYER = "noone.gif";
    
    private ResourceManager() {
        this.imageCache = new ConcurrentHashMap<>();
        this.resourcePaths = new HashMap<>();
        this.audioManager = AudioManager.getInstance();
        initializeResourcePaths();
        preloadResources();
    }
    
    /**
     * Gets the singleton instance of ResourceManager
     */
    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * Initializes the resource path mappings
     */
    private void initializeResourcePaths() {
        // Chess piece images (qizi directory)
        resourcePaths.put(BOARD_IMAGE, QIZI_PATH + BOARD_IMAGE);
        resourcePaths.put(SELECT_IMAGE, QIZI_PATH + SELECT_IMAGE);
        
        // Add all numbered piece images (1-32 for different pieces and states)
        for (int i = 1; i <= 32; i++) {
            String pieceImage = i + ".gif";
            resourcePaths.put(pieceImage, QIZI_PATH + pieceImage);
        }
        
        // General game images
        resourcePaths.put(XIANGQI_LOGO, IMG_PATH + XIANGQI_LOGO);
        resourcePaths.put(START_IMAGE, IMG_PATH + START_IMAGE);
        resourcePaths.put(PLAYER_AVATAR, IMG_PATH + PLAYER_AVATAR);
        resourcePaths.put(NO_PLAYER, IMG_PATH + NO_PLAYER);
    }
    
    /**
     * Preloads essential resources into cache
     */
    private void preloadResources() {
        // Preload essential game images
        String[] essentialImages = {
            BOARD_IMAGE, SELECT_IMAGE, XIANGQI_LOGO, START_IMAGE, 
            PLAYER_AVATAR, NO_PLAYER
        };
        
        for (String imageName : essentialImages) {
            loadImage(imageName);
        }
        
        // Preload all chess piece images
        for (int i = 1; i <= 32; i++) {
            loadImage(i + ".gif");
        }
    }
    
    /**
     * Loads an image from the resource path
     */
    private ImageIcon loadImage(String imageName) {
        if (imageCache.containsKey(imageName)) {
            return imageCache.get(imageName);
        }
        
        String imagePath = resourcePaths.get(imageName);
        if (imagePath == null) {
            System.err.println("Resource path not found for: " + imageName);
            return null;
        }
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            System.err.println("Image file not found: " + imageFile.getAbsolutePath());
            return null;
        }
        
        try {
            ImageIcon imageIcon = new ImageIcon(imagePath);
            if (imageIcon.getIconWidth() == -1) {
                System.err.println("Failed to load image: " + imagePath);
                return null;
            }
            imageCache.put(imageName, imageIcon);
            return imageIcon;
        } catch (Exception e) {
            System.err.println("Error loading image " + imagePath + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets an image by name, loading it if not cached
     */
    public ImageIcon getImage(String imageName) {
        ImageIcon image = imageCache.get(imageName);
        if (image == null) {
            image = loadImage(imageName);
        }
        return image;
    }
    
    /**
     * Gets a scaled version of an image
     */
    public ImageIcon getScaledImage(String imageName, int width, int height) {
        ImageIcon originalImage = getImage(imageName);
        if (originalImage == null) {
            return null;
        }
        
        String scaledKey = imageName + "_" + width + "x" + height;
        ImageIcon scaledImage = imageCache.get(scaledKey);
        
        if (scaledImage == null) {
            Image scaledImg = originalImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            scaledImage = new ImageIcon(scaledImg);
            imageCache.put(scaledKey, scaledImage);
        }
        
        return scaledImage;
    }
    
    /**
     * Gets chess piece image by piece number
     */
    public ImageIcon getPieceImage(int pieceNumber) {
        if (pieceNumber < 1 || pieceNumber > 32) {
            System.err.println("Invalid piece number: " + pieceNumber);
            return null;
        }
        return getImage(pieceNumber + ".gif");
    }
    
    /**
     * Gets the chess board background image
     */
    public ImageIcon getBoardImage() {
        return getImage(BOARD_IMAGE);
    }
    
    /**
     * Gets the piece selection highlight image
     */
    public ImageIcon getSelectImage() {
        return getImage(SELECT_IMAGE);
    }
    
    /**
     * Gets the game logo image
     */
    public ImageIcon getLogoImage() {
        return getImage(XIANGQI_LOGO);
    }
    
    /**
     * Gets player avatar image
     */
    public ImageIcon getPlayerAvatar() {
        return getImage(PLAYER_AVATAR);
    }
    
    /**
     * Gets no-player placeholder image
     */
    public ImageIcon getNoPlayerImage() {
        return getImage(NO_PLAYER);
    }
    
    /**
     * Gets the audio manager instance
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }
    
    /**
     * Adds a custom resource path
     */
    public void addResourcePath(String resourceName, String path) {
        resourcePaths.put(resourceName, path);
    }
    
    /**
     * Checks if a resource exists
     */
    public boolean resourceExists(String imageName) {
        String path = resourcePaths.get(imageName);
        if (path == null) {
            return false;
        }
        return new File(path).exists();
    }
    
    /**
     * Gets cache statistics
     */
    public String getCacheStats() {
        return String.format("Images cached: %d, Resource paths: %d", 
                           imageCache.size(), resourcePaths.size());
    }
    
    /**
     * Clears the image cache
     */
    public void clearCache() {
        imageCache.clear();
    }
    
    /**
     * Releases all resources
     */
    public void cleanup() {
        clearCache();
        if (audioManager != null) {
            audioManager.cleanup();
        }
    }
}