# 设计文档

## 概述

网络象棋游戏系统是一个基于Java Swing的客户端-服务器架构应用程序，提供完整的中国象棋游戏体验。系统采用多层架构设计，包括用户界面层、游戏逻辑层、网络通信层和数据管理层，支持多用户并发游戏和实时交互。

## 架构

### 系统架构图

```
┌─────────────────┐    网络通信    ┌─────────────────┐
│   游戏客户端     │ ←──────────→  │   游戏服务器     │
│                │               │                │
│ ┌─────────────┐ │               │ ┌─────────────┐ │
│ │ 用户界面层   │ │               │ │ 连接管理器   │ │
│ └─────────────┘ │               │ └─────────────┘ │
│ ┌─────────────┐ │               │ ┌─────────────┐ │
│ │ 游戏逻辑层   │ │               │ │ 游戏会话管理 │ │
│ └─────────────┘ │               │ └─────────────┘ │
│ ┌─────────────┐ │               │ ┌─────────────┐ │
│ │ 网络通信层   │ │               │ │ 数据处理层   │ │
│ └─────────────┘ │               │ └─────────────┘ │
└─────────────────┘               └─────────────────┘
```

### 客户端架构

- **用户界面层**: 使用Swing组件实现登录、大厅、游戏界面
- **游戏逻辑层**: 象棋规则引擎、移动验证、游戏状态管理
- **网络通信层**: Socket客户端、消息序列化/反序列化
- **多媒体层**: 音效播放、图形资源管理

### 服务器架构

- **连接管理器**: 处理客户端连接、断线重连
- **游戏会话管理**: 管理多个并发游戏会话
- **消息路由**: 在玩家之间转发游戏消息
- **线程池**: 管理并发连接和请求处理

## 组件和接口

### 核心组件

#### 1. 用户界面组件

**LoginFrame (登录界面)**
```java
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    
    public void showLoginDialog();
    public boolean validateCredentials(String username, String password);
    public void onLoginSuccess();
    public void onLoginFailure(String errorMessage);
}
```

**LobbyFrame (大厅界面)**
```java
public class LobbyFrame extends JFrame {
    private JList<Player> playerList;
    private JList<GameSession> gameList;
    private JButton inviteButton;
    
    public void updatePlayerList(List<Player> players);
    public void updateGameList(List<GameSession> games);
    public void sendGameInvitation(Player target);
    public void handleInvitation(GameInvitation invitation);
}
```

**GameFrame (游戏界面)**
```java
public class GameFrame extends JFrame {
    private ChessBoardPanel boardPanel;
    private JLabel timerLabel;
    private JTextArea chatArea;
    
    public void initializeBoard();
    public void updateBoard(GameState state);
    public void highlightValidMoves(Position position);
    public void showGameResult(GameResult result);
}
```

#### 2. 游戏逻辑组件

**ChessEngine (象棋引擎)**
```java
public class ChessEngine {
    private GameState currentState;
    private RuleValidator ruleValidator;
    
    public boolean isValidMove(Move move);
    public GameState executeMove(Move move);
    public boolean isInCheck(Player player);
    public boolean isCheckmate(Player player);
    public List<Move> getValidMoves(Position position);
}
```

**GameState (游戏状态)**
```java
public class GameState {
    private ChessPiece[][] board;
    private Player currentPlayer;
    private GameStatus status;
    private List<Move> moveHistory;
    
    public ChessPiece getPiece(Position position);
    public void setPiece(Position position, ChessPiece piece);
    public Player getCurrentPlayer();
    public void switchPlayer();
}
```

#### 3. 网络通信组件

**NetworkClient (网络客户端)**
```java
public class NetworkClient {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    public boolean connect(String serverAddress, int port);
    public void sendMessage(NetworkMessage message);
    public NetworkMessage receiveMessage();
    public void disconnect();
}
```

**GameServer (游戏服务器)**
```java
public class GameServer {
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;
    private Map<String, GameSession> gameSessions;
    
    public void startServer(int port);
    public void acceptConnections();
    public void handleClientMessage(String clientId, NetworkMessage message);
    public void broadcastToGame(String gameId, NetworkMessage message);
}
```

### 接口定义

#### GameEventListener (游戏事件监听器)
```java
public interface GameEventListener {
    void onMoveExecuted(Move move);
    void onGameStateChanged(GameState newState);
    void onPlayerJoined(Player player);
    void onPlayerLeft(Player player);
    void onGameEnded(GameResult result);
}
```

#### NetworkMessageHandler (网络消息处理器)
```java
public interface NetworkMessageHandler {
    void handleLoginRequest(LoginMessage message);
    void handleMoveMessage(MoveMessage message);
    void handleChatMessage(ChatMessage message);
    void handleDisconnection(String clientId);
}
```

## 数据模型

### 核心数据结构

#### Player (玩家)
```java
public class Player {
    private String playerId;
    private String username;
    private PlayerStatus status;
    private int rating;
    private GameStatistics statistics;
}
```

#### ChessPiece (棋子)
```java
public abstract class ChessPiece {
    protected PieceType type;
    protected Player owner;
    protected Position position;
    
    public abstract List<Move> getValidMoves(GameState state);
    public abstract boolean canMoveTo(Position target, GameState state);
}
```

#### Move (移动)
```java
public class Move {
    private Position from;
    private Position to;
    private ChessPiece piece;
    private ChessPiece capturedPiece;
    private long timestamp;
}
```

#### NetworkMessage (网络消息)
```java
public abstract class NetworkMessage implements Serializable {
    protected MessageType type;
    protected String senderId;
    protected long timestamp;
}
```

### 数据流

1. **用户登录流程**:
   - 客户端发送LoginMessage
   - 服务器验证凭据
   - 返回LoginResponse
   - 客户端切换到大厅界面

2. **游戏创建流程**:
   - 玩家发送游戏邀请
   - 服务器创建GameSession
   - 通知双方玩家
   - 初始化游戏状态

3. **移动执行流程**:
   - 玩家在界面上移动棋子
   - 客户端验证移动合法性
   - 发送MoveMessage到服务器
   - 服务器验证并广播给对手
   - 更新游戏状态

## 正确性属性

*属性是一个特征或行为，应该在系统的所有有效执行中保持为真——本质上，是关于系统应该做什么的正式声明。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

基于需求分析，以下是系统必须满足的核心正确性属性：

### 属性反思

在编写具体属性之前，我需要识别和消除冗余：

- 属性1（有效凭据认证）和属性2（无效凭据拒绝）可以合并为一个综合的认证属性
- 属性8（移动传输）和属性9（移动接收）可以合并为一个往返通信属性
- 属性12（棋子移动规则）和属性16（特殊规则执行）可以合并为一个综合的规则执行属性

### 核心正确性属性

**属性 1: 认证正确性**
*对于任何*凭据组合，系统的认证响应应该与凭据的有效性相匹配：有效凭据应授予访问权限，无效凭据应被拒绝并显示错误消息
**验证: 需求 1.1, 1.2**

**属性 2: 界面转换一致性**
*对于任何*成功的认证，系统应该从登录界面正确转换到游戏大厅界面
**验证: 需求 1.4**

**属性 3: 游戏邀请处理**
*对于任何*游戏邀请，接收方应该看到包含接受/拒绝选项的邀请显示
**验证: 需求 2.3**

**属性 4: 游戏会话创建**
*对于任何*两个同意开始游戏的玩家，系统应该创建新的游戏会话并将双方转换到象棋界面
**验证: 需求 2.4**

**属性 5: 大厅自动更新**
*对于任何*大厅状态变化（新玩家或游戏），显示应该自动刷新以反映更新
**验证: 需求 2.5**

**属性 6: 有效移动处理**
*对于任何*有效的象棋移动，象棋引擎应该更新游戏状态并将移动传输给对手
**验证: 需求 3.2**

**属性 7: 无效移动拒绝**
*对于任何*无效的象棋移动，象棋引擎应该拒绝移动并保持当前游戏状态不变
**验证: 需求 3.3**

**属性 8: 移动同步往返**
*对于任何*游戏中的移动，当一个玩家执行移动时，对手的棋盘应该立即更新以反映相同的移动
**验证: 需求 3.4**

**属性 9: 游戏结束处理**
*对于任何*通过将军或认输结束的游戏，系统应该宣布获胜者并更新游戏统计
**验证: 需求 3.5**

**属性 10: 象棋规则执行**
*对于任何*棋子和移动组合，象棋引擎应该根据传统象棋规则验证移动的合法性，包括特殊规则约束
**验证: 需求 4.1, 4.5**

**属性 11: 游戏状态检测**
*对于任何*游戏状态，象棋引擎应该准确检测将军、将死和和棋条件
**验证: 需求 4.2**

**属性 12: 棋子捕获处理**
*对于任何*棋子捕获，象棋引擎应该从棋盘上移除被捕获的棋子并更新游戏状态
**验证: 需求 4.3**

**属性 13: 将军状态移动限制**
*对于任何*将军状态，象棋引擎应该只允许能够解除将军条件的移动
**验证: 需求 4.4**

**属性 14: 并发连接处理**
*对于任何*数量的并发客户端连接，游戏服务器应该使用多线程独立处理每个连接
**验证: 需求 5.1, 8.1**

**属性 15: 数据传输可靠性**
*对于任何*游戏数据传输，网络协议应该确保移动和游戏状态更新被可靠传递
**验证: 需求 5.2**

**属性 16: 游戏会话隔离**
*对于任何*多个同时运行的游戏，游戏服务器应该管理独立的游戏会话而不相互干扰
**验证: 需求 5.4, 8.2**

**属性 17: 线程安全操作**
*对于任何*系统资源访问，象棋系统应该使用线程安全操作防止数据损坏
**验证: 需求 8.3**

**属性 18: 音效播放一致性**
*对于任何*棋子移动或游戏事件，系统应该播放相应的音效
**验证: 需求 6.1, 6.2**

**属性 19: 计时器管理**
*对于任何*玩家回合开始，系统应该启动该玩家的计时器并暂停对手的计时器
**验证: 需求 6.4**

**属性 20: 图形资源使用**
*对于任何*棋盘渲染，系统应该使用提供的图形资源文件显示棋子和棋盘
**验证: 需求 7.3**

**属性 21: UI交互反馈**
*对于任何*界面元素交互，系统应该提供即时的视觉反馈
**验证: 需求 7.4**

## 错误处理

### 网络错误处理策略

1. **连接超时**: 实现连接重试机制，最多尝试3次
2. **数据传输失败**: 使用消息确认机制确保数据完整性
3. **意外断线**: 保存游戏状态，允许重新连接恢复游戏
4. **服务器过载**: 实现连接队列和负载均衡

### 游戏逻辑错误处理

1. **无效移动**: 显示错误提示，不改变游戏状态
2. **非法游戏状态**: 回滚到上一个有效状态
3. **时间超限**: 根据游戏规则执行相应惩罚
4. **数据不一致**: 以服务器状态为准，同步客户端

### 用户界面错误处理

1. **资源加载失败**: 使用默认资源或显示错误信息
2. **界面响应超时**: 显示加载指示器
3. **输入验证失败**: 高亮错误字段并显示提示信息

## 测试策略

### 双重测试方法

系统将采用单元测试和基于属性的测试相结合的方法：

- **单元测试**验证特定示例、边缘情况和错误条件
- **基于属性的测试**验证应该在所有输入中保持的通用属性
- 两者结合提供全面覆盖：单元测试捕获具体错误，属性测试验证一般正确性

### 单元测试要求

单元测试将覆盖：
- 特定的象棋移动规则示例
- 网络消息序列化/反序列化
- 用户界面组件交互
- 边缘情况处理（空输入、边界值等）

### 基于属性的测试要求

- 使用**QuickCheck for Java (junit-quickcheck)**作为属性测试库
- 每个属性测试配置为运行最少100次迭代
- 每个属性测试必须用注释明确引用设计文档中的正确性属性
- 使用格式：**Feature: networked-xiangqi-game, Property {number}: {property_text}**
- 每个正确性属性必须由单个属性测试实现

### 测试数据生成

- **象棋状态生成器**: 创建有效的游戏状态用于测试
- **移动生成器**: 生成合法和非法的移动组合
- **网络消息生成器**: 创建各种类型的网络消息
- **用户输入生成器**: 模拟各种用户交互场景

### 集成测试

- 客户端-服务器通信测试
- 多玩家游戏会话测试
- 并发连接压力测试
- 网络故障恢复测试