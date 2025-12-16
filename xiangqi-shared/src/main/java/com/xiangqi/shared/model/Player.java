package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a player in the Xiangqi game system.
 * Contains player identification, status, and game statistics.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String playerId;
    private final String username;
    private PlayerStatus status;
    private int rating;
    private GameStatistics statistics;
    
    public Player(String playerId, String username) {
        this.playerId = Objects.requireNonNull(playerId, "Player ID cannot be null");
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.status = PlayerStatus.OFFLINE;
        this.rating = 1000; // Default rating
        this.statistics = new GameStatistics();
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public PlayerStatus getStatus() {
        return status;
    }
    
    public void setStatus(PlayerStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public GameStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(GameStatistics statistics) {
        this.statistics = Objects.requireNonNull(statistics, "Statistics cannot be null");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return Objects.equals(playerId, player.playerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
    
    @Override
    public String toString() {
        return "Player{" +
                "playerId='" + playerId + '\'' +
                ", username='" + username + '\'' +
                ", status=" + status +
                ", rating=" + rating +
                '}';
    }
}