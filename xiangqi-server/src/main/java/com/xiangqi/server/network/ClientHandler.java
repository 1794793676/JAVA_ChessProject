package com.xiangqi.server.network;

import com.xiangqi.shared.network.NetworkMessage;
import com.xiangqi.shared.network.NetworkMessageHandler;
import com.xiangqi.shared.network.messages.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles communication with a single client connection.
 * Manages message sending/receiving for one client session.
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    
    private final Socket clientSocket;
    private final GameServer gameServer;
    private final String clientId;
    
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private NetworkMessageHandler messageHandler;
    
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final BlockingQueue<NetworkMessage> outgoingMessages = new LinkedBlockingQueue<>();
    
    private Thread senderThread;
    private String playerId;
    private long lastHeartbeat;
    
    /**
     * Constructs a new ClientHandler for the given client socket.
     */
    public ClientHandler(Socket clientSocket, GameServer gameServer, String clientId) {
        this.clientSocket = clientSocket;
        this.gameServer = gameServer;
        this.clientId = clientId;
        this.lastHeartbeat = System.currentTimeMillis();
        this.messageHandler = gameServer; // GameServer implements NetworkMessageHandler
    }
    
    /**
     * Initializes the client handler and starts communication.
     */
    public boolean initialize() {
        try {
            clientSocket.setSoTimeout(SOCKET_TIMEOUT);
            
            // Create streams - output first, then input
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            
            // Start sender thread
            senderThread = new Thread(this::sendMessages, "ClientHandler-Sender-" + clientId);
            senderThread.setDaemon(true);
            senderThread.start();
            
            LOGGER.info("Client handler initialized for client: " + clientId);
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize client handler for " + clientId, e);
            cleanup();
            return false;
        }
    }
    
    /**
     * Main execution loop - receives messages from the client.
     */
    @Override
    public void run() {
        LOGGER.info("Client handler started for: " + clientId);
        
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                NetworkMessage message = (NetworkMessage) inputStream.readObject();
                
                // Update heartbeat timestamp
                if (message.getType() == com.xiangqi.shared.network.MessageType.HEARTBEAT) {
                    lastHeartbeat = System.currentTimeMillis();
                }
                
                // Handle the message
                handleReceivedMessage(message);
                
            } catch (SocketTimeoutException e) {
                // Check if client is still alive (heartbeat timeout)
                if (System.currentTimeMillis() - lastHeartbeat > 60000) { // 1 minute timeout
                    LOGGER.warning("Client " + clientId + " heartbeat timeout");
                    break;
                }
                continue;
            } catch (SocketException e) {
                if (running.get()) {
                    LOGGER.info("Client " + clientId + " disconnected");
                }
                break;
            } catch (IOException | ClassNotFoundException e) {
                if (running.get()) {
                    LOGGER.log(Level.WARNING, "Error receiving message from client " + clientId, e);
                }
                break;
            }
        }
        
        cleanup();
        gameServer.removeClient(clientId);
        LOGGER.info("Client handler stopped for: " + clientId);
    }
    
    /**
     * Sends a message to the client.
     */
    public void sendMessage(NetworkMessage message) {
        if (!running.get()) {
            return;
        }
        
        try {
            outgoingMessages.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interrupted while queuing message for client " + clientId, e);
        }
    }
    
    /**
     * Stops the client handler gracefully.
     */
    public void stop() {
        running.set(false);
        
        if (senderThread != null) {
            senderThread.interrupt();
        }
        
        cleanup();
    }
    
    /**
     * Gets the client ID.
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Gets the player ID associated with this client.
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Sets the player ID for this client.
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    /**
     * Checks if the client handler is running.
     */
    public boolean isRunning() {
        return running.get() && !clientSocket.isClosed();
    }
    
    /**
     * Gets the last heartbeat timestamp.
     */
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    /**
     * Continuously sends queued messages to the client.
     */
    private void sendMessages() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                NetworkMessage message = outgoingMessages.take();
                
                synchronized (outputStream) {
                    outputStream.writeObject(message);
                    outputStream.flush();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                if (running.get()) {
                    LOGGER.log(Level.WARNING, "Error sending message to client " + clientId, e);
                    stop();
                }
                break;
            }
        }
    }
    
    /**
     * Handles received messages by dispatching to the game server.
     */
    private void handleReceivedMessage(NetworkMessage message) {
        try {
            // Validate message before processing
            if (!validateMessage(message)) {
                sendErrorMessage("INVALID_MESSAGE", "Message validation failed");
                return;
            }
            
            switch (message.getType()) {
                case LOGIN_REQUEST:
                    if (message instanceof LoginMessage) {
                        messageHandler.handleLoginRequest((LoginMessage) message);
                    }
                    break;
                    
                case MOVE_REQUEST:
                    if (message instanceof MoveMessage) {
                        messageHandler.handleMoveMessage((MoveMessage) message);
                    }
                    break;
                    
                case CHAT_MESSAGE:
                    if (message instanceof ChatMessage) {
                        messageHandler.handleChatMessage((ChatMessage) message);
                    }
                    break;
                    
                case GAME_INVITATION:
                    if (message instanceof GameInvitationMessage) {
                        messageHandler.handleGameInvitation((GameInvitationMessage) message);
                    }
                    break;
                    
                case INVITATION_RESPONSE:
                    if (message instanceof InvitationResponseMessage) {
                        gameServer.handleInvitationResponse((InvitationResponseMessage) message);
                    }
                    break;
                    
                case PLAYER_LIST_REQUEST:
                    if (message instanceof PlayerListRequestMessage) {
                        gameServer.handlePlayerListRequest((PlayerListRequestMessage) message, this);
                    }
                    break;
                    
                case GAME_LIST_REQUEST:
                    if (message instanceof GameListRequestMessage) {
                        gameServer.handleGameListRequest((GameListRequestMessage) message, this);
                    }
                    break;
                    
                case LOGOUT_REQUEST:
                    if (message instanceof LogoutMessage) {
                        gameServer.handleLogout((LogoutMessage) message, this);
                    }
                    break;
                    
                case DISCONNECT:
                    if (message instanceof DisconnectMessage) {
                        messageHandler.handleDisconnection(clientId);
                        stop();
                    }
                    break;
                    
                case HEARTBEAT:
                    // Heartbeat handled above - just update timestamp
                    break;
                    
                default:
                    LOGGER.warning("Unhandled message type from client " + clientId + ": " + message.getType());
                    sendErrorMessage("UNSUPPORTED_MESSAGE", "Message type not supported: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling message from client " + clientId, e);
            sendErrorMessage("PROCESSING_ERROR", "Error processing message: " + e.getMessage());
        }
    }
    
    /**
     * Validates incoming messages for basic integrity.
     */
    private boolean validateMessage(NetworkMessage message) {
        if (message == null) {
            LOGGER.warning("Received null message from client " + clientId);
            return false;
        }
        
        if (message.getType() == null) {
            LOGGER.warning("Received message with null type from client " + clientId);
            return false;
        }
        
        // LOGIN_REQUEST messages are allowed to have null sender ID (user not logged in yet)
        // All other messages must have a valid sender ID
        if (message.getSenderId() == null) {
            if (message.getType() == com.xiangqi.shared.network.MessageType.LOGIN_REQUEST) {
                // LOGIN_REQUEST with null sender ID is valid
                return true;
            } else {
                LOGGER.warning("Received message with null sender ID from client " + clientId);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Sends an error message to the client.
     */
    public void sendErrorMessage(String errorCode, String errorDescription) {
        try {
            ErrorMessage errorMessage = new ErrorMessage("server", errorCode, errorDescription);
            sendMessage(errorMessage);
            LOGGER.info("Sent error message to client " + clientId + ": " + errorCode + " - " + errorDescription);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send error message to client " + clientId, e);
        }
    }
    
    /**
     * Cleans up resources.
     */
    private void cleanup() {
        running.set(false);
        
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing input stream for client " + clientId, e);
        }
        
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing output stream for client " + clientId, e);
        }
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing socket for client " + clientId, e);
        }
        
        outgoingMessages.clear();
    }
}