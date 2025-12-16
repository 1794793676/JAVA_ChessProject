package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for game invitations between players.
 */
public class GameInvitationMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String targetPlayerId;
    private final String invitationId;
    
    public GameInvitationMessage(String senderId, String targetPlayerId, String invitationId) {
        super(MessageType.GAME_INVITATION, senderId);
        this.targetPlayerId = targetPlayerId;
        this.invitationId = invitationId;
    }
    
    public String getTargetPlayerId() {
        return targetPlayerId;
    }
    
    public String getInvitationId() {
        return invitationId;
    }
}