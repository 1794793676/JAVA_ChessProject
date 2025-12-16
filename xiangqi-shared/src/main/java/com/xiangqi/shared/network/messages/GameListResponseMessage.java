package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

import java.util.List;

/**
 * Message containing the list of games in response to a game list request.
 */
public class GameListResponseMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final List<GameSession> games;
    
    public GameListResponseMessage(String senderId, List<GameSession> games) {
        super(MessageType.GAME_LIST_RESPONSE, senderId);
        this.games = games;
    }
    
    public List<GameSession> getGames() {
        return games;
    }
}