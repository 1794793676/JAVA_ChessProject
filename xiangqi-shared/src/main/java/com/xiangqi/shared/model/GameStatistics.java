package com.xiangqi.shared.model;

import java.io.Serializable;

/**
 * Tracks game statistics for a player.
 */
public class GameStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int gamesDraw;
    
    public GameStatistics() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDraw = 0;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    public int getGamesLost() {
        return gamesLost;
    }
    
    public int getGamesDraw() {
        return gamesDraw;
    }
    
    public void recordWin() {
        gamesPlayed++;
        gamesWon++;
    }
    
    public void recordLoss() {
        gamesPlayed++;
        gamesLost++;
    }
    
    public void recordDraw() {
        gamesPlayed++;
        gamesDraw++;
    }
    
    public double getWinRate() {
        return gamesPlayed > 0 ? (double) gamesWon / gamesPlayed : 0.0;
    }
    
    @Override
    public String toString() {
        return "GameStatistics{" +
                "gamesPlayed=" + gamesPlayed +
                ", gamesWon=" + gamesWon +
                ", gamesLost=" + gamesLost +
                ", gamesDraw=" + gamesDraw +
                ", winRate=" + String.format("%.2f", getWinRate()) +
                '}';
    }
}