package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameResult;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for notifying players that a game has ended.
 */
public class GameEndMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String gameId;
    private final GameResult gameResult;
    
    public GameEndMessage(String gameId, GameResult gameResult) {
        super(MessageType.GAME_END, null); // Server message
        this.gameId = gameId;
        this.gameResult = gameResult;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public GameResult getGameResult() {
        return gameResult;
    }
}