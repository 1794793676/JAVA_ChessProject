package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.model.Move;
import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for transmitting chess moves between players.
 */
public class MoveMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final Move move;
    private final String gameId;
    
    public MoveMessage(String senderId, Move move, String gameId) {
        super(MessageType.MOVE_REQUEST, senderId);
        this.move = move;
        this.gameId = gameId;
    }
    
    public Move getMove() {
        return move;
    }
    
    public String getGameId() {
        return gameId;
    }
}