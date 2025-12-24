package com.xiangqi.server.network;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.engine.ChessEngine;
import com.xiangqi.shared.engine.GameEventListener;
import com.xiangqi.shared.network.NetworkMessage;
import com.xiangqi.shared.network.NetworkMessageHandler;
import com.xiangqi.shared.network.messages.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game server that manages multiple client connections and game sessions.
 * Handles message routing, game session management, and client coordination.
 */
public class GameServer implements NetworkMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
    private static final int DEFAULT_PORT = 8888;
    private static final int THREAD_POOL_SIZE = 50;
    
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger clientIdCounter = new AtomicInteger(0);
    
    // Thread management
    private ExecutorService clientThreadPool;
    private Thread acceptorThread;
    private Thread maintenanceThread;
    
    // Client and game management
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();
    private final Map<String, ChessEngine> gameEngines = new ConcurrentHashMap<>();
    private final Map<String, String> clientToPlayer = new ConcurrentHashMap<>();
    private final Map<String, GameInvitationMessage> pendingInvitations = new ConcurrentHashMap<>();
    
    /**
     * Starts the game server on the default port.
     */
    public void startServer() {
        startServer(DEFAULT_PORT);
    }
    
    /**
     * Starts the game server on the specified port.
     */
    public void startServer(int port) {
        if (running.get()) {
            LOGGER.warning("Server is already running");
            return;
        }
        
        try {
            serverSocket = new ServerSocket(port);
            clientThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            running.set(true);
            
            // Start acceptor thread
            acceptorThread = new Thread(this::acceptConnections, "GameServer-Acceptor");
            acceptorThread.start();
            
            // Start maintenance thread
            maintenanceThread = new Thread(this::performMaintenance, "GameServer-Maintenance");
            maintenanceThread.setDaemon(true);
            maintenanceThread.start();
            
            LOGGER.info("Game server started on port " + port);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start server on port " + port, e);
            running.set(false);
        }
    }
    
    /**
     * Stops the game server gracefully.
     */
    public void stopServer() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        
        // Stop accepting new connections
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }
        
        // Stop all client handlers
        for (ClientHandler client : clients.values()) {
            client.stop();
        }
        clients.clear();
        
        // Shutdown thread pool
        if (clientThreadPool != null) {
            clientThreadPool.shutdown();
        }
        
        // Interrupt threads
        if (acceptorThread != null) {
            acceptorThread.interrupt();
        }
        if (maintenanceThread != null) {
            maintenanceThread.interrupt();
        }
        
        // Clear data structures
        players.clear();
        gameSessions.clear();
        clientToPlayer.clear();
        pendingInvitations.clear();
        
        LOGGER.info("Game server stopped");
    }
    
    /**
     * Accepts incoming client connections.
     */
    public void acceptConnections() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientId = "client-" + clientIdCounter.incrementAndGet();
                
                LOGGER.info("New client connection: " + clientId + " from " + clientSocket.getRemoteSocketAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientId);
                if (clientHandler.initialize()) {
                    clients.put(clientId, clientHandler);
                    clientThreadPool.execute(clientHandler);
                } else {
                    LOGGER.warning("Failed to initialize client handler for " + clientId);
                }
                
            } catch (SocketException e) {
                if (running.get()) {
                    LOGGER.log(Level.WARNING, "Socket error in acceptor", e);
                }
                break;
            } catch (IOException e) {
                if (running.get()) {
                    LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                }
                break;
            }
        }
    }
    
    /**
     * Removes a client from the server.
     */
    public void removeClient(String clientId) {
        ClientHandler client = clients.remove(clientId);
        if (client != null) {
            String playerId = clientToPlayer.remove(clientId);
            if (playerId != null) {
                players.remove(playerId);
                broadcastLobbyUpdate();
            }
            
            LOGGER.info("Removed client: " + clientId);
        }
    }
    
    /**
     * Sends a message to a specific client.
     */
    public void sendToClient(String clientId, NetworkMessage message) {
        ClientHandler client = clients.get(clientId);
        if (client != null) {
            client.sendMessage(message);
        } else {
            LOGGER.warning("Attempted to send message to non-existent client: " + clientId);
        }
    }
    
    /**
     * Broadcasts a message to all clients in a specific game.
     */
    public void broadcastToGame(String gameId, NetworkMessage message) {
        GameSession session = gameSessions.get(gameId);
        if (session != null) {
            // Send to both players in the game
            String player1Id = session.getRedPlayer().getPlayerId();
            String player2Id = session.getBlackPlayer().getPlayerId();
            
            String client1Id = getClientIdForPlayer(player1Id);
            String client2Id = getClientIdForPlayer(player2Id);
            
            LOGGER.info("BroadcastToGame " + gameId + ": message type=" + message.getType() + 
                ", client1Id=" + client1Id + ", client2Id=" + client2Id);
            
            if (client1Id != null) {
                sendToClient(client1Id, message);
            } else {
                LOGGER.warning("Client1 not found for player " + player1Id);
            }
            if (client2Id != null) {
                sendToClient(client2Id, message);
            } else {
                LOGGER.warning("Client2 not found for player " + player2Id);
            }
        } else {
            LOGGER.warning("GameSession not found for gameId: " + gameId);
        }
    }
    
    /**
     * Broadcasts a message to all connected clients.
     */
    public void broadcastToAll(NetworkMessage message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }
    
    /**
     * Broadcasts lobby updates to all clients.
     */
    public void broadcastLobbyUpdate() {
        List<Player> playerList = new ArrayList<>(players.values());
        List<GameSession> gameList = new ArrayList<>(gameSessions.values());
        
        LobbyUpdateMessage lobbyUpdate = new LobbyUpdateMessage(null, playerList, gameList);
        broadcastToAll(lobbyUpdate);
    }
    
    // NetworkMessageHandler implementation
    
    @Override
    public void handleLoginRequest(LoginMessage message) {
        // This method is kept for interface compatibility but should not be called directly
        LOGGER.warning("handleLoginRequest called without client context");
    }
    
    /**
     * Handle login request with client context.
     */
    public void handleLoginRequest(LoginMessage message, ClientHandler client) {
        // Simple authentication - in real implementation, validate against database
        String username = message.getUsername();
        String password = message.getPassword();
        String clientId = client.getClientId();
        
        // For demo purposes, accept any non-empty username/password
        boolean authenticated = username != null && !username.trim().isEmpty() && 
                               password != null && !password.trim().isEmpty();
        
        if (authenticated) {
            // Create player
            Player player = new Player(username, username);
            player.setStatus(PlayerStatus.ONLINE);  // 设置玩家为在线状态
            players.put(player.getPlayerId(), player);
            clientToPlayer.put(clientId, player.getPlayerId());
            
            LoginResponseMessage response = LoginResponseMessage.success(player);
            sendToClient(clientId, response);
            
            broadcastLobbyUpdate();
            LOGGER.info("Player logged in: " + username + " with clientId: " + clientId);
        } else {
            // Send error response
            LoginResponseMessage response = LoginResponseMessage.failure("Invalid credentials");
            sendToClient(clientId, response);
        }
    }
    
    @Override
    public void handleMoveMessage(MoveMessage message) {
        String gameId = message.getGameId();
        GameSession session = gameSessions.get(gameId);
        ChessEngine engine = gameEngines.get(gameId);
        
        if (session != null && engine != null) {
            Move move = message.getMove();
            
            // Check if this is a resignation
            if (move.isResignation()) {
                // Handle resignation
                Player resigningPlayer = move.getPiece().getOwner();
                Player winner = session.getOpponent(resigningPlayer);
                GameResult result = GameResult.resignation(winner, resigningPlayer);
                
                // Update game state
                session.getGameState().setStatus(GameStatus.RESIGNED);
                
                // Reset both players' status to ONLINE
                resigningPlayer.setStatus(PlayerStatus.ONLINE);
                winner.setStatus(PlayerStatus.ONLINE);
                LOGGER.info("Reset players " + resigningPlayer.getUsername() + " and " + winner.getUsername() + " status to ONLINE after resignation");
                
                // Notify both players
                GameEndMessage endMessage = new GameEndMessage(gameId, result);
                broadcastToGame(gameId, endMessage);
                
                // Broadcast updated player list
                broadcastLobbyUpdate();
                
                LOGGER.info("Player " + resigningPlayer.getUsername() + " resigned from game " + gameId);
                return;
            }
            
            // Execute the move using ChessEngine (which will check for check/checkmate)
            boolean moveExecuted = engine.executeMove(move);
            
            if (moveExecuted) {
                // Get the updated game state from engine
                GameState gameState = engine.getCurrentState();
                
                LOGGER.info("Move executed: " + move.getFrom() + " -> " + move.getTo() + 
                    ", Current player now: " + gameState.getCurrentPlayer().getUsername() +
                    ", Game status: " + gameState.getStatus() +
                    ", Move count: " + gameState.getMoveHistory().size());
                
                // Update session's game state
                session.setGameState(gameState);
                
                // Move successful - broadcast success response
                MoveResponseMessage response = MoveResponseMessage.success(gameId, move);
                LOGGER.info("Broadcasting MoveResponse to game " + gameId);
                broadcastToGame(gameId, response);
                
                // Broadcast updated game state to sync both clients
                GameState stateCopy = gameState.copy();
                LOGGER.info("Created state copy for broadcast, move count: " + stateCopy.getMoveHistory().size() + 
                    ", status: " + stateCopy.getStatus());
                GameStateUpdateMessage stateUpdate = new GameStateUpdateMessage(gameId, stateCopy);
                LOGGER.info("Broadcasting GameStateUpdate to game " + gameId);
                broadcastToGame(gameId, stateUpdate);
                
                session.updateLastActivity();
                
                // Check if game has ended (checkmate, stalemate, etc.)
                // The ChessEngine event listener will have already sent GameEndMessage,
                // but we ensure it's sent after the state update
                GameStatus status = gameState.getStatus();
                if (status == GameStatus.CHECKMATE || status == GameStatus.STALEMATE || 
                    status == GameStatus.RESIGNED || status == GameStatus.DRAW) {
                    // Game has ended - the GameEndMessage should already be sent by event listener
                    LOGGER.info("Game ended with status: " + status);
                }
            } else {
                // Move failed - send error response only to the player who attempted the move
                String senderId = message.getSenderId();
                if (senderId != null) {
                    String clientId = getClientIdForPlayer(senderId);
                    if (clientId != null) {
                        MoveResponseMessage response = MoveResponseMessage.failure(gameId, "Invalid move");
                        sendToClient(clientId, response);
                        LOGGER.warning("Invalid move attempt in game " + gameId + ": " + move);
                    }
                }
            }
        } else if (session == null) {
            LOGGER.warning("Game session not found for gameId: " + gameId);
        } else {
            LOGGER.warning("Chess engine not found for gameId: " + gameId);
        }
    }
    
    @Override
    public void handleChatMessage(ChatMessage message) {
        if (message.isBroadcast()) {
            // Broadcast to all clients
            broadcastToAll(message);
        } else {
            String targetId = message.getTargetId();
            
            // Check if targetId is a game session ID
            GameSession session = gameSessions.get(targetId);
            if (session != null) {
                // Check if this is a draw acceptance
                if (message.getContent().equals("DRAW_ACCEPT")) {
                    // End the game as a draw
                    GameResult result = GameResult.draw(
                        session.getRedPlayer(), 
                        session.getBlackPlayer(), 
                        "Draw by mutual agreement"
                    );
                    session.getGameState().setStatus(GameStatus.DRAW);
                    
                    // Reset both players' status to ONLINE
                    session.getRedPlayer().setStatus(PlayerStatus.ONLINE);
                    session.getBlackPlayer().setStatus(PlayerStatus.ONLINE);
                    LOGGER.info("Reset players " + session.getRedPlayer().getUsername() + " and " + 
                               session.getBlackPlayer().getUsername() + " status to ONLINE after draw");
                    
                    // Notify both players
                    GameEndMessage endMessage = new GameEndMessage(targetId, result);
                    broadcastToGame(targetId, endMessage);
                    
                    // Broadcast updated player list
                    broadcastLobbyUpdate();
                    
                    LOGGER.info("Game " + targetId + " ended in draw by mutual agreement");
                    return;
                }
                
                // This is a game chat - broadcast to both players in the game
                broadcastToGame(targetId, message);
            } else {
                // This is a private message to a specific player
                String targetClientId = getClientIdForPlayer(targetId);
                if (targetClientId != null) {
                    sendToClient(targetClientId, message);
                }
            }
        }
    }
    
    @Override
    public void handleDisconnection(String clientId) {
        removeClient(clientId);
    }
    
    @Override
    public void handleGameInvitation(GameInvitationMessage message) {
        String invitationId = message.getInvitationId();
        if (invitationId == null || invitationId.isEmpty()) {
            invitationId = UUID.randomUUID().toString();
        }
        
        LOGGER.info("Handling game invitation from " + message.getSenderId() + " to " + message.getTargetPlayerId());
        
        // Check if target player is already in a game
        Player targetPlayer = players.get(message.getTargetPlayerId());
        if (targetPlayer != null && targetPlayer.getStatus() == PlayerStatus.IN_GAME) {
            LOGGER.warning("Target player " + message.getTargetPlayerId() + " is already in a game");
            String senderClientId = getClientIdForPlayer(message.getSenderId());
            if (senderClientId != null) {
                ErrorMessage error = new ErrorMessage(null, "PLAYER_IN_GAME", "该玩家正在进行游戏，无法邀请！");
                sendToClient(senderClientId, error);
            }
            return;
        }
        
        // Store pending invitation with the invitation ID
        GameInvitationMessage storedInvitation = new GameInvitationMessage(
            message.getSenderId(), message.getTargetPlayerId(), invitationId
        );
        pendingInvitations.put(invitationId, storedInvitation);
        
        // Forward invitation to target player
        String targetClientId = getClientIdForPlayer(message.getTargetPlayerId());
        if (targetClientId != null) {
            GameInvitationMessage forwardedInvitation = new GameInvitationMessage(
                message.getSenderId(), message.getTargetPlayerId(), invitationId
            );
            sendToClient(targetClientId, forwardedInvitation);
            LOGGER.info("Forwarded invitation to client " + targetClientId);
        } else {
            LOGGER.warning("Target player not found: " + message.getTargetPlayerId());
            // Notify sender that target is not available
            String senderClientId = getClientIdForPlayer(message.getSenderId());
            if (senderClientId != null) {
                ErrorMessage error = new ErrorMessage(null, "PLAYER_NOT_FOUND", "Target player is not available");
                sendToClient(senderClientId, error);
            }
        }
    }
    
    // Additional handler methods
    
    public void handleInvitationResponse(InvitationResponseMessage message) {
        String invitationId = message.getInvitationId();
        GameInvitationMessage invitation = pendingInvitations.remove(invitationId);
        
        if (invitation != null) {
            if (message.isAccepted()) {
                // Create new game session
                Player player1 = players.get(invitation.getSenderId());
                Player player2 = players.get(message.getSenderId());
                
                if (player1 != null && player2 != null) {
                    String gameId = UUID.randomUUID().toString();
                    GameSession session = new GameSession(gameId, player1, player2);
                    gameSessions.put(gameId, session);
                    
                    // Create ChessEngine for this game
                    ChessEngine engine = new ChessEngine(session.getGameState());
                    
                    // Add event listener to handle game end
                    engine.addEventListener(new GameEventListener() {
                        @Override
                        public void onMoveExecuted(Move move) {}
                        
                        @Override
                        public void onGameStateChanged(GameState state) {}
                        
                        @Override
                        public void onPlayerJoined(Player player) {}
                        
                        @Override
                        public void onPlayerLeft(Player player) {}
                        
                        @Override
                        public void onGameEnded(GameResult result) {
                            // Add a small delay to ensure GameStateUpdateMessage is processed first
                            new Thread(() -> {
                                try {
                                    Thread.sleep(100); // 100ms delay
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                // Notify both players that game has ended
                                GameEndMessage endMessage = new GameEndMessage(gameId, result);
                                broadcastToGame(gameId, endMessage);
                                LOGGER.info("Game ended: " + gameId + ", Result: " + result.getEndStatus());
                                
                                // Reset players' status to ONLINE after game ends
                                GameSession endedSession = gameSessions.get(gameId);
                                if (endedSession != null) {
                                    Player p1 = endedSession.getRedPlayer();
                                    Player p2 = endedSession.getBlackPlayer();
                                    if (p1 != null) {
                                        p1.setStatus(PlayerStatus.ONLINE);
                                        LOGGER.info("Reset player " + p1.getUsername() + " status to ONLINE");
                                    }
                                    if (p2 != null) {
                                        p2.setStatus(PlayerStatus.ONLINE);
                                        LOGGER.info("Reset player " + p2.getUsername() + " status to ONLINE");
                                    }
                                    // Broadcast updated player list
                                    broadcastLobbyUpdate();
                                }
                            }).start();
                        }
                        
                        @Override
                        public void onInvalidMoveAttempted(Move move, String reason) {}
                        
                        @Override
                        public void onGameStateCorrupted(String reason) {
                            LOGGER.severe("Game state corrupted for game " + gameId + ": " + reason);
                        }
                    });
                    
                    gameEngines.put(gameId, engine);
                    
                    // Set both players' status to IN_GAME
                    player1.setStatus(PlayerStatus.IN_GAME);
                    player2.setStatus(PlayerStatus.IN_GAME);
                    LOGGER.info("Set players " + player1.getUsername() + " and " + player2.getUsername() + " status to IN_GAME");
                    
                    // Notify both players
                    GameStartMessage startMessage = new GameStartMessage(null, gameId, session);
                    broadcastToGame(gameId, startMessage);
                    
                    // Broadcast updated player list to all clients
                    broadcastLobbyUpdate();
                }
            } else {
                // Notify inviter that invitation was declined
                String inviterClientId = getClientIdForPlayer(invitation.getSenderId());
                if (inviterClientId != null) {
                    ErrorMessage errorMsg = new ErrorMessage(null, "INVITATION_DECLINED", "Game invitation declined");
                    sendToClient(inviterClientId, errorMsg);
                }
            }
        }
    }
    
    public void handlePlayerListRequest(PlayerListRequestMessage message, ClientHandler client) {
        List<Player> playerList = new ArrayList<>(players.values());
        PlayerListResponseMessage response = new PlayerListResponseMessage(null, playerList);
        client.sendMessage(response);
    }
    
    public void handleGameListRequest(GameListRequestMessage message, ClientHandler client) {
        List<GameSession> gameList = new ArrayList<>(gameSessions.values());
        GameListResponseMessage response = new GameListResponseMessage(null, gameList);
        client.sendMessage(response);
    }
    
    public void handleLogout(LogoutMessage message, ClientHandler client) {
        String clientId = client.getClientId();
        removeClient(clientId);
        client.stop();
    }
    
    // Helper methods
    
    private String getClientIdForPlayer(String playerId) {
        return clientToPlayer.entrySet().stream()
            .filter(entry -> entry.getValue().equals(playerId))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Performs periodic maintenance tasks.
     */
    private void performMaintenance() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(30000); // Run maintenance every 30 seconds
                
                // Remove inactive clients
                long currentTime = System.currentTimeMillis();
                List<String> inactiveClients = new ArrayList<>();
                
                for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                    ClientHandler client = entry.getValue();
                    if (!client.isRunning() || 
                        (currentTime - client.getLastHeartbeat() > 120000)) { // 2 minutes timeout
                        inactiveClients.add(entry.getKey());
                    }
                }
                
                for (String clientId : inactiveClients) {
                    LOGGER.info("Removing inactive client: " + clientId);
                    removeClient(clientId);
                }
                
                // Clean up finished games
                List<String> finishedGames = new ArrayList<>();
                for (Map.Entry<String, GameSession> entry : gameSessions.entrySet()) {
                    GameSession session = entry.getValue();
                    if (session.isEnded()) {
                        finishedGames.add(entry.getKey());
                    }
                }
                
                for (String gameId : finishedGames) {
                    gameSessions.remove(gameId);
                }
                
                if (!inactiveClients.isEmpty() || !finishedGames.isEmpty()) {
                    broadcastLobbyUpdate();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Gets the current number of connected clients.
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Gets the current number of active games.
     */
    public int getGameCount() {
        return gameSessions.size();
    }
    
    /**
     * Checks if the server is running.
     */
    public boolean isRunning() {
        return running.get();
    }
}