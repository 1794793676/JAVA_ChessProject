package com.xiangqi.shared.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the result of a completed game.
 */
public class GameResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Player winner;
    private final Player loser;
    private final GameStatus endStatus;
    private final String reason;
    private final long gameEndTime;
    
    public GameResult(Player winner, Player loser, GameStatus endStatus, String reason) {
        this.winner = winner; // Can be null for draws
        this.loser = loser;   // Can be null for draws
        this.endStatus = Objects.requireNonNull(endStatus, "End status cannot be null");
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.gameEndTime = System.currentTimeMillis();
    }
    
    public static GameResult draw(Player player1, Player player2, String reason) {
        return new GameResult(null, null, GameStatus.DRAW, reason);
    }
    
    public static GameResult checkmate(Player winner, Player loser) {
        return new GameResult(winner, loser, GameStatus.CHECKMATE, "Checkmate");
    }
    
    public static GameResult resignation(Player winner, Player loser) {
        return new GameResult(winner, loser, GameStatus.RESIGNED, "Resignation");
    }
    
    public static GameResult timeout(Player winner, Player loser) {
        return new GameResult(winner, loser, GameStatus.TIMEOUT, "Time limit exceeded");
    }
    
    public Player getWinner() {
        return winner;
    }
    
    public Player getLoser() {
        return loser;
    }
    
    public GameStatus getEndStatus() {
        return endStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getGameEndTime() {
        return gameEndTime;
    }
    
    public boolean isDraw() {
        return winner == null && loser == null;
    }
    
    @Override
    public String toString() {
        if (isDraw()) {
            return "GameResult{DRAW, reason='" + reason + "'}";
        } else {
            return "GameResult{" +
                    "winner=" + (winner != null ? winner.getUsername() : "null") +
                    ", loser=" + (loser != null ? loser.getUsername() : "null") +
                    ", endStatus=" + endStatus +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }
}