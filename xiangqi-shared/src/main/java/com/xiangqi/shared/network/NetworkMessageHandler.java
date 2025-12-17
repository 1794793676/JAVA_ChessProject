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
     * Handles login response messages.
     */
    default void handleLoginResponse(LoginResponseMessage message) {
        // Default empty implementation
    }
    
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
     * Handles game start messages.
     */
    default void handleGameStart(GameStartMessage message) {
        // Default empty implementation
    }
    
    /**
     * Handles lobby update requests.
     */
    default void handleLobbyUpdate(LobbyUpdateMessage message) {
        // Default empty implementation
    }
    
    /**
     * Handles player list response messages.
     */
    default void handlePlayerListResponse(PlayerListResponseMessage message) {
        // Default empty implementation
    }
    
    /**
     * Handles game list response messages.
     */
    default void handleGameListResponse(GameListResponseMessage message) {
        // Default empty implementation
    }
    
    /**
     * Handles error messages.
     */
    default void handleError(ErrorMessage message) {
        // Default empty implementation
    }
}