package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for chat communication between players.
 */
public class ChatMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String content;
    private final String targetId; // null for broadcast messages
    
    public ChatMessage(String senderId, String content, String targetId) {
        super(MessageType.CHAT_MESSAGE, senderId);
        this.content = content;
        this.targetId = targetId;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public boolean isBroadcast() {
        return targetId == null;
    }
}