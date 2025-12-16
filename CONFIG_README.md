# 配置文件说明
# Configuration Files Documentation

## 概述 / Overview

本项目包含多个配置文件，用于自定义服务器和客户端的行为。所有配置文件都使用Java Properties格式。

This project includes multiple configuration files to customize server and client behavior. All configuration files use Java Properties format.

## 配置文件列表 / Configuration Files List

### 1. server.properties - 服务器配置

服务器端配置文件，包含以下主要设置：

**基本设置 / Basic Settings:**
- `server.port`: 服务器监听端口 (默认: 8888)
- `server.maxConnections`: 最大同时连接数 (默认: 100)
- `server.threadPoolSize`: 线程池大小 (默认: 20)

**调试和监控 / Debug and Monitoring:**
- `server.debug`: 启用调试模式 (默认: false)
- `server.monitoring`: 启用监控服务 (默认: true)

**游戏设置 / Game Settings:**
- `game.maxGamesPerUser`: 每用户最大游戏数 (默认: 3)
- `game.defaultTimeLimit`: 默认时间限制，秒 (默认: 1800)
- `game.heartbeatInterval`: 心跳间隔，秒 (默认: 30)

**网络设置 / Network Settings:**
- `network.connectionTimeout`: 连接超时，毫秒 (默认: 30000)
- `network.readTimeout`: 读取超时，毫秒 (默认: 10000)
- `network.maxMessageSize`: 最大消息大小，字节 (默认: 8192)

### 2. client.properties - 客户端配置

客户端配置文件，包含以下主要设置：

**服务器连接 / Server Connection:**
- `client.serverHost`: 服务器主机地址 (默认: localhost)
- `client.serverPort`: 服务器端口 (默认: 8888)
- `client.connectionTimeout`: 连接超时，毫秒 (默认: 10000)
- `client.reconnectAttempts`: 重连尝试次数 (默认: 3)

**界面设置 / UI Settings:**
- `ui.theme`: 界面主题 (默认: system)
- `ui.language`: 界面语言 (默认: zh_CN)
- `ui.windowWidth`: 窗口宽度 (默认: 800)
- `ui.windowHeight`: 窗口高度 (默认: 600)

**音效设置 / Audio Settings:**
- `audio.enabled`: 启用音效 (默认: true)
- `audio.volume`: 音量 (默认: 0.8)
- `audio.enableMoveSound`: 移动音效 (默认: true)
- `audio.enableCaptureSound`: 吃子音效 (默认: true)

## 使用方法 / Usage

### 服务器配置 / Server Configuration

1. 复制 `server.properties` 到服务器运行目录
2. 根据需要修改配置项
3. 启动服务器时指定配置文件：
   ```bash
   java -jar xiangqi-server.jar -c server.properties
   ```

### 客户端配置 / Client Configuration

1. 复制 `client.properties` 到客户端运行目录
2. 根据需要修改配置项
3. 客户端会自动加载同目录下的配置文件

## 配置示例 / Configuration Examples

### 高性能服务器配置 / High Performance Server

```properties
server.port=8888
server.maxConnections=500
server.threadPoolSize=50
server.monitoring=true
performance.enableStatistics=true
```

### 开发环境配置 / Development Environment

```properties
server.debug=true
logging.level=DEBUG
server.maxConnections=10
network.connectionTimeout=5000
```

### 生产环境配置 / Production Environment

```properties
server.port=8888
server.maxConnections=1000
server.threadPoolSize=100
logging.level=INFO
logging.file=xiangqi-server.log
security.enableAuthentication=true
```

## 配置验证 / Configuration Validation

启动时，应用程序会验证配置文件：

1. **格式验证**: 检查Properties文件格式
2. **值验证**: 检查数值范围和类型
3. **依赖验证**: 检查配置项之间的依赖关系
4. **默认值**: 无效或缺失的配置项使用默认值

## 故障排除 / Troubleshooting

### 常见配置问题 / Common Configuration Issues

1. **端口冲突**:
   - 检查端口是否被其他程序占用
   - 尝试使用不同的端口号

2. **连接超时**:
   - 增加 `connectionTimeout` 值
   - 检查网络连接质量

3. **内存不足**:
   - 减少 `maxConnections` 值
   - 增加JVM堆内存大小

4. **配置文件未找到**:
   - 确认配置文件路径正确
   - 检查文件权限

### 日志配置 / Logging Configuration

启用详细日志以诊断配置问题：

```properties
logging.level=DEBUG
server.debug=true
debug.showNetworkMessages=true
```

## 性能调优 / Performance Tuning

### 服务器性能优化 / Server Performance Optimization

```properties
# 增加连接数和线程池
server.maxConnections=1000
server.threadPoolSize=100

# 优化网络设置
network.connectionTimeout=15000
network.readTimeout=5000

# 启用性能统计
performance.enableStatistics=true
performance.statisticsInterval=60
```

### 客户端性能优化 / Client Performance Optimization

```properties
# 禁用动画以提高性能
ui.enableAnimations=false

# 减少资源缓存
resources.cacheSize=20

# 优化网络设置
client.connectionTimeout=5000
client.reconnectDelay=2000
```

---

更多配置选项和详细说明请参考开发者文档。
For more configuration options and detailed explanations, please refer to the developer documentation.