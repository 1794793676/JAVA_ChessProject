package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for notifying players that a game has started.
 */
public class GameStartMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final GameSession gameSession;
    
    public GameStartMessage(GameSession gameSession) {
        super(MessageType.GAME_START, null); // Server message
        this.gameSession = gameSession;
    }
    
    public GameSession getGameSession() {
        return gameSession;
    }
}