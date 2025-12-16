package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.Player;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

import java.util.List;

/**
 * Message containing the list of players in response to a player list request.
 */
public class PlayerListResponseMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final List<Player> players;
    
    public PlayerListResponseMessage(String senderId, List<Player> players) {
        super(MessageType.PLAYER_LIST_RESPONSE, senderId);
        this.players = players;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
}