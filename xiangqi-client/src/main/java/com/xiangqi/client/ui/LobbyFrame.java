package com.xiangqi.client.ui;

import com.xiangqi.shared.model.GameSession;
import com.xiangqi.shared.model.Player;
import com.xiangqi.shared.model.PlayerStatus;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 游戏大厅界面 - 显示玩家列表和游戏列表，处理游戏邀请
 * 实现需求 2.1, 2.2, 2.3, 2.4, 2.5
 */
public class LobbyFrame extends JFrame {
    private DefaultListModel<Player> playerListModel;
    private DefaultListModel<GameSession> gameListModel;
    private JList<Player> playerList;
    private JList<GameSession> gameList;
    private JButton inviteButton;
    private JButton refreshButton;
    private JButton logoutButton;
    private JLabel statusLabel;
    private JLabel currentPlayerLabel;
    
    private Timer refreshTimer;
    private Player currentPlayer;
    
    // 事件监听器接口
    public interface LobbyListener {
        void onGameInvitation(Player targetPlayer);
        void onRefreshRequest();
        void onLogout();
        void onJoinGame(GameSession gameSession);
    }
    
    private LobbyListener lobbyListener;
    
    public LobbyFrame() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupAutoRefresh();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("象棋游戏 - 大厅");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        // 列表模型
        playerListModel = new DefaultListModel<>();
        gameListModel = new DefaultListModel<>();
        
        // 列表组件
        playerList = new JList<>(playerListModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setCellRenderer(new PlayerListCellRenderer());
        
        gameList = new JList<>(gameListModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setCellRenderer(new GameListCellRenderer());
        
        // 按钮
        inviteButton = new JButton("邀请游戏");
        refreshButton = new JButton("刷新");
        logoutButton = new JButton("退出登录");
        
        // 标签
        statusLabel = new JLabel("欢迎来到象棋大厅");
        statusLabel.setForeground(Color.BLUE);
        currentPlayerLabel = new JLabel("当前玩家: 未登录");
        currentPlayerLabel.setFont(new Font("宋体", Font.BOLD, 12));
        
        // 初始状态
        inviteButton.setEnabled(false);
    }
    
    /**
     * 设置界面布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部面板 - 当前玩家信息
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topPanel.add(currentPlayerLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // 中央面板 - 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        
        // 左侧 - 玩家列表
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(new TitledBorder("在线玩家"));
        
        JScrollPane playerScrollPane = new JScrollPane(playerList);
        playerScrollPane.setPreferredSize(new Dimension(350, 400));
        playerPanel.add(playerScrollPane, BorderLayout.CENTER);
        
        JPanel playerButtonPanel = new JPanel(new FlowLayout());
        playerButtonPanel.add(inviteButton);
        playerButtonPanel.add(refreshButton);
        playerPanel.add(playerButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(playerPanel);
        
        // 右侧 - 游戏列表
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBorder(new TitledBorder("进行中的游戏"));
        
        JScrollPane gameScrollPane = new JScrollPane(gameList);
        gameScrollPane.setPreferredSize(new Dimension(350, 400));
        gamePanel.add(gameScrollPane, BorderLayout.CENTER);
        
        JPanel gameButtonPanel = new JPanel(new FlowLayout());
        JButton watchButton = new JButton("观战");
        watchButton.setEnabled(false); // 暂时禁用观战功能
        gameButtonPanel.add(watchButton);
        gamePanel.add(gameButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(gamePanel);
        add(splitPane, BorderLayout.CENTER);
        
        // 底部面板 - 状态信息
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 邀请按钮
        inviteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player selectedPlayer = playerList.getSelectedValue();
                if (selectedPlayer != null && lobbyListener != null) {
                    // 检查玩家是否正在游戏中
                    if (selectedPlayer.getStatus() == PlayerStatus.IN_GAME) {
                        JOptionPane.showMessageDialog(
                            LobbyFrame.this,
                            "该玩家正在进行游戏，无法邀请！",
                            "无法邀请",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    lobbyListener.onGameInvitation(selectedPlayer);
                }
            }
        });
        
        // 刷新按钮
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lobbyListener != null) {
                    lobbyListener.onRefreshRequest();
                }
            }
        });
        
        // 退出登录按钮
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lobbyListener != null) {
                    lobbyListener.onLogout();
                }
            }
        });
        
        // 玩家列表选择事件
        playerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Player selectedPlayer = playerList.getSelectedValue();
                // 只有在线且不在游戏中的玩家才能被邀请
                boolean canInvite = selectedPlayer != null && 
                                  !selectedPlayer.equals(currentPlayer) &&
                                  (selectedPlayer.getStatus() == PlayerStatus.ONLINE ||
                                   selectedPlayer.getStatus() == PlayerStatus.IN_LOBBY) &&
                                  selectedPlayer.getStatus() != PlayerStatus.IN_GAME;
                inviteButton.setEnabled(canInvite);
            }
        });
        
        // 游戏列表双击事件
        gameList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    GameSession selectedGame = gameList.getSelectedValue();
                    if (selectedGame != null && lobbyListener != null) {
                        lobbyListener.onJoinGame(selectedGame);
                    }
                }
            }
        });
    }
    
    /**
     * 设置自动刷新机制
     */
    private void setupAutoRefresh() {
        refreshTimer = new Timer(5000, new ActionListener() { // 每5秒刷新一次
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lobbyListener != null) {
                    lobbyListener.onRefreshRequest();
                }
            }
        });
    }
    
    /**
     * 设置大厅监听器
     */
    public void setLobbyListener(LobbyListener listener) {
        this.lobbyListener = listener;
    }
    
    /**
     * 设置当前玩家
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
        if (player != null) {
            currentPlayerLabel.setText("当前玩家: " + player.getUsername() + " (等级: " + player.getRating() + ")");
        } else {
            currentPlayerLabel.setText("当前玩家: 未登录");
        }
    }
    
    /**
     * 更新玩家列表
     */
    public void updatePlayerList(List<Player> players) {
        SwingUtilities.invokeLater(() -> {
            playerListModel.clear();
            if (players != null) {
                for (Player player : players) {
                    // 不显示当前玩家自己
                    if (currentPlayer == null || !player.equals(currentPlayer)) {
                        playerListModel.addElement(player);
                    }
                }
            }
            statusLabel.setText("在线玩家: " + playerListModel.getSize() + " 人");
        });
    }
    
    /**
     * 根据玩家ID查找玩家
     */
    public Player findPlayerById(String playerId) {
        if (playerId == null) {
            return null;
        }
        
        // Check if it's the current player
        if (currentPlayer != null && playerId.equals(currentPlayer.getPlayerId())) {
            return currentPlayer;
        }
        
        // Search in the player list
        for (int i = 0; i < playerListModel.getSize(); i++) {
            Player player = playerListModel.getElementAt(i);
            if (playerId.equals(player.getPlayerId())) {
                return player;
            }
        }
        
        return null;
    }
    
    /**
     * 更新游戏列表
     */
    public void updateGameList(List<GameSession> games) {
        SwingUtilities.invokeLater(() -> {
            gameListModel.clear();
            if (games != null) {
                for (GameSession game : games) {
                    gameListModel.addElement(game);
                }
            }
        });
    }
    
    /**
     * 显示游戏邀请对话框
     */
    public void showGameInvitation(Player fromPlayer, String invitationId) {
        SwingUtilities.invokeLater(() -> {
            String message = fromPlayer.getUsername() + " 邀请您进行象棋对战，是否接受？";
            int result = JOptionPane.showConfirmDialog(
                this,
                message,
                "游戏邀请",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            // 这里需要通过监听器通知结果
            if (lobbyListener instanceof ExtendedLobbyListener) {
                ((ExtendedLobbyListener) lobbyListener).onInvitationResponse(invitationId, result == JOptionPane.YES_OPTION);
            }
        });
    }
    
    /**
     * 显示状态消息
     */
    public void showStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    /**
     * 显示错误消息
     */
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * 启动自动刷新
     */
    public void startAutoRefresh() {
        if (refreshTimer != null && !refreshTimer.isRunning()) {
            refreshTimer.start();
        }
    }
    
    /**
     * 停止自动刷新
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }
    
    /**
     * 显示大厅界面
     */
    public void showLobby() {
        setVisible(true);
        startAutoRefresh();
        if (lobbyListener != null) {
            lobbyListener.onRefreshRequest(); // 立即刷新一次
        }
    }
    
    /**
     * 隐藏大厅界面
     */
    public void hideLobby() {
        stopAutoRefresh();
        setVisible(false);
    }
    
    /**
     * 扩展的监听器接口，包含邀请响应
     */
    public interface ExtendedLobbyListener extends LobbyListener {
        void onInvitationResponse(String invitationId, boolean accepted);
    }
    
    /**
     * 玩家列表单元格渲染器
     */
    private static class PlayerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Player) {
                Player player = (Player) value;
                String statusText = "";
                
                // 根据玩家状态添加状态标识
                if (player.getStatus() == PlayerStatus.IN_GAME) {
                    statusText = " [游戏中]";
                } else if (player.getStatus() == PlayerStatus.ONLINE || player.getStatus() == PlayerStatus.IN_LOBBY) {
                    statusText = " [在线]";
                }
                
                String displayText = player.getUsername() + " (等级: " + player.getRating() + ")" + statusText;
                setText(displayText);
                
                // 根据玩家状态设置颜色
                if (player.getStatus() == PlayerStatus.ONLINE || player.getStatus() == PlayerStatus.IN_LOBBY) {
                    setForeground(isSelected ? Color.WHITE : Color.BLACK);
                } else if (player.getStatus() == PlayerStatus.IN_GAME) {
                    setForeground(isSelected ? Color.LIGHT_GRAY : Color.GRAY);
                } else {
                    setForeground(isSelected ? Color.LIGHT_GRAY : Color.LIGHT_GRAY);
                }
            }
            
            return this;
        }
    }
    
    /**
     * 游戏列表单元格渲染器
     */
    private static class GameListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof GameSession) {
                GameSession game = (GameSession) value;
                String displayText = game.getRedPlayer().getUsername() + " vs " + 
                                   game.getBlackPlayer().getUsername() + 
                                   " (" + game.getGameState().getStatus() + ")";
                setText(displayText);
            }
            
            return this;
        }
    }
}