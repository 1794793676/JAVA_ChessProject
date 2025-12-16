package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

import java.util.List;

/**
 * Message containing the list of active games.
 */
public class GameListResponseMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final List<GameSession> games;
    
    public GameListResponseMessage(List<GameSession> games) {
        super(MessageType.GAME_LIST_RESPONSE, null); // Server message
        this.games = games;
    }
    
    public List<GameSession> getGames() {
        return games;
    }
}