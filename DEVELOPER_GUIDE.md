# 象棋游戏开发者文档
# Xiangqi Game Developer Documentation

## 项目概述 / Project Overview

这是一个基于Java Swing的网络象棋游戏项目，采用客户端-服务器架构。项目使用Maven进行构建管理，支持多人在线对战和完整的象棋游戏逻辑。

This is a networked Chinese Chess (Xiangqi) game project built with Java Swing using a client-server architecture. The project uses Maven for build management and supports multiplayer online battles with complete chess game logic.

## 项目结构 / Project Structure

```
xiangqi-game/
├── pom.xml                     # 根POM文件
├── xiangqi-client/             # 客户端模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/client/
│       └── test/java/com/xiangqi/client/
├── xiangqi-server/             # 服务器模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/server/
│       └── test/java/com/xiangqi/server/
├── xiangqi-shared/             # 共享模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/shared/
│       └── test/java/com/xiangqi/shared/
└── source/                     # 资源文件
    ├── audio/                  # 音频文件
    ├── face/                   # 头像图片
    ├── img/                    # 界面图片
    └── qizi/                   # 棋子图片
```

## 架构设计 / Architecture Design

### 模块划分 / Module Division

1. **xiangqi-shared**: 共享组件
   - 游戏数据模型
   - 网络消息定义
   - 象棋引擎和规则
   - 通用工具类

2. **xiangqi-server**: 服务器端
   - 网络连接管理
   - 游戏会话管理
   - 消息路由和处理
   - 多线程并发控制

3. **xiangqi-client**: 客户端
   - 用户界面组件
   - 网络通信客户端
   - 多媒体资源管理
   - 用户交互处理

### 核心组件 / Core Components

#### 1. 游戏引擎 (Game Engine)

**ChessEngine**: 核心象棋引擎
- 移动验证和执行
- 游戏状态管理
- 将军/将死检测
- 规则执行

**RuleValidator**: 规则验证器
- 各种棋子移动规则
- 特殊规则处理
- 移动合法性检查

#### 2. 网络通信 (Network Communication)

**NetworkMessage**: 网络消息基类
- 消息序列化/反序列化
- 消息类型定义
- 消息路由机制

**GameServer**: 游戏服务器
- 客户端连接管理
- 多线程处理
- 游戏会话管理

**NetworkClient**: 网络客户端
- 服务器连接
- 消息发送/接收
- 断线重连

#### 3. 用户界面 (User Interface)

**LoginFrame**: 登录界面
- 用户认证
- 服务器连接
- 错误处理

**LobbyFrame**: 游戏大厅
- 玩家列表
- 游戏邀请
- 实时更新

**GameFrame**: 游戏界面
- 棋盘显示
- 移动交互
- 游戏控制

## 开发环境设置 / Development Environment Setup

### 必需工具 / Required Tools

- **JDK 8+**: Java开发工具包
- **Maven 3.6+**: 构建工具
- **IDE**: IntelliJ IDEA 或 Eclipse
- **Git**: 版本控制

### 构建项目 / Building the Project

```bash
# 克隆项目
git clone <repository-url>
cd xiangqi-game

# 编译所有模块
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn package

# 安装到本地仓库
mvn install
```

### 运行项目 / Running the Project

#### 启动服务器 / Start Server
```bash
cd xiangqi-server
mvn exec:java -Dexec.mainClass="com.xiangqi.server.ServerMain"
# 或者
java -jar target/xiangqi-server-1.0.jar
```

#### 启动客户端 / Start Client
```bash
cd xiangqi-client
mvn exec:java -Dexec.mainClass="com.xiangqi.client.ClientMain"
# 或者
java -jar target/xiangqi-client-1.0.jar
```

## 代码规范 / Coding Standards

### Java编码规范 / Java Coding Standards

1. **命名规范**:
   - 类名：PascalCase (如 `GameEngine`)
   - 方法名：camelCase (如 `validateMove`)
   - 常量：UPPER_SNAKE_CASE (如 `MAX_CONNECTIONS`)
   - 包名：小写，用点分隔 (如 `com.xiangqi.shared.model`)

2. **注释规范**:
   - 所有公共类和方法必须有Javadoc注释
   - 复杂逻辑需要行内注释说明
   - 中英文注释并存，便于国际化

3. **代码格式**:
   - 使用4个空格缩进
   - 行长度不超过120字符
   - 大括号采用K&R风格

### 设计模式 / Design Patterns

项目中使用的主要设计模式：

1. **单例模式**: ResourceManager, AudioManager
2. **工厂模式**: PieceFactory, MessageFactory
3. **观察者模式**: GameEventListener
4. **策略模式**: 各种棋子的移动策略
5. **命令模式**: 网络消息处理

## 测试策略 / Testing Strategy

### 测试框架 / Testing Frameworks

- **JUnit 5**: 单元测试框架
- **junit-quickcheck**: 基于属性的测试
- **Mockito**: 模拟对象框架

### 测试类型 / Test Types

1. **单元测试**: 测试单个类或方法
2. **集成测试**: 测试组件间交互
3. **属性测试**: 验证通用属性和不变量
4. **网络测试**: 测试客户端-服务器通信

### 运行测试 / Running Tests

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl xiangqi-shared

# 运行特定测试类
mvn test -Dtest=ChessEngineTest

# 生成测试报告
mvn surefire-report:report
```

## 网络协议 / Network Protocol

### 消息格式 / Message Format

所有网络消息都继承自 `NetworkMessage` 基类：

```java
public abstract class NetworkMessage implements Serializable {
    protected MessageType type;
    protected String senderId;
    protected long timestamp;
}
```

### 消息类型 / Message Types

- `LOGIN_REQUEST` / `LOGIN_RESPONSE`: 登录相关
- `GAME_INVITATION` / `INVITATION_RESPONSE`: 游戏邀请
- `MOVE_MESSAGE` / `MOVE_RESPONSE`: 移动消息
- `CHAT_MESSAGE`: 聊天消息
- `GAME_END_MESSAGE`: 游戏结束
- `HEARTBEAT_MESSAGE`: 心跳消息

### 通信流程 / Communication Flow

1. 客户端连接服务器
2. 发送登录请求
3. 服务器验证并响应
4. 进入游戏大厅，接收更新
5. 游戏过程中交换移动消息
6. 游戏结束，更新统计信息

## 性能优化 / Performance Optimization

### 客户端优化 / Client Optimization

1. **资源缓存**: 预加载图片和音频资源
2. **UI优化**: 使用双缓冲减少闪烁
3. **网络优化**: 消息批处理和压缩
4. **内存管理**: 及时释放不用的资源

### 服务器优化 / Server Optimization

1. **线程池**: 使用线程池处理并发连接
2. **连接管理**: 及时清理断开的连接
3. **消息队列**: 异步处理消息
4. **负载均衡**: 分散游戏会话负载

## 扩展开发 / Extension Development

### 添加新棋子类型 / Adding New Piece Types

1. 继承 `ChessPiece` 抽象类
2. 实现 `getValidMoves()` 方法
3. 在 `PieceFactory` 中注册
4. 添加对应的图形资源

### 添加新消息类型 / Adding New Message Types

1. 继承 `NetworkMessage` 基类
2. 在 `MessageType` 枚举中添加类型
3. 在消息处理器中添加处理逻辑
4. 更新客户端和服务器代码

### 添加新界面组件 / Adding New UI Components

1. 继承适当的Swing组件
2. 实现必要的事件监听器
3. 集成到主界面框架中
4. 添加相应的测试用例

## 调试和故障排除 / Debugging and Troubleshooting

### 日志配置 / Logging Configuration

项目使用Java标准日志框架：

```java
private static final Logger logger = Logger.getLogger(ClassName.class.getName());
```

### 调试技巧 / Debugging Tips

1. **网络调试**: 启用网络消息日志
2. **游戏状态**: 输出游戏状态快照
3. **性能分析**: 使用JProfiler或类似工具
4. **内存分析**: 监控内存使用情况

### 常见问题 / Common Issues

1. **并发问题**: 使用同步机制保护共享资源
2. **内存泄漏**: 及时关闭流和连接
3. **网络超时**: 设置合适的超时时间
4. **UI冻结**: 避免在EDT中执行长时间操作

## 部署指南 / Deployment Guide

### 打包发布 / Packaging for Release

```bash
# 创建可执行JAR
mvn clean package

# 创建包含依赖的JAR
mvn assembly:assembly

# 生成发布包
mvn clean package assembly:single
```

### 部署配置 / Deployment Configuration

1. **服务器部署**:
   - 配置防火墙开放端口
   - 设置适当的JVM参数
   - 配置日志轮转

2. **客户端分发**:
   - 创建安装程序
   - 包含必要的资源文件
   - 提供配置文件模板

## 版本控制 / Version Control

### Git工作流 / Git Workflow

1. **主分支**: `main` - 稳定版本
2. **开发分支**: `develop` - 开发版本
3. **功能分支**: `feature/*` - 新功能开发
4. **修复分支**: `hotfix/*` - 紧急修复

### 提交规范 / Commit Convention

```
type(scope): description

feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试相关
chore: 构建工具等
```

## 贡献指南 / Contributing Guidelines

1. Fork项目到个人仓库
2. 创建功能分支
3. 编写代码和测试
4. 提交Pull Request
5. 代码审查和合并

---

更多详细信息请参考项目Wiki或联系开发团队。
For more detailed information, please refer to the project Wiki or contact the development team.