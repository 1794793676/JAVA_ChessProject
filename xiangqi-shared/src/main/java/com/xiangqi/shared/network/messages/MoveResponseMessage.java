package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.Move;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for responding to move requests.
 */
public class MoveResponseMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final Move move;
    private final String gameId;
    private final boolean success;
    private final String errorMessage;
    
    public MoveResponseMessage(String gameId, Move move, boolean success, String errorMessage) {
        super(MessageType.MOVE_RESPONSE, null); // Server message
        this.gameId = gameId;
        this.move = move;
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    public static MoveResponseMessage success(String gameId, Move move) {
        return new MoveResponseMessage(gameId, move, true, null);
    }
    
    public static MoveResponseMessage failure(String gameId, String errorMessage) {
        return new MoveResponseMessage(gameId, null, false, errorMessage);
    }
    
    public Move getMove() {
        return move;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}