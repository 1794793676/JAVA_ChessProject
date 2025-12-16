package com.xiangqi.shared.network.messages;

import com.xiangqi.shared.network.MessageType;
import com.xiangqi.shared.network.NetworkMessage;

/**
 * Message for responding to game invitations.
 */
public class InvitationResponseMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String invitationId;
    private final boolean accepted;
    
    public InvitationResponseMessage(String senderId, String invitationId, boolean accepted) {
        super(MessageType.INVITATION_RESPONSE, senderId);
        this.invitationId = invitationId;
        this.accepted = accepted;
    }
    
    public String getInvitationId() {
        return invitationId;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
}