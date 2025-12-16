package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for error notifications.
 */
public class ErrorMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final String errorDescription;
    
    public ErrorMessage(String senderId, String errorCode, String errorDescription) {
        super(MessageType.ERROR_MESSAGE, senderId);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorDescription() {
        return errorDescription;
    }
}