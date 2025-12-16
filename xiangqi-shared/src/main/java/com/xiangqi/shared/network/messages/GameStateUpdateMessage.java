package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameState;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for broadcasting game state updates to players.
 */
public class GameStateUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String gameId;
    private final GameState gameState;
    
    public GameStateUpdateMessage(String gameId, GameState gameState) {
        super(MessageType.GAME_STATE_UPDATE, null); // Server message
        this.gameId = gameId;
        this.gameState = gameState;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public GameState getGameState() {
        return gameState;
    }
}