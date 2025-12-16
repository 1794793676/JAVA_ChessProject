package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for user logout requests.
 */
public class LogoutMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public LogoutMessage(String senderId) {
        super(MessageType.LOGOUT_REQUEST, senderId);
    }
}