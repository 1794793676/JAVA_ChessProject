package com.xiangqi.shared.network;

import com.xiangqi.shared.network.messages.*;

/**
 * Interface for handling different types of network messages.
 * Implementations define how to process each message type.
 */
public interface NetworkMessageHandler {
    
    /**
     * Handles login request messages.
     */
    void handleLoginRequest(LoginMessage message);
    
    /**
     * Handles move messages during gameplay.
     */
    void handleMoveMessage(MoveMessage message);
    
    /**
     * Handles chat messages between players.
     */
    void handleChatMessage(ChatMessage message);
    
    /**
     * Handles client disconnection events.
     */
    void handleDisconnection(String clientId);
    
    /**
     * Handles game invitation messages.
     */
    default void handleGameInvitation(GameInvitationMessage message) {
        // Default empty implementation
    }
    
    /**
     * Handles lobby update requests.
     */
    default void handleLobbyUpdate(LobbyUpdateMessage message) {
        // Default empty implementation
    }
    
    /**
     * Handles error messages.
     */
    default void handleError(ErrorMessage message) {
        // Default empty implementation
    }
}