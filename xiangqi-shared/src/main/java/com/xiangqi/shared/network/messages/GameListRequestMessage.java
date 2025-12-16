package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for requesting the list of active games.
 */
public class GameListRequestMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public GameListRequestMessage(String senderId) {
        super(MessageType.GAME_LIST_REQUEST, senderId);
    }
}