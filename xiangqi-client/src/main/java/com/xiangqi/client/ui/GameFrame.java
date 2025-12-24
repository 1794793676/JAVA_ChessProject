package com.xiangqi.client.ui;

import com.xiangqi.shared.model.*;
import com.xiangqi.shared.engine.GameEventListener;
import com.xiangqi.client.network.NetworkClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main game window that integrates all game UI components including the chess board,
 * timer display, chat functionality, and game controls.
 */
public class GameFrame extends JFrame implements ChessBoardPanel.BoardEventListener, GameEventListener {
    private static final Logger LOGGER = Logger.getLogger(GameFrame.class.getName());
    private static final int TIMER_INITIAL_SECONDS = 600; // 10 minutes per player
    
    // UI Components
    private ChessBoardPanel boardPanel;
    private JLabel redTimerLabel;
    private JLabel blackTimerLabel;
    private JLabel currentPlayerLabel;
    private JLabel gameStatusLabel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendChatButton;
    private JButton resignButton;
    private JButton drawOfferButton;
    private JButton newGameButton;
    
    // Game state
    private GameState gameState;
    private Player localPlayer;
    private NetworkClient networkClient;
    
    // Timer management
    private int redTimeRemaining = TIMER_INITIAL_SECONDS;
    private int blackTimeRemaining = TIMER_INITIAL_SECONDS;
    private ScheduledExecutorService timerExecutor;
    private boolean timerRunning = false;
    
    // Event listeners
    private GameEventListener gameEventListener;
    
    public interface GameEventListener {
        void onMoveAttempted(Move move);
        void onChatMessageSent(String message);
        void onResignRequested();
        void onDrawOfferRequested();
        void onNewGameRequested();
        void onReturnToLobbyRequested();
    }
    
    public GameFrame(Player localPlayer, NetworkClient networkClient) {
        this.localPlayer = localPlayer;
        this.networkClient = networkClient;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupTimer();
        
        setTitle("象棋游戏 - " + localPlayer.getUsername());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * Initialize all UI components.
     */
    private void initializeComponents() {
        // Chess board
        boardPanel = new ChessBoardPanel();
        boardPanel.setBoardEventListener(this);
        // Note: setLocalPlayer will be called later when game state is set
        
        // Timer labels
        redTimerLabel = new JLabel(formatTime(redTimeRemaining));
        blackTimerLabel = new JLabel(formatTime(blackTimeRemaining));
        redTimerLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        blackTimerLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        redTimerLabel.setForeground(Color.RED);
        blackTimerLabel.setForeground(Color.BLACK);
        
        // Game status labels
        currentPlayerLabel = new JLabel("当前玩家: 等待游戏开始");
        gameStatusLabel = new JLabel("游戏状态: 等待对手");
        currentPlayerLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        gameStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // Chat components
        chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        chatArea.setBackground(Color.WHITE);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        chatInput = new JTextField(25);
        chatInput.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sendChatButton = new JButton("发送");
        
        // Game control buttons
        resignButton = new JButton("认输");
        drawOfferButton = new JButton("求和");
        newGameButton = new JButton("新游戏");
        
        // Initially disable game buttons
        resignButton.setEnabled(false);
        drawOfferButton.setEnabled(false);
        newGameButton.setEnabled(false);
    }
    
    /**
     * Setup the main layout of the game window.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main game area (left side)
        JPanel gamePanel = new JPanel(new BorderLayout());
        
        // Timer panel (top)
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBorder(BorderFactory.createTitledBorder("计时器"));
        
        JPanel redTimerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        redTimerPanel.add(new JLabel("红方: "));
        redTimerPanel.add(redTimerLabel);
        
        JPanel blackTimerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        blackTimerPanel.add(new JLabel("黑方: "));
        blackTimerPanel.add(blackTimerLabel);
        
        timerPanel.add(redTimerPanel, BorderLayout.WEST);
        timerPanel.add(blackTimerPanel, BorderLayout.EAST);
        
        // Status panel
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.add(currentPlayerLabel);
        statusPanel.add(gameStatusLabel);
        
        // Board panel (center)
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.add(boardPanel, BorderLayout.CENTER);
        boardContainer.setBorder(BorderFactory.createLoweredBevelBorder());
        
        gamePanel.add(timerPanel, BorderLayout.NORTH);
        gamePanel.add(boardContainer, BorderLayout.CENTER);
        gamePanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Right side panel (chat and controls)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 600));
        
        // Chat panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("聊天"));
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);
        
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("游戏控制"));
        controlPanel.add(resignButton);
        controlPanel.add(drawOfferButton);
        controlPanel.add(newGameButton);
        
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Add panels to main frame
        add(gamePanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Setup event handlers for all interactive components.
     */
    private void setupEventHandlers() {
        // Chat input handlers
        sendChatButton.addActionListener(e -> sendChatMessage());
        chatInput.addActionListener(e -> sendChatMessage());
        
        // Game control button handlers
        resignButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要认输吗？",
                "确认认输",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION && gameEventListener != null) {
                gameEventListener.onResignRequested();
            }
        });
        
        drawOfferButton.addActionListener(e -> {
            if (gameEventListener != null) {
                gameEventListener.onDrawOfferRequested();
            }
            appendChatMessage("系统", "您提出了和棋请求");
        });
        
        newGameButton.addActionListener(e -> {
            if (gameEventListener != null) {
                gameEventListener.onNewGameRequested();
            }
        });
        
        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }
    
    /**
     * Setup the game timer system.
     */
    private void setupTimer() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Update timer every second
        timerExecutor.scheduleAtFixedRate(() -> {
            if (timerRunning && gameState != null) {
                SwingUtilities.invokeLater(() -> updateTimer());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Update the timer for the current player.
     */
    private void updateTimer() {
        if (gameState == null || !timerRunning) return;
        
        Player currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer == null) return;
        
        boolean isRedPlayer = currentPlayer.equals(gameState.getRedPlayer());
        
        if (isRedPlayer) {
            redTimeRemaining--;
            redTimerLabel.setText(formatTime(redTimeRemaining));
            if (redTimeRemaining <= 0) {
                handleTimeExpired(currentPlayer);
            }
        } else {
            blackTimeRemaining--;
            blackTimerLabel.setText(formatTime(blackTimeRemaining));
            if (blackTimeRemaining <= 0) {
                handleTimeExpired(currentPlayer);
            }
        }
    }
    
    /**
     * Handle time expiration for a player.
     */
    private void handleTimeExpired(Player player) {
        timerRunning = false;
        JOptionPane.showMessageDialog(
            this,
            player.getUsername() + " 超时，游戏结束！",
            "时间到",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Create timeout game result
        Player winner = gameState.getOpponent(player);
        GameResult result = GameResult.timeout(winner, player);
        showGameEndDialog(result);
    }
    
    /**
     * Format time in MM:SS format.
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    /**
     * Send a chat message.
     */
    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            if (gameEventListener != null) {
                gameEventListener.onChatMessageSent(message);
            }
            appendChatMessage(localPlayer.getUsername(), message);
            chatInput.setText("");
        }
    }
    
    /**
     * Append a message to the chat area.
     */
    public void appendChatMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(sender + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    /**
     * Update the game state and refresh all displays.
     */
    public void updateGameState(GameState newState) {
        this.gameState = newState;
        
        SwingUtilities.invokeLater(() -> {
            // Set local player if not already set
            boardPanel.setLocalPlayer(localPlayer, newState);
            
            // Update board display
            boardPanel.updateGameState(newState);
            
            // Update status labels
            if (newState.getCurrentPlayer() != null) {
                currentPlayerLabel.setText("当前玩家: " + newState.getCurrentPlayer().getUsername());
            }
            
            gameStatusLabel.setText("游戏状态: " + getStatusText(newState.getStatus()));
            
            // Update button states
            boolean gameInProgress = newState.getStatus() == GameStatus.IN_PROGRESS || 
                                   newState.getStatus() == GameStatus.CHECK;
            resignButton.setEnabled(gameInProgress);
            drawOfferButton.setEnabled(gameInProgress);
            newGameButton.setEnabled(!gameInProgress);
            
            // Update timer state
            if (gameInProgress) {
                startTimer();
            } else {
                stopTimer();
            }
            
            // Show check notification (but not for checkmate, which is handled by GameEndMessage)
            if (newState.getStatus() == GameStatus.CHECK) {
                showCheckNotification(newState.getCurrentPlayer());
            }
        });
    }
    
    /**
     * Get human-readable status text.
     */
    private String getStatusText(GameStatus status) {
        switch (status) {
            case WAITING_FOR_PLAYERS: return "等待玩家";
            case IN_PROGRESS: return "进行中";
            case CHECK: return "将军";
            case CHECKMATE: return "将死";
            case STALEMATE: return "困毙";
            case DRAW: return "和棋";
            case RESIGNED: return "认输";
            case TIMEOUT: return "超时";
            case ABANDONED: return "弃局";
            default: return status.toString();
        }
    }
    
    /**
     * Start the game timer.
     */
    public void startTimer() {
        timerRunning = true;
    }
    
    /**
     * Stop the game timer.
     */
    public void stopTimer() {
        timerRunning = false;
    }
    
    /**
     * Reset the game timers.
     */
    public void resetTimers() {
        redTimeRemaining = TIMER_INITIAL_SECONDS;
        blackTimeRemaining = TIMER_INITIAL_SECONDS;
        redTimerLabel.setText(formatTime(redTimeRemaining));
        blackTimerLabel.setText(formatTime(blackTimeRemaining));
    }
    
    /**
     * Show game end dialog with result.
     */
    public void showGameEndDialog(GameResult result) {
        SwingUtilities.invokeLater(() -> {
            stopTimer();
            
            String message = buildGameEndMessage(result);
            
            int option = JOptionPane.showOptionDialog(
                this,
                message,
                "游戏结束",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"新游戏", "返回大厅"},
                "返回大厅"
            );
            
            if (option == 0) { // New game
                if (gameEventListener != null) {
                    gameEventListener.onNewGameRequested();
                }
            } else { // Return to lobby (option == 1 or dialog closed)
                // Notify client to return to lobby
                if (gameEventListener != null) {
                    gameEventListener.onReturnToLobbyRequested();
                }
                // Dispose game frame after notifying
                dispose();
            }
        });
    }
    
    /**
     * Build the game end message based on the result.
     */
    private String buildGameEndMessage(GameResult result) {
        StringBuilder message = new StringBuilder();
        
        switch (result.getEndStatus()) {
            case CHECKMATE:
                if (result.getWinner() != null) {
                    if (result.getWinner().equals(localPlayer)) {
                        message.append("🎉 恭喜您获胜！\n\n");
                        message.append("您成功将死了对手 ").append(result.getLoser().getUsername()).append("！");
                    } else {
                        message.append("💔 很遗憾，您输了！\n\n");
                        message.append("您被 ").append(result.getWinner().getUsername()).append(" 将死了！");
                    }
                } else {
                    message.append("将死！游戏结束");
                }
                break;
            case RESIGNED:
                if (result.getWinner() != null) {
                    message.append("获胜者: ").append(result.getWinner().getUsername()).append("\n");
                }
                message.append("对手认输");
                break;
            case TIMEOUT:
                if (result.getWinner() != null) {
                    message.append("获胜者: ").append(result.getWinner().getUsername()).append("\n");
                }
                message.append("超时");
                break;
            case DRAW:
                message.append("和棋");
                break;
            case STALEMATE:
                message.append("困毙 - 无子可动");
                break;
            case ABANDONED:
                message.append("对手断线");
                break;
            default:
                if (result.getWinner() != null) {
                    message.append("获胜者: ").append(result.getWinner().getUsername()).append("\n");
                }
                message.append("游戏结束: ").append(result.getReason());
        }
        
        return message.toString();
    }
    
    /**
     * Handle window closing event.
     */
    private void handleWindowClosing() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出游戏吗？",
            "确认退出",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            if (gameState != null && gameState.getStatus() == GameStatus.IN_PROGRESS) {
                // Resign if game is in progress
                if (gameEventListener != null) {
                    gameEventListener.onResignRequested();
                }
            }
            dispose();
        }
    }
    
    /**
     * Set the game event listener.
     */
    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }
    
    // ChessBoardPanel.BoardEventListener implementation
    
    @Override
    public void onMoveAttempted(Position from, Position to) {
        if (gameState == null) return;
        
        ChessPiece piece = gameState.getPiece(from);
        if (piece == null) return;
        
        // Check if it's the local player's turn
        if (!piece.getOwner().equals(localPlayer)) {
            String positionInfo = String.format("位置 (%d,%d)", from.getRow(), from.getCol());
            appendChatMessage("系统", "不是您的回合！" + positionInfo);
            return;
        }
        
        // Create move object
        Move move = new Move(from, to, piece, gameState.getPiece(to));
        
        // Show move attempt info
        String pieceName = getPieceDisplayName(piece);
        String moveInfo = String.format("%s 从 (%d,%d) 移动到 (%d,%d)", 
            pieceName, from.getRow(), from.getCol(), to.getRow(), to.getCol());
        LOGGER.fine("Move attempt: " + moveInfo);
        
        if (gameEventListener != null) {
            gameEventListener.onMoveAttempted(move);
        }
    }
    
    /**
     * Get display name for chess piece.
     */
    private String getPieceDisplayName(ChessPiece piece) {
        String colorPrefix = piece.isRed() ? "红" : "黑";
        String typeName;
        switch (piece.getType()) {
            case GENERAL: typeName = piece.isRed() ? "帅" : "将"; break;
            case ADVISOR: typeName = "士"; break;
            case ELEPHANT: typeName = piece.isRed() ? "相" : "象"; break;
            case HORSE: typeName = "马"; break;
            case CHARIOT: typeName = "车"; break;
            case CANNON: typeName = "炮"; break;
            case SOLDIER: typeName = piece.isRed() ? "兵" : "卒"; break;
            default: typeName = piece.getType().name();
        }
        return colorPrefix + typeName;
    }
    
    @Override
    public void onPieceSelected(Position position) {
        // Optional: Add sound effects or other feedback for piece selection
    }
    
    // GameEventListener implementation for error handling
    
    @Override
    public void onMoveExecuted(Move move) {
        // Move executed successfully - no action needed here
        LOGGER.fine("Move executed: " + move);
    }
    
    @Override
    public void onGameStateChanged(GameState newState) {
        updateGameState(newState);
    }
    
    @Override
    public void onPlayerJoined(Player player) {
        appendChatMessage("系统", player.getUsername() + " 加入了游戏");
    }
    
    @Override
    public void onPlayerLeft(Player player) {
        appendChatMessage("系统", player.getUsername() + " 离开了游戏");
    }
    
    @Override
    public void onGameEnded(GameResult result) {
        showGameEndDialog(result);
    }
    
    @Override
    public void onInvalidMoveAttempted(Move move, String reason) {
        SwingUtilities.invokeLater(() -> {
            // Build detailed error message with position info
            String positionInfo = String.format("从 (%d,%d) 到 (%d,%d)", 
                move.getFrom().getRow(), move.getFrom().getCol(),
                move.getTo().getRow(), move.getTo().getCol());
            String detailedReason = getUserFriendlyErrorMessage(reason) + " - " + positionInfo;
            
            // Show error message to user
            showInvalidMoveError(detailedReason);
            
            // Log the error
            LOGGER.warning("Invalid move attempted: " + move + " - " + reason);
            
            // Add to chat for user feedback with position
            appendChatMessage("系统", "无效移动: " + detailedReason);
        });
    }
    
    public void onGameStateCorrupted(String reason) {
        SwingUtilities.invokeLater(() -> {
            LOGGER.severe("Game state corrupted: " + reason);
            
            // Show critical error dialog
            JOptionPane.showMessageDialog(
                this,
                "游戏状态出现严重错误: " + reason + "\n游戏将被重置。",
                "游戏错误",
                JOptionPane.ERROR_MESSAGE
            );
            
            // Disable game controls
            resignButton.setEnabled(false);
            drawOfferButton.setEnabled(false);
            newGameButton.setEnabled(true);
            
            // Add to chat
            appendChatMessage("系统", "游戏状态错误: " + reason);
        });
    }
    
    /**
     * Shows a notification when a player is in check.
     */
    private void showCheckNotification(Player playerInCheck) {
        // Only show notification if it's the local player
        if (playerInCheck.equals(localPlayer)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    this,
                    "警告：您的将/帅正在被将军！\n请移动棋子解除将军状态。",
                    "将军提示",
                    JOptionPane.WARNING_MESSAGE
                );
                appendChatMessage("系统", "您正处于被将军状态，必须解除将军！");
            });
        } else {
            appendChatMessage("系统", playerInCheck.getUsername() + " 正处于被将军状态！");
        }
    }
    
    /**
     * Shows an error dialog for invalid moves with detailed feedback.
     */
    private void showInvalidMoveError(String reason) {
        // Create a more user-friendly error message
        String userMessage = getUserFriendlyErrorMessage(reason);
        
        // Show a non-blocking error notification
        JOptionPane.showMessageDialog(
            this,
            userMessage,
            "无效移动",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Converts technical error messages to user-friendly messages.
     */
    private String getUserFriendlyErrorMessage(String technicalReason) {
        if (technicalReason == null) {
            return "移动无效";
        }
        
        String reason = technicalReason.toLowerCase();
        
        if (reason.contains("not your turn")) {
            return "现在不是您的回合";
        } else if (reason.contains("no piece at source")) {
            return "选择的位置没有棋子";
        } else if (reason.contains("cannot move to target")) {
            return "该棋子不能移动到目标位置";
        } else if (reason.contains("leave your general in check")) {
            return "此移动会让您的将军处于被将状态";
        } else if (reason.contains("invalid source position")) {
            return "起始位置无效";
        } else if (reason.contains("invalid target position")) {
            return "目标位置无效";
        } else if (reason.contains("piece mismatch")) {
            return "棋子不匹配";
        } else if (reason.contains("game not initialized")) {
            return "游戏尚未开始";
        } else {
            return "移动违反了游戏规则: " + technicalReason;
        }
    }
    
    @Override
    public void dispose() {
        // Cleanup timer
        if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdown();
        }
        super.dispose();
    }
}