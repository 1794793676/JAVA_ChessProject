package com.xiangqi.shared.network;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for all network messages in the Xiangqi system.
 * Provides common properties and behavior for message transmission.
 */
public abstract class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected final MessageType type;
    protected final String senderId;
    protected final long timestamp;
    
    protected NetworkMessage(MessageType type, String senderId) {
        this.type = Objects.requireNonNull(type, "Message type cannot be null");
        this.senderId = senderId; // Can be null for system messages
        this.timestamp = System.currentTimeMillis();
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NetworkMessage that = (NetworkMessage) obj;
        return timestamp == that.timestamp &&
               type == that.type &&
               Objects.equals(senderId, that.senderId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, senderId, timestamp);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "type=" + type +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}