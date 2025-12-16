package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for notifying about client disconnection.
 */
public class DisconnectMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String reason;
    
    public DisconnectMessage(String senderId, String reason) {
        super(MessageType.DISCONNECT, senderId);
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}