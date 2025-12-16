package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.model.Player;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

import java.util.List;

/**
 * Message for lobby updates and player list changes.
 */
public class LobbyUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final List<Player> players;
    private final List<GameSession> games;
    
    public LobbyUpdateMessage(String senderId, List<Player> players, List<GameSession> games) {
        super(MessageType.LOBBY_UPDATE, senderId);
        this.players = players;
        this.games = games;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public List<GameSession> getGames() {
        return games;
    }
}