package com.xiangqi.shared.model;

/**
 * Enumeration representing the various states a game can be in.
 */
public enum GameStatus {
    WAITING_FOR_PLAYERS,
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW,
    RESIGNED,
    TIMEOUT,
    ABANDONED
}