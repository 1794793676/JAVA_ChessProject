package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an active game session between two players.
 * Contains game metadata, state, and session management information.
 */
public class GameSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String sessionId;
    private final Player redPlayer;
    private final Player blackPlayer;
    private GameState gameState;
    private final long creationTime;
    private long lastActivityTime;
    
    public GameSession(Player redPlayer, Player blackPlayer) {
        this.sessionId = UUID.randomUUID().toString();
        this.redPlayer = Objects.requireNonNull(redPlayer, "Red player cannot be null");
        this.blackPlayer = Objects.requireNonNull(blackPlayer, "Black player cannot be null");
        this.gameState = new GameState(redPlayer, blackPlayer);
        this.creationTime = System.currentTimeMillis();
        this.lastActivityTime = creationTime;
    }
    
    public GameSession(String sessionId, Player redPlayer, Player blackPlayer) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.redPlayer = Objects.requireNonNull(redPlayer, "Red player cannot be null");
        this.blackPlayer = Objects.requireNonNull(blackPlayer, "Black player cannot be null");
        this.gameState = new GameState(redPlayer, blackPlayer);
        this.creationTime = System.currentTimeMillis();
        this.lastActivityTime = creationTime;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public Player getRedPlayer() {
        return redPlayer;
    }
    
    public Player getBlackPlayer() {
        return blackPlayer;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "Game state cannot be null");
        updateLastActivity();
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void updateLastActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }
    
    /**
     * Checks if the given player is part of this game session.
     */
    public boolean hasPlayer(Player player) {
        return redPlayer.equals(player) || blackPlayer.equals(player);
    }
    
    /**
     * Gets the opponent of the given player in this session.
     */
    public Player getOpponent(Player player) {
        if (redPlayer.equals(player)) {
            return blackPlayer;
        } else if (blackPlayer.equals(player)) {
            return redPlayer;
        } else {
            throw new IllegalArgumentException("Player is not part of this game session");
        }
    }
    
    /**
     * Checks if the game session is active (not ended).
     */
    public boolean isActive() {
        GameStatus status = gameState.getStatus();
        return status == GameStatus.IN_PROGRESS || 
               status == GameStatus.CHECK || 
               status == GameStatus.WAITING_FOR_PLAYERS;
    }
    
    /**
     * Checks if the game session has ended.
     */
    public boolean isEnded() {
        return !isActive();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameSession that = (GameSession) obj;
        return Objects.equals(sessionId, that.sessionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
    
    @Override
    public String toString() {
        return "GameSession{" +
                "sessionId='" + sessionId + '\'' +
                ", redPlayer=" + redPlayer.getUsername() +
                ", blackPlayer=" + blackPlayer.getUsername() +
                ", status=" + gameState.getStatus() +
                ", currentPlayer=" + (gameState.getCurrentPlayer() != null ? 
                    gameState.getCurrentPlayer().getUsername() : "null") +
                '}';
    }
}