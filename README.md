# 中国象棋游戏

一个基于Java Swing的网络中国象棋游戏应用，采用客户端-服务器架构，支持实时对弈和完整的象棋逻辑。

## 项目简介

这是一个功能完整的多人在线象棋游戏系统，使用Java 21开发，采用经典的C/S架构设计。玩家可以通过客户端连接到服务器，与其他在线玩家进行实时对弈。（学校课设罢了）

### 主要特性

- ✅ **完整的象棋规则**：实现了所有中国象棋规则，包括将军、将死、困毙等判定
- 🌐 **网络对战**：支持多人同时在线，实时对弈
- 🎨 **精美界面**：使用Swing构建的友好用户界面
- 🔊 **音效支持**：移动、吃子、将军等操作都有音效反馈
- 💾 **游戏记录**：自动记录棋局历史，支持复盘
- 🔧 **灵活配置**：通过配置文件自定义服务器和客户端行为

## 项目结构

这是一个多模块Maven项目，结构如下：

```
JAVA_ChessProject/
├── pom.xml                     # 根POM配置文件
├── build.bat                   # Windows构建脚本
├── start-server.bat            # 服务器启动脚本
├── start-client.bat            # 客户端启动脚本
├── server.properties           # 服务器配置文件
├── client.properties           # 客户端配置文件
├── README.md                   # 项目说明文档
├── QUICK_START.md              # 快速启动指南
├── USER_GUIDE.md               # 用户使用手册
├── DEVELOPER_GUIDE.md          # 开发者文档
├── CONFIG_README.md            # 配置说明文档
│
├── xiangqi-shared/             # 共享模块（数据模型、网络协议）
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/shared/
│       │   ├── model/          # 核心数据模型
│       │   ├── network/        # 网络消息类
│       │   └── engine/         # 游戏引擎接口
│       └── test/java/          # 单元测试
│
├── xiangqi-client/             # 客户端模块（Swing界面、网络通信）
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/client/
│       │   ├── ui/             # 用户界面
│       │   ├── network/        # 网络客户端
│       │   └── multimedia/     # 多媒体资源
│       └── test/java/          # 单元测试
│
├── xiangqi-server/             # 服务器模块（游戏服务、连接管理）
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/server/
│       │   └── network/        # 网络服务器
│       └── test/java/          # 单元测试
│
└── source/                     # 游戏资源文件
    ├── audio/                  # 音效文件（.wav）
    ├── face/                   # 玩家头像（.gif）
    ├── img/                    # 界面图片（.gif）
    └── qizi/                   # 棋子图片（.gif）
```

## 核心组件

### 共享模块 (xiangqi-shared)

#### 数据模型
- **Player（玩家）**：表示游戏玩家，包含ID、用户名、状态、等级分和统计数据
- **Position（位置）**：表示棋盘坐标，带有验证功能（10x9的象棋棋盘）
- **ChessPiece（棋子）**：所有棋子的抽象基类
- **Move（移动）**：表示一次走棋，包含起点、终点、棋子和吃子信息
- **GameState（游戏状态）**：完整的游戏状态，包括棋盘、当前玩家、状态和走棋历史
- **GameResult（游戏结果）**：表示已完成游戏的结果

#### 枚举类型
- **PlayerStatus（玩家状态）**：离线、在线、在大厅、游戏中、离开
- **PieceType（棋子类型）**：将、士、象、马、车、炮、兵
- **GameStatus（游戏状态）**：等待玩家、进行中、将军、将死等
- **MessageType（消息类型）**：客户端-服务器通信的网络消息类型

#### 网络组件
- **NetworkMessage（网络消息）**：所有网络消息的抽象基类
- **NetworkMessageHandler（消息处理器）**：处理不同消息类型的接口
- **消息类**：LoginMessage、MoveMessage、ChatMessage、GameInvitationMessage等

#### 引擎接口
- **GameEventListener（游戏事件监听器）**：监听游戏事件（移动、状态变化等）

### 客户端模块 (xiangqi-client)

#### 用户界面
- **LoginFrame（登录界面）**：处理用户登录和服务器连接
- **LobbyFrame（游戏大厅）**：显示在线玩家和游戏列表
- **GameFrame（游戏界面）**：主要的对弈界面
- **ChessBoardPanel（棋盘面板）**：渲染棋盘和棋子

#### 多媒体
- **AudioManager（音频管理器）**：管理游戏音效
- **ResourceManager（资源管理器）**：加载和缓存图片资源

### 服务器模块 (xiangqi-server)

#### 网络服务
- **GameServer（游戏服务器）**：核心服务器类，处理客户端连接
- **ServerMain（服务器主类）**：服务器启动入口

### 测试框架
- **JUnit 5**：用于单元测试
- **QuickCheck for Java**：用于基于属性的测试
- **完整测试覆盖**：为核心功能创建了测试用例

## 技术栈

- **编程语言**：Java 21
- **构建工具**：Maven 3.x
- **GUI框架**：Java Swing
- **测试框架**：JUnit 5、QuickCheck for Java
- **架构模式**：客户端-服务器（C/S）
- **网络通信**：基于Socket的自定义协议

## 快速开始

### 环境要求

- JDK 21 或更高版本
- Maven 3.6 或更高版本
- Windows操作系统（提供了.bat脚本）

### 构建项目

```bash
# 双击运行或在命令行执行
build.bat
```

### 启动服务器

```bash
# 双击运行或在命令行执行
start-server.bat
```

### 启动客户端

```bash
# 双击运行或在命令行执行
start-client.bat
```

详细的启动说明请参考 [快速启动指南](QUICK_START.md)。

## 文档导航

- 📖 [快速启动指南](QUICK_START.md) - 快速构建和启动项目
- 📘 [用户使用手册](USER_GUIDE.md) - 详细的游戏使用说明
- 👨‍💻 [开发者文档](DEVELOPER_GUIDE.md) - 代码结构和开发指南
- ⚙️ [配置说明文档](CONFIG_README.md) - 配置文件详细说明

## Maybe Continue

- 服务器客户端分布部署
- 观战功能
- 游戏内聊天优化
- 快捷键
- ...

## 开发注意事项

- 所有类都实现了Serializable接口以支持网络传输
- 全面的空值检查和参数验证
- Position类验证象棋棋盘尺寸（10行×9列）
- 游戏状态支持深度拷贝以进行移动验证
- 消息系统具有良好的可扩展性
- 
- 叫AI干

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，请通过Issue反馈。(应该不会回复了)