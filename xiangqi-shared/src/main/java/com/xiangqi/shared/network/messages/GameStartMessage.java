package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message sent when a game starts.
 */
public class GameStartMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String gameId;
    private final GameSession gameSession;
    
    public GameStartMessage(String senderId, String gameId, GameSession gameSession) {
        super(MessageType.GAME_START, senderId);
        this.gameId = gameId;
        this.gameSession = gameSession;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public GameSession getGameSession() {
        return gameSession;
    }
}