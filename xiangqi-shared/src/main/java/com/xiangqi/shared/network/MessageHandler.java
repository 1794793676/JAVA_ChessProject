package com.xiangqi.shared.network;

import com.xiangqi.shared.engine.ChessEngine;
import com.xiangqi.shared.model.*;
import com.xiangqi.shared.network.messages.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized message handler that processes various network messages.
 * Implements message validation, routing logic, and error handling.
 * 实现需求 5.2, 5.4
 */
public class MessageHandler implements NetworkMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());
    
    // Message validation and processing components
    private final ChessEngine chessEngine;
    private final Map<String, Player> authenticatedPlayers;
    private final Map<String, GameSession> activeSessions;
    private final Map<String, String> clientToPlayer;
    
    // Message routing callbacks
    private MessageRoutingCallback routingCallback;
    
    /**
     * Interface for message routing callbacks.
     */
    public interface MessageRoutingCallback {
        void sendToClient(String clientId, NetworkMessage message);
        void broadcastToGame(String gameId, NetworkMessage message);
        void broadcastToAll(NetworkMessage message);
        String getClientIdForPlayer(String playerId);
        void removeClient(String clientId);
    }
    
    /**
     * Constructs a new MessageHandler with the specified chess engine.
     */
    public MessageHandler(ChessEngine chessEngine) {
        this.chessEngine = chessEngine;
        this.authenticatedPlayers = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        this.clientToPlayer = new ConcurrentHashMap<>();
    }
    
    /**
     * Sets the message routing callback for sending responses.
     */
    public void setRoutingCallback(MessageRoutingCallback callback) {
        this.routingCallback = callback;
    }
    
    @Override
    public void handleLoginRequest(LoginMessage message) {
        LOGGER.info("Processing login request for user: " + message.getUsername());
        
        try {
            // Validate message
            ValidationResult validation = validateLoginMessage(message);
            if (!validation.isValid()) {
                sendErrorResponse(null, "LOGIN_VALIDATION_FAILED", validation.getErrorMessage());
                return;
            }
            
            // Authenticate user
            AuthenticationResult authResult = authenticateUser(message.getUsername(), message.getPassword());
            
            if (authResult.isSuccess()) {
                Player player = authResult.getPlayer();
                authenticatedPlayers.put(player.getPlayerId(), player);
                
                // Send success response
                LoginResponseMessage response = LoginResponseMessage.success(player);
                sendResponse(null, response); // Client ID would be determined by routing callback
                
                LOGGER.info("Login successful for user: " + message.getUsername());
            } else {
                sendErrorResponse(null, "AUTHENTICATION_FAILED", authResult.getErrorMessage());
                LOGGER.warning("Login failed for user: " + message.getUsername() + " - " + authResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing login request", e);
            sendErrorResponse(null, "INTERNAL_ERROR", "Internal server error during login");
        }
    }
    
    @Override
    public void handleMoveMessage(MoveMessage message) {
        LOGGER.fine("Processing move message for game: " + message.getGameId());
        
        try {
            // Validate message
            ValidationResult validation = validateMoveMessage(message);
            if (!validation.isValid()) {
                sendErrorResponse(message.getSenderId(), "MOVE_VALIDATION_FAILED", validation.getErrorMessage());
                return;
            }
            
            // Get game session
            GameSession session = activeSessions.get(message.getGameId());
            if (session == null) {
                sendErrorResponse(message.getSenderId(), "GAME_NOT_FOUND", "Game session not found");
                return;
            }
            
            // Validate player's turn
            Player currentPlayer = session.getGameState().getCurrentPlayer();
            if (currentPlayer == null || !currentPlayer.getPlayerId().equals(message.getSenderId())) {
                sendErrorResponse(message.getSenderId(), "NOT_YOUR_TURN", "It's not your turn");
                return;
            }
            
            // Process the move using chess engine
            MoveResult moveResult = processMoveWithEngine(session, message.getMove());
            
            if (moveResult.isValid()) {
                // Update game state
                session.setGameState(moveResult.getNewGameState());
                
                // Create and send move response
                MoveResponseMessage response = MoveResponseMessage.success(
                    message.getGameId(), 
                    message.getMove()
                );
                
                // Broadcast to both players in the game
                if (routingCallback != null) {
                    routingCallback.broadcastToGame(message.getGameId(), response);
                }
                
                // Check for game end conditions
                if (moveResult.isGameEnded()) {
                    handleGameEnd(session, moveResult.getGameResult());
                }
                
                LOGGER.fine("Move processed successfully for game: " + message.getGameId());
            } else {
                sendErrorResponse(message.getSenderId(), "INVALID_MOVE", moveResult.getErrorMessage());
                LOGGER.warning("Invalid move attempted in game: " + message.getGameId() + " - " + moveResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing move message", e);
            sendErrorResponse(message.getSenderId(), "INTERNAL_ERROR", "Internal server error during move processing");
        }
    }
    
    @Override
    public void handleChatMessage(ChatMessage message) {
        LOGGER.fine("Processing chat message from: " + message.getSenderId());
        
        try {
            // Validate message
            ValidationResult validation = validateChatMessage(message);
            if (!validation.isValid()) {
                sendErrorResponse(message.getSenderId(), "CHAT_VALIDATION_FAILED", validation.getErrorMessage());
                return;
            }
            
            // Route message based on type
            if (message.isBroadcast()) {
                // Broadcast to all clients
                if (routingCallback != null) {
                    routingCallback.broadcastToAll(message);
                }
            } else if (message.getTargetId() != null && message.getTargetId().startsWith("game-")) {
                // Send to game participants (assuming target ID format includes game ID)
                if (routingCallback != null) {
                    routingCallback.broadcastToGame(message.getTargetId(), message);
                }
            } else if (message.getTargetId() != null) {
                // Send to specific target
                if (routingCallback != null) {
                    String targetClientId = routingCallback.getClientIdForPlayer(message.getTargetId());
                    if (targetClientId != null) {
                        routingCallback.sendToClient(targetClientId, message);
                    }
                }
            }
            
            LOGGER.fine("Chat message routed successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing chat message", e);
            sendErrorResponse(message.getSenderId(), "INTERNAL_ERROR", "Internal server error during chat processing");
        }
    }
    
    @Override
    public void handleDisconnection(String clientId) {
        LOGGER.info("Processing disconnection for client: " + clientId);
        
        try {
            // Find associated player
            String playerId = clientToPlayer.remove(clientId);
            if (playerId != null) {
                Player player = authenticatedPlayers.remove(playerId);
                if (player != null) {
                    // Handle player disconnection in active games
                    handlePlayerDisconnection(player);
                    LOGGER.info("Player disconnected: " + player.getUsername());
                }
            }
            
            // Remove client from routing
            if (routingCallback != null) {
                routingCallback.removeClient(clientId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing disconnection", e);
        }
    }
    
    @Override
    public void handleGameInvitation(GameInvitationMessage message) {
        LOGGER.info("Processing game invitation from: " + message.getSenderId() + " to: " + message.getTargetPlayerId());
        
        try {
            // Validate message
            ValidationResult validation = validateGameInvitationMessage(message);
            if (!validation.isValid()) {
                sendErrorResponse(message.getSenderId(), "INVITATION_VALIDATION_FAILED", validation.getErrorMessage());
                return;
            }
            
            // Check if target player exists and is available
            Player targetPlayer = authenticatedPlayers.get(message.getTargetPlayerId());
            if (targetPlayer == null) {
                sendErrorResponse(message.getSenderId(), "PLAYER_NOT_FOUND", "Target player not found");
                return;
            }
            
            if (targetPlayer.getStatus() != PlayerStatus.ONLINE) {
                sendErrorResponse(message.getSenderId(), "PLAYER_UNAVAILABLE", "Target player is not available");
                return;
            }
            
            // Forward invitation to target player
            if (routingCallback != null) {
                String targetClientId = routingCallback.getClientIdForPlayer(message.getTargetPlayerId());
                if (targetClientId != null) {
                    routingCallback.sendToClient(targetClientId, message);
                    LOGGER.info("Game invitation forwarded to target player");
                } else {
                    sendErrorResponse(message.getSenderId(), "PLAYER_OFFLINE", "Target player is offline");
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing game invitation", e);
            sendErrorResponse(message.getSenderId(), "INTERNAL_ERROR", "Internal server error during invitation processing");
        }
    }
    
    @Override
    public void handleLobbyUpdate(LobbyUpdateMessage message) {
        // This is typically sent by the server, not processed by it
        LOGGER.fine("Lobby update message received (typically server-generated)");
    }
    
    @Override
    public void handleError(ErrorMessage message) {
        LOGGER.warning("Error message received: " + message.getErrorCode() + " - " + message.getErrorDescription());
        // Error messages are typically handled by clients, not processed by server
    }
    
    /**
     * Handles invitation response messages.
     */
    public void handleInvitationResponse(InvitationResponseMessage message) {
        LOGGER.info("Processing invitation response: " + (message.isAccepted() ? "accepted" : "declined"));
        
        try {
            if (message.isAccepted()) {
                // Create new game session
                createGameSession(message.getInvitationId(), message.getSenderId());
            } else {
                // Notify inviter of declined invitation
                notifyInvitationDeclined(message.getInvitationId());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing invitation response", e);
        }
    }
    
    // Validation methods
    
    private ValidationResult validateLoginMessage(LoginMessage message) {
        if (message.getUsername() == null || message.getUsername().trim().isEmpty()) {
            return ValidationResult.invalid("Username cannot be empty");
        }
        
        if (message.getPassword() == null || message.getPassword().trim().isEmpty()) {
            return ValidationResult.invalid("Password cannot be empty");
        }
        
        if (message.getUsername().length() > 50) {
            return ValidationResult.invalid("Username too long");
        }
        
        return ValidationResult.valid();
    }
    
    private ValidationResult validateMoveMessage(MoveMessage message) {
        if (message.getGameId() == null || message.getGameId().trim().isEmpty()) {
            return ValidationResult.invalid("Game ID cannot be empty");
        }
        
        if (message.getMove() == null) {
            return ValidationResult.invalid("Move cannot be null");
        }
        
        if (message.getMove().getFrom() == null || message.getMove().getTo() == null) {
            return ValidationResult.invalid("Move positions cannot be null");
        }
        
        return ValidationResult.valid();
    }
    
    private ValidationResult validateChatMessage(ChatMessage message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return ValidationResult.invalid("Chat message cannot be empty");
        }
        
        if (message.getContent().length() > 500) {
            return ValidationResult.invalid("Chat message too long");
        }
        
        if (message.getSenderId() == null || message.getSenderId().trim().isEmpty()) {
            return ValidationResult.invalid("Sender ID cannot be empty");
        }
        
        return ValidationResult.valid();
    }
    
    private ValidationResult validateGameInvitationMessage(GameInvitationMessage message) {
        if (message.getSenderId() == null || message.getTargetPlayerId() == null) {
            return ValidationResult.invalid("Sender and target player IDs cannot be null");
        }
        
        if (message.getSenderId().equals(message.getTargetPlayerId())) {
            return ValidationResult.invalid("Cannot invite yourself to a game");
        }
        
        return ValidationResult.valid();
    }
    
    // Helper methods
    
    private AuthenticationResult authenticateUser(String username, String password) {
        // Simple authentication for demo purposes
        // In a real implementation, this would check against a database
        if (username != null && !username.trim().isEmpty() && 
            password != null && !password.trim().isEmpty()) {
            
            Player player = new Player(username, username);
            return AuthenticationResult.success(player);
        } else {
            return AuthenticationResult.failure("Invalid credentials");
        }
    }
    
    private MoveResult processMoveWithEngine(GameSession session, Move move) {
        try {
            // Set the current state in the chess engine
            chessEngine.setCurrentState(session.getGameState());
            
            // Use chess engine to validate and process the move
            if (chessEngine.executeMove(move)) {
                GameState newState = chessEngine.getCurrentState();
                
                // Check for game end conditions
                boolean gameEnded = false;
                GameResult gameResult = null;
                
                if (chessEngine.isCheckmate(newState.getCurrentPlayer())) {
                    gameEnded = true;
                    Player winner = session.getGameState().getOpponent(newState.getCurrentPlayer());
                    gameResult = GameResult.checkmate(winner, newState.getCurrentPlayer());
                } else if (chessEngine.isStalemate(newState.getCurrentPlayer())) {
                    gameEnded = true;
                    gameResult = GameResult.draw(session.getRedPlayer(), session.getBlackPlayer(), "Stalemate");
                }
                
                return MoveResult.valid(newState, gameEnded, gameResult);
            } else {
                return MoveResult.invalid("Move violates chess rules");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing move with engine", e);
            return MoveResult.invalid("Error validating move");
        }
    }
    
    private void handleGameEnd(GameSession session, GameResult result) {
        // Send game end message to both players
        GameEndMessage endMessage = new GameEndMessage(session.getSessionId(), result);
        
        if (routingCallback != null) {
            routingCallback.broadcastToGame(session.getSessionId(), endMessage);
        }
        
        // Remove session from active sessions
        activeSessions.remove(session.getSessionId());
        
        LOGGER.info("Game ended: " + session.getSessionId() + " - " + result.getEndStatus());
    }
    
    private void handlePlayerDisconnection(Player player) {
        // Find and handle any active games involving this player
        for (GameSession session : activeSessions.values()) {
            if (session.getRedPlayer().equals(player) || session.getBlackPlayer().equals(player)) {
                // End the game due to disconnection
                Player opponent = session.getOpponent(player);
                GameResult result = new GameResult(opponent, player, GameStatus.ABANDONED, "Player disconnected");
                handleGameEnd(session, result);
            }
        }
    }
    
    private void createGameSession(String invitationId, String acceptingPlayerId) {
        // Implementation would create a new game session
        // This is a simplified version
        LOGGER.info("Creating game session for invitation: " + invitationId);
    }
    
    private void notifyInvitationDeclined(String invitationId) {
        // Implementation would notify the inviter
        LOGGER.info("Invitation declined: " + invitationId);
    }
    
    private void sendResponse(String clientId, NetworkMessage message) {
        if (routingCallback != null) {
            routingCallback.sendToClient(clientId, message);
        }
    }
    
    private void sendErrorResponse(String clientId, String errorCode, String errorMessage) {
        ErrorMessage error = new ErrorMessage(null, errorCode, errorMessage);
        sendResponse(clientId, error);
    }
    
    // Helper classes
    
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    private static class AuthenticationResult {
        private final boolean success;
        private final Player player;
        private final String errorMessage;
        
        private AuthenticationResult(boolean success, Player player, String errorMessage) {
            this.success = success;
            this.player = player;
            this.errorMessage = errorMessage;
        }
        
        public static AuthenticationResult success(Player player) {
            return new AuthenticationResult(true, player, null);
        }
        
        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    private static class MoveResult {
        private final boolean valid;
        private final GameState newGameState;
        private final boolean gameEnded;
        private final GameResult gameResult;
        private final String errorMessage;
        
        private MoveResult(boolean valid, GameState newGameState, boolean gameEnded, 
                          GameResult gameResult, String errorMessage) {
            this.valid = valid;
            this.newGameState = newGameState;
            this.gameEnded = gameEnded;
            this.gameResult = gameResult;
            this.errorMessage = errorMessage;
        }
        
        public static MoveResult valid(GameState newGameState, boolean gameEnded, GameResult gameResult) {
            return new MoveResult(true, newGameState, gameEnded, gameResult, null);
        }
        
        public static MoveResult invalid(String errorMessage) {
            return new MoveResult(false, null, false, null, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public GameState getNewGameState() {
            return newGameState;
        }
        
        public boolean isGameEnded() {
            return gameEnded;
        }
        
        public GameResult getGameResult() {
            return gameResult;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}