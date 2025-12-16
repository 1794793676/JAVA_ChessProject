package com.xiangqi.shared.network;

/**
 * Enumeration of all network message types in the Xiangqi system.
 */
public enum MessageType {
    // Authentication messages
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    LOGOUT_REQUEST,
    
    // Lobby messages
    LOBBY_UPDATE,
    PLAYER_LIST_REQUEST,
    PLAYER_LIST_RESPONSE,
    GAME_LIST_REQUEST,
    GAME_LIST_RESPONSE,
    
    // Game invitation messages
    GAME_INVITATION,
    INVITATION_RESPONSE,
    
    // Game play messages
    MOVE_REQUEST,
    MOVE_RESPONSE,
    GAME_STATE_UPDATE,
    GAME_START,
    GAME_END,
    
    // Chat messages
    CHAT_MESSAGE,
    
    // System messages
    ERROR_MESSAGE,
    HEARTBEAT,
    DISCONNECT
}