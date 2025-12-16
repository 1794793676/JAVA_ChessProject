package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for maintaining connection heartbeat.
 */
public class HeartbeatMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public HeartbeatMessage(String senderId) {
        super(MessageType.HEARTBEAT, senderId);
    }
}