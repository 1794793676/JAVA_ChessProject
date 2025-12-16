package com.xiangqi.server.network;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.model.Player;
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
            
            if (client1Id != null) {
                sendToClient(client1Id, message);
            }
            if (client2Id != null) {
                sendToClient(client2Id, message);
            }
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
        // Simple authentication - in real implementation, validate against database
        String username = message.getUsername();
        String password = message.getPassword();
        
        // For demo purposes, accept any non-empty username/password
        boolean authenticated = username != null && !username.trim().isEmpty() && 
                               password != null && !password.trim().isEmpty();
        
        if (authenticated) {
            // Create player
            Player player = new Player(username, username);
            players.put(player.getPlayerId(), player);
            
            // Find client ID for this login (this is simplified - in real implementation, 
            // we'd need to track which client sent the login request)
            String clientId = findClientForLogin(message);
            if (clientId != null) {
                clientToPlayer.put(clientId, player.getPlayerId());
                
                LoginResponseMessage response = LoginResponseMessage.success(player);
                sendToClient(clientId, response);
                
                broadcastLobbyUpdate();
                LOGGER.info("Player logged in: " + username);
            }
        } else {
            // Send error response
            String clientId = findClientForLogin(message);
            if (clientId != null) {
                LoginResponseMessage response = LoginResponseMessage.failure("Invalid credentials");
                sendToClient(clientId, response);
            }
        }
    }
    
    @Override
    public void handleMoveMessage(MoveMessage message) {
        String gameId = message.getGameId();
        GameSession session = gameSessions.get(gameId);
        
        if (session != null) {
            // Validate and process the move
            // This would integrate with the chess engine
            
            // For now, just broadcast the move to both players
            MoveResponseMessage response = MoveResponseMessage.success(gameId, message.getMove());
            broadcastToGame(gameId, response);
            
            // Update game state
            // session.processMove(message.getMove());
        }
    }
    
    @Override
    public void handleChatMessage(ChatMessage message) {
        if (message.isBroadcast()) {
            // Broadcast to all clients
            broadcastToAll(message);
        } else {
            // Send to specific target
            String targetClientId = getClientIdForPlayer(message.getTargetId());
            if (targetClientId != null) {
                sendToClient(targetClientId, message);
            }
        }
    }
    
    @Override
    public void handleDisconnection(String clientId) {
        removeClient(clientId);
    }
    
    @Override
    public void handleGameInvitation(GameInvitationMessage message) {
        String invitationId = UUID.randomUUID().toString();
        pendingInvitations.put(invitationId, message);
        
        // Forward invitation to target player
        String targetClientId = getClientIdForPlayer(message.getTargetPlayerId());
        if (targetClientId != null) {
            GameInvitationMessage forwardedInvitation = new GameInvitationMessage(
                message.getSenderId(), message.getTargetPlayerId(), invitationId
            );
            sendToClient(targetClientId, forwardedInvitation);
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
                    
                    // Notify both players
                    GameStartMessage startMessage = new GameStartMessage(null, gameId, session);
                    broadcastToGame(gameId, startMessage);
                    
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
    
    private String findClientForLogin(LoginMessage message) {
        // In a real implementation, we'd need to track which client sent which message
        // For now, return the first available client (this is a simplification)
        return clients.keySet().stream().findFirst().orElse(null);
    }
    
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