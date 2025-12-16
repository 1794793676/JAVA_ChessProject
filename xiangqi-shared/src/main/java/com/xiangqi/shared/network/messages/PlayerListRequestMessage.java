package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for requesting the list of online players.
 */
public class PlayerListRequestMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public PlayerListRequestMessage(String senderId) {
        super(MessageType.PLAYER_LIST_REQUEST, senderId);
    }
}