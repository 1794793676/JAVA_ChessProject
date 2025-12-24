package com.xiangqi.client.network;

import com.xiangqi.shared.network.NetworkMessage;
import com.xiangqi.shared.network.NetworkMessageHandler;
import com.xiangqi.shared.network.messages.DisconnectMessage;
import com.xiangqi.shared.network.messages.HeartbeatMessage;

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
 * Network client for handling socket connections to the game server.
 * Manages message sending/receiving, connection management, and reconnection.
 */
public class NetworkClient {
    private static final Logger LOGGER = Logger.getLogger(NetworkClient.class.getName());
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    private static final int RECONNECT_DELAY = 5000; // 5 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private NetworkMessageHandler messageHandler;
    
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private final BlockingQueue<NetworkMessage> outgoingMessages = new LinkedBlockingQueue<>();
    
    private Thread receiverThread;
    private Thread senderThread;
    private Thread heartbeatThread;
    
    private String serverAddress;
    private int serverPort;
    private String clientId;
    
    /**
     * Constructs a new NetworkClient with the specified message handler.
     */
    public NetworkClient(NetworkMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
    
    /**
     * Connects to the game server at the specified address and port.
     * 
     * @param serverAddress The server IP address or hostname
     * @param port The server port number
     * @return true if connection was successful, false otherwise
     */
    public boolean connect(String serverAddress, int port) {
        return connect(serverAddress, port, null);
    }
    
    /**
     * Connects to the game server with a specific client ID.
     * 
     * @param serverAddress The server IP address or hostname
     * @param port The server port number
     * @param clientId The client identifier for this connection
     * @return true if connection was successful, false otherwise
     */
    public boolean connect(String serverAddress, int port, String clientId) {
        this.serverAddress = serverAddress;
        this.serverPort = port;
        this.clientId = clientId;
        
        try {
            // Create socket connection
            socket = new Socket(serverAddress, port);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            
            // Create streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush(); // Important: flush before creating input stream
            inputStream = new ObjectInputStream(socket.getInputStream());
            
            connected.set(true);
            shouldReconnect.set(true);
            
            // Start communication threads
            startCommunicationThreads();
            
            LOGGER.info("Successfully connected to server at " + serverAddress + ":" + port);
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to server", e);
            cleanup();
            return false;
        }
    }
    
    /**
     * Sends a message to the server.
     * 
     * @param message The message to send
     */
    public void sendMessage(NetworkMessage message) {
        if (!connected.get()) {
            LOGGER.warning("Cannot send message - not connected to server");
            return;
        }
        
        try {
            outgoingMessages.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interrupted while queuing message", e);
        }
    }
    
    /**
     * Disconnects from the server gracefully.
     */
    public void disconnect() {
        shouldReconnect.set(false);
        
        if (connected.get()) {
            // Send disconnect message
            sendMessage(new DisconnectMessage(clientId, "Client disconnecting"));
            
            // Wait a moment for the message to be sent
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        cleanup();
        LOGGER.info("Disconnected from server");
    }
    
    /**
     * Checks if the client is currently connected to the server.
     */
    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }
    
    /**
     * Gets the client ID for this connection.
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Sets the client ID for this connection.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /**
     * Attempts to reconnect to the server.
     */
    private void attemptReconnection() {
        if (!shouldReconnect.get()) {
            return;
        }
        
        int attempts = 0;
        while (attempts < MAX_RECONNECT_ATTEMPTS && shouldReconnect.get()) {
            attempts++;
            LOGGER.info("Attempting to reconnect... (attempt " + attempts + "/" + MAX_RECONNECT_ATTEMPTS + ")");
            
            try {
                Thread.sleep(RECONNECT_DELAY);
                
                if (connect(serverAddress, serverPort, clientId)) {
                    LOGGER.info("Reconnection successful");
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        LOGGER.severe("Failed to reconnect after " + MAX_RECONNECT_ATTEMPTS + " attempts");
        if (messageHandler != null) {
            messageHandler.handleDisconnection(clientId);
        }
    }
    
    /**
     * Starts the communication threads for sending and receiving messages.
     */
    private void startCommunicationThreads() {
        // Receiver thread
        receiverThread = new Thread(this::receiveMessages, "NetworkClient-Receiver");
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        // Sender thread
        senderThread = new Thread(this::sendMessages, "NetworkClient-Sender");
        senderThread.setDaemon(true);
        senderThread.start();
        
        // Heartbeat thread
        heartbeatThread = new Thread(this::sendHeartbeats, "NetworkClient-Heartbeat");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }
    
    /**
     * Continuously receives messages from the server.
     */
    private void receiveMessages() {
        while (connected.get() && !Thread.currentThread().isInterrupted()) {
            try {
                NetworkMessage message = (NetworkMessage) inputStream.readObject();
                
                if (messageHandler != null) {
                    handleReceivedMessage(message);
                }
                
            } catch (SocketTimeoutException e) {
                // Timeout is normal, continue listening
                continue;
            } catch (SocketException e) {
                if (connected.get()) {
                    LOGGER.log(Level.WARNING, "Socket error while receiving", e);
                    handleConnectionLoss();
                }
                break;
            } catch (IOException | ClassNotFoundException e) {
                if (connected.get()) {
                    LOGGER.log(Level.SEVERE, "Error receiving message", e);
                    handleConnectionLoss();
                }
                break;
            }
        }
    }
    
    /**
     * Continuously sends queued messages to the server.
     */
    private void sendMessages() {
        while (connected.get() && !Thread.currentThread().isInterrupted()) {
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
                if (connected.get()) {
                    LOGGER.log(Level.SEVERE, "Error sending message", e);
                    handleConnectionLoss();
                }
                break;
            }
        }
    }
    
    /**
     * Sends periodic heartbeat messages to maintain connection.
     */
    private void sendHeartbeats() {
        while (connected.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(15000); // Send heartbeat every 15 seconds
                
                if (connected.get()) {
                    sendMessage(new HeartbeatMessage(clientId));
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Handles received messages by dispatching to appropriate handler methods.
     */
    private void handleReceivedMessage(NetworkMessage message) {
        try {
            switch (message.getType()) {
                case LOGIN_RESPONSE:
                    if (message instanceof com.xiangqi.shared.network.messages.LoginResponseMessage) {
                        messageHandler.handleLoginResponse((com.xiangqi.shared.network.messages.LoginResponseMessage) message);
                    }
                    break;
                case LOBBY_UPDATE:
                    if (message instanceof com.xiangqi.shared.network.messages.LobbyUpdateMessage) {
                        messageHandler.handleLobbyUpdate((com.xiangqi.shared.network.messages.LobbyUpdateMessage) message);
                    }
                    break;
                case MOVE_RESPONSE:
                    if (message instanceof com.xiangqi.shared.network.messages.MoveResponseMessage) {
                        messageHandler.handleMoveResponse((com.xiangqi.shared.network.messages.MoveResponseMessage) message);
                    }
                    break;
                case CHAT_MESSAGE:
                    if (message instanceof com.xiangqi.shared.network.messages.ChatMessage) {
                        messageHandler.handleChatMessage((com.xiangqi.shared.network.messages.ChatMessage) message);
                    }
                    break;
                case GAME_INVITATION:
                    if (message instanceof com.xiangqi.shared.network.messages.GameInvitationMessage) {
                        messageHandler.handleGameInvitation((com.xiangqi.shared.network.messages.GameInvitationMessage) message);
                    }
                    break;
                case GAME_START:
                    if (message instanceof com.xiangqi.shared.network.messages.GameStartMessage) {
                        messageHandler.handleGameStart((com.xiangqi.shared.network.messages.GameStartMessage) message);
                    }
                    break;
                case GAME_STATE_UPDATE:
                    if (message instanceof com.xiangqi.shared.network.messages.GameStateUpdateMessage) {
                        LOGGER.info("Received GAME_STATE_UPDATE message, dispatching to handler");
                        messageHandler.handleGameStateUpdate((com.xiangqi.shared.network.messages.GameStateUpdateMessage) message);
                    } else {
                        LOGGER.warning("GAME_STATE_UPDATE message type mismatch: " + 
                            (message != null ? message.getClass().getName() : "null"));
                    }
                    break;
                case GAME_END:
                    if (message instanceof com.xiangqi.shared.network.messages.GameEndMessage) {
                        LOGGER.info("Received GAME_END message, dispatching to handler");
                        messageHandler.handleGameEnd((com.xiangqi.shared.network.messages.GameEndMessage) message);
                    } else {
                        LOGGER.warning("GAME_END message type mismatch: " + 
                            (message != null ? message.getClass().getName() : "null"));
                    }
                    break;
                case PLAYER_LIST_RESPONSE:
                    if (message instanceof com.xiangqi.shared.network.messages.PlayerListResponseMessage) {
                        messageHandler.handlePlayerListResponse((com.xiangqi.shared.network.messages.PlayerListResponseMessage) message);
                    }
                    break;
                case GAME_LIST_RESPONSE:
                    if (message instanceof com.xiangqi.shared.network.messages.GameListResponseMessage) {
                        messageHandler.handleGameListResponse((com.xiangqi.shared.network.messages.GameListResponseMessage) message);
                    }
                    break;
                case ERROR_MESSAGE:
                    if (message instanceof com.xiangqi.shared.network.messages.ErrorMessage) {
                        messageHandler.handleError((com.xiangqi.shared.network.messages.ErrorMessage) message);
                    }
                    break;
                case HEARTBEAT:
                    // Heartbeat received - connection is alive
                    break;
                default:
                    LOGGER.warning("Unhandled message type: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling received message", e);
        }
    }
    
    /**
     * Handles connection loss and initiates reconnection if appropriate.
     */
    private void handleConnectionLoss() {
        if (connected.getAndSet(false)) {
            LOGGER.warning("Connection lost to server");
            cleanup();
            
            if (shouldReconnect.get()) {
                attemptReconnection();
            }
        }
    }
    
    /**
     * Cleans up resources and stops all threads.
     */
    private void cleanup() {
        connected.set(false);
        
        // Interrupt and stop threads
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        if (senderThread != null) {
            senderThread.interrupt();
        }
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
        
        // Close streams and socket
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing input stream", e);
        }
        
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing output stream", e);
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing socket", e);
        }
        
        // Clear message queue
        outgoingMessages.clear();
    }
}