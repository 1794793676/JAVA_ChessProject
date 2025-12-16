package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for user login requests.
 */
public class LoginMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String username;
    private final String password;
    
    public LoginMessage(String username, String password) {
        super(MessageType.LOGIN_REQUEST, null); // No sender ID for login requests
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
}