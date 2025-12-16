# 象棋游戏用户使用说明
# Xiangqi Game User Guide

## 概述 / Overview

这是一个基于Java Swing的网络象棋游戏，支持多人在线对战。游戏采用客户端-服务器架构，提供完整的中国象棋游戏体验。

This is a networked Chinese Chess (Xiangqi) game built with Java Swing, supporting multiplayer online battles. The game uses a client-server architecture and provides a complete Chinese Chess gaming experience.

## 系统要求 / System Requirements

- Java 8 或更高版本 / Java 8 or higher
- 至少 512MB 内存 / At least 512MB RAM
- 网络连接 / Network connection
- 支持的操作系统：Windows, macOS, Linux / Supported OS: Windows, macOS, Linux

## 安装和启动 / Installation and Startup

### 服务器端 / Server Side

1. 确保Java环境已安装 / Ensure Java is installed
2. 下载服务器JAR文件 / Download server JAR file
3. 启动服务器 / Start server:
   ```bash
   java -jar xiangqi-server.jar
   ```

#### 服务器命令行选项 / Server Command Line Options

```bash
java -jar xiangqi-server.jar [选项]

选项 / Options:
  -p, --port <端口>           服务器端口号 (默认: 8888)
  -c, --config <文件>         配置文件路径 (默认: server.properties)
  -m, --max-connections <数量> 最大连接数 (默认: 100)
  -t, --threads <数量>        线程池大小 (默认: 20)
  -d, --debug                启用调试模式
  --no-monitoring            禁用监控服务
  --help                     显示帮助信息
```

#### 服务器配置 / Server Configuration

服务器配置文件 `server.properties` 包含以下设置：

- `server.port`: 服务器监听端口
- `server.maxConnections`: 最大同时连接数
- `server.threadPoolSize`: 处理线程池大小
- `server.debug`: 是否启用调试模式
- `server.monitoring`: 是否启用监控服务

### 客户端 / Client Side

1. 确保Java环境已安装 / Ensure Java is installed
2. 下载客户端JAR文件 / Download client JAR file
3. 启动客户端 / Start client:
   ```bash
   java -jar xiangqi-client.jar
   ```

#### 客户端命令行选项 / Client Command Line Options

```bash
java -jar xiangqi-client.jar [选项]

选项 / Options:
  -h, --host <主机>     服务器主机地址 (默认: localhost)
  -p, --port <端口>     服务器端口号 (默认: 8888)
  -r, --resources <路径> 资源文件路径 (默认: source)
  -d, --debug          启用调试模式
  --help               显示帮助信息
```

## 游戏操作指南 / Game Operation Guide

### 1. 登录 / Login

- 启动客户端后，输入用户名和密码
- 点击"登录"按钮连接到服务器
- 成功登录后进入游戏大厅

### 2. 游戏大厅 / Game Lobby

在游戏大厅中，您可以：
- 查看在线玩家列表
- 查看正在进行的游戏
- 邀请其他玩家开始游戏
- 接受或拒绝游戏邀请

### 3. 游戏对战 / Game Battle

#### 基本操作 / Basic Operations

- **移动棋子**: 点击棋子选中，再点击目标位置
- **拖拽移动**: 拖拽棋子到目标位置
- **取消选择**: 点击空白区域或已选中的棋子

#### 游戏界面元素 / Game Interface Elements

- **棋盘**: 显示当前游戏状态
- **计时器**: 显示双方剩余时间
- **聊天区**: 与对手交流
- **移动历史**: 查看历史移动记录
- **游戏状态**: 显示当前轮到谁下棋

#### 象棋规则 / Xiangqi Rules

游戏严格按照中国象棋规则执行：

1. **将/帅**: 只能在九宫内移动，每次一格
2. **士**: 只能在九宫内斜向移动
3. **象/相**: 斜向移动两格，不能过河
4. **马**: 走"日"字，可能被蹩脚
5. **车**: 直线移动，距离不限
6. **炮**: 直线移动，吃子需要跳过一个棋子
7. **兵/卒**: 向前一格，过河后可左右移动

### 4. 游戏结束 / Game End

游戏可能的结束方式：
- **将军**: 对方将/帅被攻击
- **将死**: 对方无法解除将军状态
- **认输**: 主动认输
- **超时**: 时间用完

## 音效和多媒体 / Audio and Multimedia

游戏包含以下音效：
- 移动棋子音效
- 吃子音效
- 将军提示音
- 游戏结束音效

可以在设置中调整音效音量或关闭音效。

## 故障排除 / Troubleshooting

### 常见问题 / Common Issues

1. **无法连接服务器**
   - 检查服务器是否正在运行
   - 确认服务器地址和端口正确
   - 检查网络连接和防火墙设置

2. **游戏卡顿或延迟**
   - 检查网络连接质量
   - 关闭其他占用网络的程序
   - 尝试重新连接

3. **音效无法播放**
   - 检查音频文件是否存在
   - 确认系统音频设备正常
   - 检查游戏音效设置

4. **界面显示异常**
   - 确认Java版本兼容性
   - 尝试不同的系统外观主题
   - 检查屏幕分辨率设置

### 日志文件 / Log Files

- 客户端日志：查看控制台输出
- 服务器日志：`xiangqi-server.log`（如果配置了文件日志）

## 技术支持 / Technical Support

如果遇到问题，请：
1. 查看日志文件获取错误信息
2. 确认系统要求是否满足
3. 尝试重启应用程序
4. 检查网络连接状态

## 版本信息 / Version Information

- 当前版本：1.0.0
- 支持的Java版本：8+
- 最后更新：2024年12月

---

享受您的象棋游戏体验！/ Enjoy your Xiangqi gaming experience!