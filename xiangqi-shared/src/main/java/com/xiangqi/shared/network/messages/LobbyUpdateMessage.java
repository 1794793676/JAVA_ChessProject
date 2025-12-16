package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for lobby updates and player list changes.
 */
public class LobbyUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public LobbyUpdateMessage(String senderId) {
        super(MessageType.LOBBY_UPDATE, senderId);
    }
}