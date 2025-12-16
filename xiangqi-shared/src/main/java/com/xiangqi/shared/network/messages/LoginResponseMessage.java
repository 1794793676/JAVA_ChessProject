package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.Player;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for login response from server.
 */
public class LoginResponseMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final boolean success;
    private final Player player;
    private final String errorMessage;
    
    public LoginResponseMessage(boolean success, Player player, String errorMessage) {
        super(MessageType.LOGIN_RESPONSE, null); // Server message
        this.success = success;
        this.player = player;
        this.errorMessage = errorMessage;
    }
    
    public static LoginResponseMessage success(Player player) {
        return new LoginResponseMessage(true, player, null);
    }
    
    public static LoginResponseMessage failure(String errorMessage) {
        return new LoginResponseMessage(false, null, errorMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}