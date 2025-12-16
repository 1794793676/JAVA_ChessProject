package com.xiangqi.client;

import com.xiangqi.client.network.NetworkClient;
import com.xiangqi.client.ui.GameFrame;
import com.xiangqi.client.ui.LobbyFrame;
import com.xiangqi.client.ui.LoginFrame;
import com.xiangqi.client.multimedia.AudioManager;
import com.xiangqi.client.multimedia.ResourceManager;
import com.xiangqi.shared.model.*;
import com.xiangqi.shared.network.NetworkMessageHandler;
import com.xiangqi.shared.network.messages.*;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main client controller that coordinates all client components.
 * Handles interface switching, event listening, and network communication.
 * 实现需求 1.4, 2.4
 */
public class GameClient implements NetworkMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(GameClient.class.getName());
    private static final String DEFAULT_SERVER_ADDRESS = "localhost";
    private static final int DEFAULT_SERVER_PORT = 8888;
    
    // UI Components
    private LoginFrame loginFrame;
    private LobbyFrame lobbyFrame;
    private GameFrame gameFrame;
    
    // Network and multimedia
    private NetworkClient networkClient;
    private AudioManager audioManager;
    private ResourceManager resourceManager;
    
    // Game state
    private Player currentPlayer;
    private GameSession currentGameSession;
    private final ConcurrentMap<String, String> pendingInvitations = new ConcurrentHashMap<>();
    
    // Connection settings
    private String serverAddress = DEFAULT_SERVER_ADDRESS;
    private int serverPort = DEFAULT_SERVER_PORT;
    
    /**
     * Constructs a new GameClient and initializes all components.
     */
    public GameClient() {
        initializeComponents();
        setupEventListeners();
        LOGGER.info("GameClient initialized");
    }
    
    /**
     * Constructs a GameClient with custom server settings.
     */
    public GameClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        initializeComponents();
        setupEventListeners();
        LOGGER.info("GameClient initialized with server " + serverAddress + ":" + serverPort);
    }
    
    /**
     * Initialize all client components.
     */
    private void initializeComponents() {
        try {
            // Initialize multimedia components
            resourceManager = ResourceManager.getInstance();
            audioManager = AudioManager.getInstance();
            
            // Initialize network client
            networkClient = new NetworkClient(this);
            
            // Initialize UI components
            loginFrame = new LoginFrame();
            lobbyFrame = new LobbyFrame();
            
            LOGGER.info("All components initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize components", e);
            showErrorDialog("初始化失败", "无法初始化游戏组件: " + e.getMessage());
        }
    }
    
    /**
     * Setup event listeners for all UI components.
     */
    private void setupEventListeners() {
        // Login frame listeners
        loginFrame.setLoginListener(new LoginFrame.LoginListener() {
            @Override
            public void onLoginAttempt(String username, String password) {
                handleLoginAttempt(username, password);
            }
        });
        
        // Lobby frame listeners
        lobbyFrame.setLobbyListener(new LobbyFrame.ExtendedLobbyListener() {
            @Override
            public void onGameInvitation(Player targetPlayer) {
                handleGameInvitation(targetPlayer);
            }
            
            @Override
            public void onRefreshRequest() {
                handleRefreshRequest();
            }
            
            @Override
            public void onLogout() {
                handleLogout();
            }
            
            @Override
            public void onJoinGame(GameSession gameSession) {
                handleJoinGame(gameSession);
            }
            
            @Override
            public void onInvitationResponse(String invitationId, boolean accepted) {
                handleInvitationResponse(invitationId, accepted);
            }
        });
    }
    
    /**
     * Starts the game client by showing the login interface.
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            // Note: Look and feel setting commented out due to compilation issues
            // This can be re-enabled later if needed
            
            showLoginInterface();
        });
    }
    
    /**
     * Shows the login interface.
     */
    private void showLoginInterface() {
        SwingUtilities.invokeLater(() -> {
            if (lobbyFrame != null) {
                lobbyFrame.hideLobby();
            }
            if (gameFrame != null) {
                gameFrame.setVisible(false);
            }
            
            loginFrame.showLoginDialog();
            LOGGER.info("Login interface displayed");
        });
    }
    
    /**
     * Shows the lobby interface.
     */
    private void showLobbyInterface() {
        SwingUtilities.invokeLater(() -> {
            loginFrame.setVisible(false);
            if (gameFrame != null) {
                gameFrame.setVisible(false);
            }
            
            lobbyFrame.setCurrentPlayer(currentPlayer);
            lobbyFrame.showLobby();
            LOGGER.info("Lobby interface displayed");
        });
    }
    
    /**
     * Shows the game interface.
     */
    private void showGameInterface(GameSession gameSession) {
        SwingUtilities.invokeLater(() -> {
            lobbyFrame.hideLobby();
            
            if (gameFrame != null) {
                gameFrame.dispose();
            }
            
            gameFrame = new GameFrame(currentPlayer, networkClient);
            gameFrame.setGameEventListener(new GameFrame.GameEventListener() {
                @Override
                public void onMoveAttempted(Move move) {
                    handleMoveAttempt(move);
                }
                
                @Override
                public void onChatMessageSent(String message) {
                    handleChatMessage(message);
                }
                
                @Override
                public void onResignRequested() {
                    handleResignRequest();
                }
                
                @Override
                public void onDrawOfferRequested() {
                    handleDrawOfferRequest();
                }
                
                @Override
                public void onNewGameRequested() {
                    handleNewGameRequest();
                }
            });
            
            currentGameSession = gameSession;
            gameFrame.updateGameState(gameSession.getGameState());
            gameFrame.setVisible(true);
            
            // Play game start sound
            audioManager.playSound("seat");
            
            LOGGER.info("Game interface displayed for session: " + gameSession.getSessionId());
        });
    }
    
    /**
     * Handle login attempt from UI.
     */
    private void handleLoginAttempt(String username, String password) {
        // Connect to server if not already connected
        if (!networkClient.isConnected()) {
            if (!networkClient.connect(serverAddress, serverPort)) {
                loginFrame.onLoginFailure("无法连接到服务器");
                return;
            }
        }
        
        // Send login message
        LoginMessage loginMessage = new LoginMessage(username, password);
        networkClient.sendMessage(loginMessage);
        
        LOGGER.info("Login attempt for user: " + username);
    }
    
    /**
     * Handle game invitation from lobby.
     */
    private void handleGameInvitation(Player targetPlayer) {
        String invitationId = java.util.UUID.randomUUID().toString();
        GameInvitationMessage invitation = new GameInvitationMessage(
            currentPlayer.getPlayerId(),
            targetPlayer.getPlayerId(),
            invitationId
        );
        networkClient.sendMessage(invitation);
        
        lobbyFrame.showStatus("已向 " + targetPlayer.getUsername() + " 发送游戏邀请");
        LOGGER.info("Game invitation sent to: " + targetPlayer.getUsername());
    }
    
    /**
     * Handle refresh request from lobby.
     */
    private void handleRefreshRequest() {
        // Request updated player and game lists
        networkClient.sendMessage(new PlayerListRequestMessage(currentPlayer.getPlayerId()));
        networkClient.sendMessage(new GameListRequestMessage(currentPlayer.getPlayerId()));
    }
    
    /**
     * Handle logout request.
     */
    private void handleLogout() {
        if (networkClient.isConnected()) {
            LogoutMessage logoutMessage = new LogoutMessage(currentPlayer.getPlayerId());
            networkClient.sendMessage(logoutMessage);
        }
        
        currentPlayer = null;
        currentGameSession = null;
        pendingInvitations.clear();
        
        showLoginInterface();
        LOGGER.info("User logged out");
    }
    
    /**
     * Handle join game request.
     */
    private void handleJoinGame(GameSession gameSession) {
        // This would be used for spectating games (future feature)
        LOGGER.info("Join game requested for: " + gameSession.getSessionId());
    }
    
    /**
     * Handle invitation response.
     */
    private void handleInvitationResponse(String invitationId, boolean accepted) {
        InvitationResponseMessage response = new InvitationResponseMessage(
            invitationId,
            currentPlayer.getPlayerId(),
            accepted
        );
        networkClient.sendMessage(response);
        
        LOGGER.info("Invitation response sent: " + (accepted ? "accepted" : "declined"));
    }
    
    /**
     * Handle move attempt from game interface.
     */
    private void handleMoveAttempt(Move move) {
        if (currentGameSession == null) {
            LOGGER.warning("Move attempted but no active game session");
            return;
        }
        
        MoveMessage moveMessage = new MoveMessage(
            currentPlayer.getPlayerId(),
            move,
            currentGameSession.getSessionId()
        );
        networkClient.sendMessage(moveMessage);
        
        // Play move sound
        audioManager.playSound("go");
        
        LOGGER.info("Move attempted: " + move.getFrom() + " to " + move.getTo());
    }
    
    /**
     * Handle chat message from game interface.
     */
    private void handleChatMessage(String message) {
        if (currentGameSession == null) {
            LOGGER.warning("Chat message sent but no active game session");
            return;
        }
        
        ChatMessage chatMessage = new ChatMessage(
            currentPlayer.getPlayerId(),
            message,
            currentGameSession.getSessionId() // Use session ID as target for game chat
        );
        networkClient.sendMessage(chatMessage);
        
        LOGGER.info("Chat message sent: " + message);
    }
    
    /**
     * Handle resign request from game interface.
     */
    private void handleResignRequest() {
        if (currentGameSession == null) {
            LOGGER.warning("Resign requested but no active game session");
            return;
        }
        
        // Create a special move message indicating resignation
        // Create a dummy position and piece for resignation
        Position dummyPos = new Position(0, 0);
        ChessPiece dummyPiece = new com.xiangqi.shared.model.pieces.General(currentPlayer, dummyPos);
        Move resignMove = new Move(dummyPos, dummyPos, dummyPiece);
        MoveMessage resignMessage = new MoveMessage(
            currentPlayer.getPlayerId(),
            resignMove,
            currentGameSession.getSessionId()
        );
        networkClient.sendMessage(resignMessage);
        
        LOGGER.info("Resign requested");
    }
    
    /**
     * Handle draw offer request from game interface.
     */
    private void handleDrawOfferRequest() {
        if (currentGameSession == null) {
            LOGGER.warning("Draw offer requested but no active game session");
            return;
        }
        
        // This could be implemented as a special message type or game action
        LOGGER.info("Draw offer requested");
    }
    
    /**
     * Handle new game request from game interface.
     */
    private void handleNewGameRequest() {
        // Return to lobby for now
        showLobbyInterface();
        LOGGER.info("New game requested - returning to lobby");
    }
    
    /**
     * Show error dialog to user.
     */
    private void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        });
    }
    
    // NetworkMessageHandler implementation
    
    @Override
    public void handleLoginRequest(LoginMessage message) {
        // This is handled by the server, not the client
    }
    
    @Override
    public void handleMoveMessage(MoveMessage message) {
        if (currentGameSession != null && gameFrame != null) {
            // Update game state based on received move
            // This would typically involve updating the game session and refreshing the UI
            SwingUtilities.invokeLater(() -> {
                gameFrame.updateGameState(currentGameSession.getGameState());
                
                // Play appropriate sound based on move type
                if (message.getMove().getCapturedPiece() != null) {
                    audioManager.playSound("eat");
                } else {
                    audioManager.playSound("go");
                }
            });
        }
    }
    
    @Override
    public void handleChatMessage(ChatMessage message) {
        if (gameFrame != null) {
            SwingUtilities.invokeLater(() -> {
                // Get sender name from authenticated players or use sender ID
                String senderName = message.getSenderId();
                if (currentPlayer != null && currentPlayer.getPlayerId().equals(message.getSenderId())) {
                    senderName = currentPlayer.getUsername();
                }
                gameFrame.appendChatMessage(senderName, message.getContent());
            });
        }
    }
    
    @Override
    public void handleDisconnection(String clientId) {
        SwingUtilities.invokeLater(() -> {
            showErrorDialog("连接断开", "与服务器的连接已断开");
            showLoginInterface();
        });
        
        currentPlayer = null;
        currentGameSession = null;
        pendingInvitations.clear();
    }
    
    @Override
    public void handleGameInvitation(GameInvitationMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (lobbyFrame != null) {
                pendingInvitations.put(message.getInvitationId(), message.getSenderId());
                // Create a temporary player object for the invitation display
                Player fromPlayer = new Player(message.getSenderId(), message.getSenderId());
                lobbyFrame.showGameInvitation(fromPlayer, message.getInvitationId());
            }
        });
    }
    
    @Override
    public void handleLobbyUpdate(LobbyUpdateMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (lobbyFrame != null) {
                lobbyFrame.updatePlayerList(message.getPlayers());
                lobbyFrame.updateGameList(message.getGames());
            }
        });
    }
    
    @Override
    public void handleError(ErrorMessage message) {
        LOGGER.warning("Received error message: " + message.getErrorCode() + " - " + message.getErrorDescription());
        
        SwingUtilities.invokeLater(() -> {
            handleServerError(message);
        });
    }
    
    /**
     * Handles server error messages with appropriate recovery actions.
     */
    private void handleServerError(ErrorMessage message) {
        String errorCode = message.getErrorCode();
        String description = message.getErrorDescription();
        
        switch (errorCode) {
            case "INVALID_MOVE":
                // Game-specific error - show in game chat if game is active
                if (gameFrame != null && gameFrame.isVisible()) {
                    gameFrame.appendChatMessage("服务器", "无效移动: " + description);
                } else {
                    showErrorDialog("移动错误", description);
                }
                break;
                
            case "GAME_STATE_ERROR":
                // Critical game error - may need to restart game
                showErrorDialog("游戏状态错误", description + "\n建议重新开始游戏。");
                if (gameFrame != null) {
                    gameFrame.appendChatMessage("系统", "游戏状态错误，建议重新开始");
                }
                break;
                
            case "CONNECTION_ERROR":
                // Network error - attempt reconnection
                showErrorDialog("连接错误", description);
                attemptReconnection();
                break;
                
            case "AUTHENTICATION_ERROR":
                // Auth error - return to login
                showErrorDialog("认证错误", description);
                showLoginInterface();
                break;
                
            default:
                // Generic error - use legacy handling for compatibility
                String errorMsg = message.getErrorDescription();
                if (loginFrame != null && loginFrame.isVisible()) {
                    loginFrame.onLoginFailure(errorMsg);
                } else if (lobbyFrame != null && lobbyFrame.isVisible()) {
                    lobbyFrame.showError(errorMsg);
                } else {
                    showErrorDialog("错误", errorMsg);
                }
                break;
        }
    }
    
    /**
     * Attempts to reconnect to the server after a connection error.
     */
    private void attemptReconnection() {
        LOGGER.info("Attempting to reconnect to server...");
        
        // Show reconnection dialog
        int result = JOptionPane.showConfirmDialog(
            getCurrentFrame(),
            "连接已断开。是否尝试重新连接？",
            "连接断开",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Attempt reconnection in background thread
            new Thread(() -> {
                try {
                    if (networkClient.connect(serverAddress, serverPort)) {
                        LOGGER.info("Reconnection successful");
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                getCurrentFrame(),
                                "重新连接成功！",
                                "连接恢复",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    } else {
                        LOGGER.warning("Reconnection failed");
                        SwingUtilities.invokeLater(() -> {
                            showErrorDialog("重连失败", "无法重新连接到服务器，请检查网络连接。");
                        });
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Exception during reconnection", e);
                    SwingUtilities.invokeLater(() -> {
                        showErrorDialog("重连异常", "重连过程中发生异常: " + e.getMessage());
                    });
                }
            }).start();
        }
    }
    
    /**
     * Gets the currently visible frame for dialog positioning.
     */
    private JFrame getCurrentFrame() {
        if (gameFrame != null && gameFrame.isVisible()) {
            return gameFrame;
        } else if (lobbyFrame != null && lobbyFrame.isVisible()) {
            return lobbyFrame;
        } else if (loginFrame != null && loginFrame.isVisible()) {
            return loginFrame;
        }
        return null;
    }
    
    /**
     * Handle login response from server.
     */
    public void handleLoginResponse(LoginResponseMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (message.isSuccess()) {
                currentPlayer = message.getPlayer();
                networkClient.setClientId(currentPlayer.getPlayerId());
                loginFrame.onLoginSuccess();
                showLobbyInterface();
                
                // Play login success sound
                audioManager.playSound("seat");
            } else {
                loginFrame.onLoginFailure(message.getErrorMessage());
            }
        });
    }
    
    /**
     * Handle game start message from server.
     */
    public void handleGameStart(GameStartMessage message) {
        SwingUtilities.invokeLater(() -> {
            GameSession gameSession = message.getGameSession();
            showGameInterface(gameSession);
            
            // Play game start sound
            audioManager.playSound("jiang");
        });
    }
    
    /**
     * Handle game end message from server.
     */
    public void handleGameEnd(GameEndMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                gameFrame.showGameEndDialog(message.getGameResult());
            }
        });
    }
    
    /**
     * Shutdown the client gracefully.
     */
    public void shutdown() {
        LOGGER.info("Shutting down GameClient");
        
        if (networkClient != null && networkClient.isConnected()) {
            networkClient.disconnect();
        }
        
        if (audioManager != null) {
            audioManager.cleanup();
        }
        
        SwingUtilities.invokeLater(() -> {
            if (loginFrame != null) {
                loginFrame.dispose();
            }
            if (lobbyFrame != null) {
                lobbyFrame.dispose();
            }
            if (gameFrame != null) {
                gameFrame.dispose();
            }
        });
        
        System.exit(0);
    }
    
    /**
     * Main method for testing the GameClient.
     */
    public static void main(String[] args) {
        GameClient client = new GameClient();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
        
        client.start();
    }
}