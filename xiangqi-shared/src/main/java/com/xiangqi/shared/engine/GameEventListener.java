package com.xiangqi.shared.engine;

import com.xiangqi.shared.model.*;

/**
 * Interface for listening to game events.
 * Implementations can respond to various game state changes and player actions.
 */
public interface GameEventListener {
    
    /**
     * Called when a move has been executed successfully.
     */
    void onMoveExecuted(Move move);
    
    /**
     * Called when the game state has changed.
     */
    void onGameStateChanged(GameState newState);
    
    /**
     * Called when a player joins the game.
     */
    void onPlayerJoined(Player player);
    
    /**
     * Called when a player leaves the game.
     */
    void onPlayerLeft(Player player);
    
    /**
     * Called when the game ends with a specific result.
     */
    void onGameEnded(GameResult result);
    
    /**
     * Called when a player's turn begins.
     */
    default void onTurnStarted(Player player) {
        // Default empty implementation
    }
    
    /**
     * Called when an invalid move is attempted.
     */
    default void onInvalidMoveAttempted(Move move, String reason) {
        // Default empty implementation
    }
    
    /**
     * Called when the game state becomes corrupted and cannot be recovered.
     */
    default void onGameStateCorrupted(String reason) {
        // Default empty implementation
    }
}