# 象棋游戏开发者文档

本文档面向希望了解项目架构、参与开发或扩展功能的开发者。内容涵盖项目结构、核心组件、开发规范、测试策略等方方面面。

## 📑 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [开发环境配置](#开发环境配置)
- [项目结构详解](#项目结构详解)
- [核心组件说明](#核心组件说明)
- [代码规范](#代码规范)
- [测试策略](#测试策略)
- [网络协议](#网络协议)
- [扩展开发](#扩展开发)
- [性能优化](#性能优化)
- [调试技巧](#调试技巧)

---

## 项目概述

### 项目简介

这是一个基于Java Swing的网络象棋游戏项目，采用经典的客户端-服务器（C/S）架构。项目使用Maven进行构建管理，支持多人在线对战和完整的象棋游戏逻辑。

### 设计目标

- **模块化设计**：将共享、客户端、服务器分离为独立模块
- **可扩展性**：易于添加新功能和棋子类型
- **高性能**：支持多人同时在线对战
- **可测试性**：完整的单元测试和集成测试覆盖
- **易维护性**：清晰的代码结构和文档

### 关键特性

- ✅ 完整的中国象棋规则实现
- ✅ 基于Socket的网络通信
- ✅ 多线程支持并发连接
- ✅ 事件驱动的架构设计
- ✅ 资源管理和缓存机制
- ✅ 可配置的服务器和客户端参数

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                               │
│              [客户端1] [客户端2] ... [客户端N]               │
└─────────────┬───────────────────────────────────┬───────────┘
              │                                   │
              │  网络通信 (Socket + 自定义协议)    │
              │                                   │
┌─────────────▼───────────────────────────────────▼───────────┐
│                       服务器层                               │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │ 连接管理器   │  │  游戏会话     │  │  消息路由器     │   │
│  │             │  │  管理器       │  │                 │   │
│  └─────────────┘  └──────────────┘  └─────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 使用共享模块
                              │
┌─────────────────────────────▼─────────────────────────────┐
│                        共享层                              │
│  ┌──────────┐  ┌───────────┐  ┌───────────┐  ┌────────┐ │
│  │ 数据模型  │  │ 游戏引擎   │  │ 网络消息   │  │ 工具类 │ │
│  └──────────┘  └───────────┘  └───────────┘  └────────┘ │
└───────────────────────────────────────────────────────────┘
```

### 模块依赖关系

```
xiangqi-client ────依赖────> xiangqi-shared
                            ▲
xiangqi-server ────依赖────┘
```

### 技术栈详情

| 层次 | 技术 | 说明 |
|------|------|------|
| **编程语言** | Java 21 | 使用最新LTS版本 |
| **构建工具** | Maven 3.x | 多模块项目管理 |
| **UI框架** | Swing | 跨平台GUI框架 |
| **网络通信** | Socket + ObjectStream | 自定义协议 |
| **并发处理** | ExecutorService | 线程池管理 |
| **测试框架** | JUnit 5 | 单元测试 |
| **属性测试** | QuickCheck | 属性驱动测试 |
| **序列化** | Java Serialization | 对象传输 |

---

## 开发环境配置

### 必需工具

| 工具 | 最低版本 | 推荐版本 | 用途 |
|------|----------|----------|------|
| **JDK** | 21 | 21+ | Java开发环境 |
| **Maven** | 3.6 | 3.9+ | 项目构建工具 |
| **IDE** | - | IntelliJ IDEA / Eclipse | 代码编辑 |
| **Git** | 2.x | 最新版 | 版本控制 |

### IDE配置

#### IntelliJ IDEA（推荐）

1. **导入项目**
   ```
   File → Open → 选择项目根目录的pom.xml
   ```

2. **配置JDK**
   ```
   File → Project Structure → Project
   SDK: 选择 JDK 21
   Language Level: 21
   ```

3. **配置Maven**
   ```
   File → Settings → Build, Execution, Deployment → Build Tools → Maven
   Maven home directory: 指向Maven安装目录
   User settings file: 指向settings.xml
   ```

4. **启用注解处理**
   ```
   File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   勾选"Enable annotation processing"
   ```

5. **代码风格**
   ```
   File → Settings → Editor → Code Style → Java
   导入项目提供的代码风格配置文件(如果有)
   ```

#### Eclipse配置

1. **导入Maven项目**
   ```
   File → Import → Maven → Existing Maven Projects
   ```

2. **设置JDK**
   ```
   Window → Preferences → Java → Installed JREs
   添加JDK 21
   ```

3. **Maven设置**
   ```
   Window → Preferences → Maven
   配置Maven installation和User Settings
   ```

### 项目克隆和构建

```bash
# 克隆项目（如果使用Git）
git clone <repository-url>
cd JAVA_ChessProject

# 编译所有模块
mvn clean compile

# 运行测试
mvn test

# 打包项目（生成JAR文件）
mvn package

# 安装到本地Maven仓库
mvn install

# 跳过测试快速构建
mvn clean package -DskipTests
```

### 常用Maven命令

```bash
# 清理构建产物
mvn clean

# 仅编译源代码
mvn compile

# 编译测试代码
mvn test-compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ChessEngineTest

# 运行特定模块的测试
mvn test -pl xiangqi-shared

# 查看依赖树
mvn dependency:tree

# 更新依赖
mvn clean install -U

# 生成项目站点
mvn site
```

---

## 项目结构详解

### 目录结构说明

```
JAVA_ChessProject/
├── pom.xml                     # 父POM，定义全局配置和依赖管理
├── build.bat                   # Windows构建脚本
├── start-server.bat            # 服务器启动脚本
├── start-client.bat            # 客户端启动脚本
├── server.properties           # 服务器配置
├── client.properties           # 客户端配置
├── docs/                       # 项目文档目录
│   ├── *.md                    # 各类文档
│
├── xiangqi-shared/             # 共享模块 - 被客户端和服务器依赖
│   ├── pom.xml                 # 模块POM配置
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/xiangqi/shared/
│   │   │       ├── model/      # 数据模型包
│   │   │       │   ├── Player.java
│   │   │       │   ├── Position.java
│   │   │       │   ├── ChessPiece.java
│   │   │       │   ├── Move.java
│   │   │       │   ├── GameState.java
│   │   │       │   └── GameResult.java
│   │   │       │
│   │   │       ├── network/    # 网络通信包
│   │   │       │   ├── NetworkMessage.java
│   │   │       │   ├── MessageType.java
│   │   │       │   ├── LoginMessage.java
│   │   │       │   ├── MoveMessage.java
│   │   │       │   └── ...
│   │   │       │
│   │   │       └── engine/     # 游戏引擎包
│   │   │           ├── ChessEngine.java
│   │   │           ├── RuleValidator.java
│   │   │           └── GameEventListener.java
│   │   │
│   │   └── test/               # 单元测试
│   │       └── java/com/xiangqi/shared/
│   │
│   └── target/                 # Maven构建输出目录
│
├── xiangqi-server/             # 服务器模块
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/xiangqi/server/
│   │   │       ├── ServerMain.java         # 服务器主入口
│   │   │       └── network/                # 网络服务包
│   │   │           ├── GameServer.java     # 核心服务器类
│   │   │           ├── ClientHandler.java  # 客户端连接处理
│   │   │           └── GameSession.java    # 游戏会话管理
│   │   │
│   │   └── test/               # 单元测试
│   │
│   └── target/                 # 构建输出
│
├── xiangqi-client/             # 客户端模块
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/xiangqi/client/
│   │   │       ├── ClientMain.java         # 客户端主入口
│   │   │       │
│   │   │       ├── ui/                     # 用户界面包
│   │   │       │   ├── LoginFrame.java     # 登录界面
│   │   │       │   ├── LobbyFrame.java     # 游戏大厅
│   │   │       │   ├── GameFrame.java      # 游戏主界面
│   │   │       │   └── ChessBoardPanel.java # 棋盘面板
│   │   │       │
│   │   │       ├── network/                # 网络客户端包
│   │   │       │   └── NetworkClient.java  # 网络通信客户端
│   │   │       │
│   │   │       └── multimedia/             # 多媒体包
│   │   │           ├── AudioManager.java   # 音频管理
│   │   │           └── ResourceManager.java # 资源管理
│   │   │
│   │   └── test/               # 单元测试
│   │
│   └── target/                 # 构建输出
│
└── source/                     # 游戏资源文件
    ├── audio/                  # 音效文件 (.wav)
    │   ├── move.wav
    │   ├── capture.wav
    │   ├── check.wav
    │   └── ...
    │
    ├── face/                   # 玩家头像 (.gif)
    │   └── *.gif
    │
    ├── img/                    # UI图片 (.gif)
    │   └── *.gif
    │
    └── qizi/                   # 棋子图片 (.gif)
        ├── r_jiang.gif         # 红方将
        ├── r_che.gif           # 红方车
        ├── b_jiang.gif         # 黑方将
        └── ...
```

### 包命名规范

- `com.xiangqi.shared.model` - 数据模型类
- `com.xiangqi.shared.network` - 网络消息类
- `com.xiangqi.shared.engine` - 游戏引擎和规则
- `com.xiangqi.server.network` - 服务器网络层
- `com.xiangqi.client.ui` - 客户端界面
- `com.xiangqi.client.network` - 客户端网络层
- `com.xiangqi.client.multimedia` - 客户端多媒体

---

## 核心组件说明

### 共享模块 (xiangqi-shared)

#### 1. 数据模型 (model包)

**Player.java - 玩家类**

```java
public class Player implements Serializable {
    private String id;              // 唯一标识
    private String username;        // 用户名
    private PlayerStatus status;    // 玩家状态
    private int rating;            // 等级分
    private PlayerStatistics stats; // 统计数据
    
    // 构造方法、getter/setter、业务方法
}
```

**Position.java - 位置类**

```java
public class Position implements Serializable {
    private int row;    // 行 (0-9)
    private int col;    // 列 (0-8)
    
    // 验证位置是否合法（10x9棋盘）
    public boolean isValid() {
        return row >= 0 && row < 10 && col >= 0 && col < 9;
    }
}
```

**ChessPiece.java - 棋子抽象类**

```java
public abstract class ChessPiece implements Serializable {
    protected PieceType type;      // 棋子类型
    protected Side side;           // 红方/黑方
    protected Position position;   // 当前位置
    
    // 抽象方法：获取所有合法移动
    public abstract List<Position> getValidMoves(GameState state);
    
    // 抽象方法：验证移动是否合法
    public abstract boolean isValidMove(Position target, GameState state);
}
```

**GameState.java - 游戏状态类**

```java
public class GameState implements Serializable {
    private ChessPiece[][] board;   // 棋盘 [10][9]
    private Side currentPlayer;     // 当前玩家
    private GameStatus status;      // 游戏状态
    private List<Move> moveHistory; // 移动历史
    private Player redPlayer;       // 红方玩家
    private Player blackPlayer;     // 黑方玩家
    
    // 深度拷贝，用于模拟移动
    public GameState deepCopy();
    
    // 执行移动
    public void executeMove(Move move);
    
    // 撤销移动
    public void undoMove();
}
```

#### 2. 网络通信 (network包)

**NetworkMessage.java - 消息基类**

```java
public abstract class NetworkMessage implements Serializable {
    protected MessageType type;     // 消息类型
    protected String senderId;      // 发送者ID
    protected long timestamp;       // 时间戳
    
    public abstract void process(NetworkMessageHandler handler);
}
```

**MessageType枚举 - 消息类型**

```java
public enum MessageType {
    // 认证相关
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    LOGOUT,
    
    // 游戏大厅
    LOBBY_UPDATE,
    PLAYER_LIST,
    GAME_INVITATION,
    INVITATION_RESPONSE,
    
    // 游戏对弈
    MOVE_MESSAGE,
    MOVE_RESPONSE,
    GAME_START,
    GAME_END,
    
    // 其他
    CHAT_MESSAGE,
    HEARTBEAT,
    ERROR_MESSAGE
}
```

#### 3. 游戏引擎 (engine包)

**ChessEngine.java - 游戏引擎**

```java
public class ChessEngine {
    private GameState gameState;
    private RuleValidator ruleValidator;
    private List<GameEventListener> listeners;
    
    // 执行移动
    public boolean makeMove(Move move) {
        if (ruleValidator.isValidMove(move, gameState)) {
            gameState.executeMove(move);
            notifyListeners(GameEvent.MOVE_MADE, move);
            
            // 检查将军、将死
            if (isCheck(gameState.getCurrentPlayer())) {
                if (isCheckmate(gameState.getCurrentPlayer())) {
                    gameState.setStatus(GameStatus.CHECKMATE);
                    notifyListeners(GameEvent.CHECKMATE, null);
                } else {
                    gameState.setStatus(GameStatus.CHECK);
                    notifyListeners(GameEvent.CHECK, null);
                }
            }
            return true;
        }
        return false;
    }
    
    // 检查是否将军
    public boolean isCheck(Side side);
    
    // 检查是否将死
    public boolean isCheckmate(Side side);
}
```

### 服务器模块 (xiangqi-server)

#### GameServer.java - 核心服务器类

```java
public class GameServer implements NetworkMessageHandler {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Map<String, ClientHandler> clients;
    private Map<String, GameSession> gameSessions;
    private Properties config;
    
    // 启动服务器
    public void start(int port) {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(config.getThreadPoolSize());
        
        // 监听客户端连接
        while (running) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket, this);
            threadPool.execute(handler);
        }
    }
    
    // 处理客户端消息
    @Override
    public void handleMessage(NetworkMessage message, ClientHandler client) {
        switch (message.getType()) {
            case LOGIN_REQUEST:
                handleLogin((LoginMessage) message, client);
                break;
            case MOVE_MESSAGE:
                handleMove((MoveMessage) message, client);
                break;
            // ... 其他消息类型
        }
    }
    
    // 广播消息给所有客户端
    public void broadcast(NetworkMessage message);
    
    // 发送消息给特定客户端
    public void sendToClient(String clientId, NetworkMessage message);
}
```

#### ClientHandler.java - 客户端连接处理

```java
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameServer server;
    private Player player;
    private boolean connected;
    
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // 持续接收消息
            while (connected) {
                NetworkMessage message = (NetworkMessage) in.readObject();
                server.handleMessage(message, this);
            }
        } catch (Exception e) {
            handleDisconnect();
        }
    }
    
    // 发送消息
    public void sendMessage(NetworkMessage message) {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }
}
```

### 客户端模块 (xiangqi-client)

#### NetworkClient.java - 网络客户端

```java
public class NetworkClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private MessageListener messageListener;
    private boolean connected;
    
    // 连接服务器
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            // 启动消息接收线程
            new Thread(this::receiveMessages).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    // 发送消息
    public void sendMessage(NetworkMessage message) {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }
    
    // 接收消息线程
    private void receiveMessages() {
        while (connected) {
            NetworkMessage message = (NetworkMessage) in.readObject();
            messageListener.onMessageReceived(message);
        }
    }
}
```

#### UI组件

**LoginFrame, LobbyFrame, GameFrame** 等Swing组件负责用户界面展示和交互。

**ResourceManager** 管理图片资源缓存，**AudioManager** 处理音效播放。

---

## 代码规范

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `GameEngine`, `ChessPiece` |
| 接口 | PascalCase | `GameEventListener`, `NetworkMessageHandler` |
| 方法 | camelCase | `makeMove()`, `isValidMove()` |
| 变量 | camelCase | `gameState`, `currentPlayer` |
| 常量 | UPPER_SNAKE_CASE | `MAX_PLAYERS`, `DEFAULT_PORT` |
| 包名 | 小写+点分隔 | `com.xiangqi.shared.model` |

### 注释规范

```java
/**
 * 象棋引擎类，负责游戏逻辑处理
 * Chess engine class responsible for game logic
 *
 * @author 作者名
 * @version 1.0.0
 * @since 2024-12-24
 */
public class ChessEngine {
    
    /**
     * 执行一步棋
     * Execute a chess move
     *
     * @param move 要执行的移动
     * @return 是否成功执行
     * @throws IllegalArgumentException 如果移动不合法
     */
    public boolean makeMove(Move move) {
        // 实现代码
    }
}
```

### 代码格式

- 使用4个空格缩进（不使用Tab）
- 行长度不超过120字符
- 大括号采用K&R风格（左大括号不换行）
- 方法之间空一行
- 逻辑块之间空一行

---

## 测试策略

### 单元测试示例

```java
@Test
public void testPositionValidation() {
    // 合法位置
    assertTrue(new Position(0, 0).isValid());
    assertTrue(new Position(9, 8).isValid());
    
    // 非法位置
    assertFalse(new Position(-1, 0).isValid());
    assertFalse(new Position(10, 0).isValid());
    assertFalse(new Position(0, 9).isValid());
}
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定模块
mvn test -pl xiangqi-shared

# 生成测试报告
mvn surefire-report:report
```

---

## 网络协议

### 消息格式

所有消息继承自`NetworkMessage`，通过Java序列化传输。

### 通信流程

1. 客户端连接服务器
2. 发送`LOGIN_REQUEST`
3. 服务器验证并返回`LOGIN_RESPONSE`
4. 进入大厅，接收`LOBBY_UPDATE`
5. 游戏中交换`MOVE_MESSAGE`
6. 游戏结束发送`GAME_END`消息

---

## 扩展开发

### 添加新棋子

1. 继承`ChessPiece`类
2. 实现`getValidMoves()`方法
3. 在`PieceFactory`中注册
4. 添加对应图片资源

### 添加新消息类型

1. 在`MessageType`枚举中添加类型
2. 创建新的消息类继承`NetworkMessage`
3. 在服务器和客户端的消息处理器中添加处理逻辑

---

## 性能优化

### 客户端优化
- 预加载和缓存图片资源
- 使用双缓冲技术减少界面闪烁
- 异步处理网络消息

### 服务器优化
- 使用线程池管理并发连接
- 及时清理断开的连接
- 消息批处理和异步处理

---

## 调试技巧

### 启用调试日志

```bash
# 客户端
java -jar xiangqi-client.jar -d

# 服务器
java -jar xiangqi-server.jar -d
```

### 常见问题
- 并发问题：使用`synchronized`或`Lock`
- 内存泄漏：及时关闭资源
- 网络超时：设置合理的超时时间

---

## 版本控制

### Git工作流

- `main` - 稳定版本
- `develop` - 开发版本
- `feature/*` - 新功能
- `hotfix/*` - 紧急修复

### 提交规范

```
type(scope): subject

feat: 新功能
fix: 修复bug
docs: 文档更新
refactor: 重构
test: 测试相关
```

---

## 相关文档

- [README.md](README.md) - 项目概述
- [QUICK_START.md](QUICK_START.md) - 快速启动
- [USER_GUIDE.md](USER_GUIDE.md) - 用户手册
- [CONFIG_README.md](CONFIG_README.md) - 配置说明

---

**祝开发顺利！** 🚀