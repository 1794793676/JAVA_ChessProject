# 象棋游戏配置文件说明

本文档详细介绍项目的所有配置文件及其参数说明，帮助您根据需要自定义服务器和客户端的行为。

## 📋 目录

- [配置文件概览](#配置文件概览)
- [服务器配置](#服务器配置)
- [客户端配置](#客户端配置)
- [配置示例](#配置示例)
- [高级配置](#高级配置)
- [故障排除](#故障排除)

---

## 配置文件概览

项目包含两个主要配置文件，均使用Java Properties格式（`key=value`）：

| 文件名 | 位置 | 用途 |
|--------|------|------|
| `server.properties` | 项目根目录 | 服务器端配置 |
| `client.properties` | 项目根目录 | 客户端配置 |

### Properties文件格式说明

```properties
# 这是注释行，以#开头
# 基本格式：键=值
server.port=8888

# 可以使用多行（用反斜杠续行）
server.description=这是一个网络象棋\
游戏服务器

# 空白行会被忽略

# 布尔值使用 true/false
server.debug=false

# 数字直接写数值
server.maxConnections=100
```

---

## 服务器配置

### 配置文件：server.properties

#### 基本设置

```properties
# 服务器监听端口
# 默认：8888
# 范围：1024-65535（建议使用1024以上端口）
server.port=8888

# 最大同时连接数
# 默认：100
# 说明：超过此数量的连接将被拒绝
server.maxConnections=100

# 线程池大小
# 默认：20
# 说明：用于处理客户端请求的工作线程数量
# 建议：设置为CPU核心数的2-4倍
server.threadPoolSize=20
```

#### 调试和监控设置

```properties
# 启用调试模式
# 默认：false
# 说明：启用后会输出详细的调试日志
server.debug=false

# 启用监控服务
# 默认：true
# 说明：监控服务器状态、性能指标等
server.monitoring=true
```

#### 游戏设置

```properties
# 每个用户最多参与的游戏数
# 默认：3
# 说明：防止单个用户占用过多资源
game.maxGamesPerUser=3

# 默认时间限制（秒）
# 默认：1800（30分钟）
# 说明：每局游戏的默认时长限制
game.defaultTimeLimit=1800

# 心跳间隔（秒）
# 默认：30
# 说明：服务器检查客户端连接的间隔时间
game.heartbeatInterval=30
```

#### 网络设置

```properties
# 连接超时（毫秒）
# 默认：30000（30秒）
# 说明：客户端连接建立的超时时间
network.connectionTimeout=30000

# 读取超时（毫秒）
# 默认：10000（10秒）
# 说明：等待客户端数据的超时时间
network.readTimeout=10000

# 最大消息大小（字节）
# 默认：8192（8KB）
# 说明：单个网络消息的最大尺寸
network.maxMessageSize=8192
```

#### 日志设置

```properties
# 日志级别
# 可选值：SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
# 默认：INFO
logging.level=INFO

# 日志文件名
# 默认：xiangqi-server.log
# 说明：日志文件保存路径（相对或绝对路径）
logging.file=xiangqi-server.log

# 单个日志文件最大大小
# 默认：10MB
# 说明：超过此大小会自动轮转到新文件
logging.maxFileSize=10MB

# 保留日志文件数量
# 默认：5
# 说明：最多保留多少个历史日志文件
logging.maxFiles=5
```

#### 安全设置

```properties
# 启用身份验证
# 默认：true
# 说明：是否需要用户名验证
security.enableAuthentication=true

# 最大登录尝试次数
# 默认：3
# 说明：超过次数后锁定IP一段时间
security.maxLoginAttempts=3

# 会话超时（秒）
# 默认：3600（1小时）
# 说明：用户会话的有效期
security.sessionTimeout=3600
```

#### 性能设置

```properties
# 启用性能统计
# 默认：true
# 说明：收集和记录性能指标
performance.enableStatistics=true

# 统计信息收集间隔（秒）
# 默认：300（5分钟）
performance.statisticsInterval=300

# GC间隔（秒）
# 默认：600（10分钟）
# 说明：建议进行垃圾回收的间隔（仅建议，不强制）
performance.gcInterval=600
```

---

## 客户端配置

### 配置文件：client.properties

#### 服务器连接设置

```properties
# 服务器地址
# 默认：localhost
# 说明：可以是IP地址或域名
client.serverHost=localhost

# 服务器端口
# 默认：8888
# 说明：必须与服务器端口一致
client.serverPort=8888

# 连接超时（毫秒）
# 默认：10000（10秒）
client.connectionTimeout=10000

# 重连尝试次数
# 默认：3
# 说明：连接失败时自动重试的次数
client.reconnectAttempts=3

# 重连延迟（毫秒）
# 默认：5000（5秒）
# 说明：每次重连尝试之间的等待时间
client.reconnectDelay=5000
```

#### 界面设置

```properties
# 界面主题
# 可选值：system, metal, nimbus, windows
# 默认：system（使用系统默认主题）
ui.theme=system

# 界面语言
# 可选值：zh_CN（简体中文）, en_US（英文）
# 默认：zh_CN
ui.language=zh_CN

# 窗口宽度（像素）
# 默认：800
# 建议最小值：800
ui.windowWidth=800

# 窗口高度（像素）
# 默认：600
# 建议最小值：600
ui.windowHeight=600

# 启用动画效果
# 默认：true
# 说明：关闭可提升性能
ui.enableAnimations=true

# 动画速度
# 可选值：slow, normal, fast
# 默认：normal
ui.animationSpeed=normal
```

#### 音效设置

```properties
# 启用音效
# 默认：true
audio.enabled=true

# 音量（0.0-1.0）
# 默认：0.8
# 说明：0.0表示静音，1.0表示最大音量
audio.volume=0.8

# 移动棋子音效
# 默认：true
audio.enableMoveSound=true

# 吃子音效
# 默认：true
audio.enableCaptureSound=true

# 将军提示音
# 默认：true
audio.enableCheckSound=true

# 游戏结束音效
# 默认：true
audio.enableGameEndSound=true
```

#### 游戏设置

```properties
# 显示合法移动提示
# 默认：true
# 说明：选中棋子后高亮显示可移动位置
game.showValidMoves=true

# 启用移动历史
# 默认：true
# 说明：在界面上显示走棋记录
game.enableMoveHistory=true

# 自动保存
# 默认：true
# 说明：游戏进行中自动保存棋局
game.autoSave=true

# 移动确认
# 默认：false
# 说明：移动前弹出确认对话框
game.confirmMoves=false

# 显示坐标
# 默认：true
# 说明：在棋盘上显示坐标标记
game.showCoordinates=true
```

#### 资源设置

```properties
# 资源基础路径
# 默认：source
# 说明：游戏资源文件（图片、音频）的根目录
resources.basePath=source

# 图片格式
# 默认：gif
resources.imageFormat=gif

# 音频格式
# 默认：wav
resources.audioFormat=wav

# 资源缓存大小
# 默认：50
# 说明：缓存的资源文件数量
resources.cacheSize=50
```

#### 调试设置

```properties
# 启用调试模式
# 默认：false
debug.enabled=false

# 日志级别
# 可选值：SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
# 默认：INFO
debug.logLevel=INFO

# 显示网络消息
# 默认：false
# 说明：在控制台输出所有网络消息
debug.showNetworkMessages=false

# 显示游戏事件
# 默认：false
# 说明：在控制台输出游戏内部事件
debug.showGameEvents=false
```

---

## 配置示例

### 示例1：高性能服务器配置

适用于：高并发环境、多核服务器

```properties
# server.properties - 高性能配置
server.port=8888
server.maxConnections=500
server.threadPoolSize=50

network.connectionTimeout=15000
network.readTimeout=5000

performance.enableStatistics=true
performance.statisticsInterval=60

logging.level=WARNING
```

**说明：**
- 增加最大连接数和线程池以支持更多玩家
- 减少读取超时提高响应速度
- 降低日志级别减少IO开销

### 示例2：开发测试环境

适用于：本地开发、功能测试

```properties
# server.properties - 开发环境
server.port=8888
server.debug=true
server.maxConnections=10

logging.level=ALL
logging.file=dev-server.log

network.connectionTimeout=5000
game.heartbeatInterval=60

security.enableAuthentication=false
```

**说明：**
- 启用调试模式和详细日志
- 减少连接数节省资源
- 关闭身份验证方便测试

### 示例3：生产环境配置

适用于：正式上线、公网部署

```properties
# server.properties - 生产环境
server.port=8888
server.maxConnections=1000
server.threadPoolSize=100

logging.level=INFO
logging.file=/var/log/xiangqi/server.log
logging.maxFileSize=50MB
logging.maxFiles=10

security.enableAuthentication=true
security.maxLoginAttempts=5
security.sessionTimeout=7200

performance.enableStatistics=true
performance.gcInterval=300
```

**说明：**
- 高并发支持
- 完整的日志记录和轮转
- 增强的安全设置
- 性能监控和优化

### 示例4：低配置客户端

适用于：性能较弱的电脑

```properties
# client.properties - 低配置优化
client.serverHost=localhost
client.serverPort=8888

ui.windowWidth=800
ui.windowHeight=600
ui.enableAnimations=false
ui.theme=system

audio.enabled=false

game.showValidMoves=true
game.enableMoveHistory=false

resources.cacheSize=20

debug.enabled=false
```

**说明：**
- 关闭动画和音效节省资源
- 减少界面功能
- 降低资源缓存大小

### 示例5：远程服务器连接

适用于：连接互联网上的服务器

```properties
# client.properties - 远程连接
client.serverHost=123.456.789.100
client.serverPort=8888
client.connectionTimeout=15000
client.reconnectAttempts=5
client.reconnectDelay=3000

ui.language=zh_CN
ui.windowWidth=1024
ui.windowHeight=768

audio.enabled=true
audio.volume=0.6

game.showValidMoves=true
game.confirmMoves=true
```

**说明：**
- 使用服务器公网IP
- 增加连接超时和重试次数
- 启用移动确认避免误操作

---

## 高级配置

### 自定义配置文件路径

#### 服务器

```bash
# 使用绝对路径
java -jar xiangqi-server.jar -c C:\config\server.properties

# 使用相对路径
java -jar xiangqi-server.jar -c ./config/server.properties
```

#### 客户端

客户端默认从以下位置按顺序查找配置文件：
1. 命令行指定的路径
2. 当前工作目录的 `client.properties`
3. JAR文件同目录的 `client.properties`
4. 使用内置默认值

### 环境变量覆盖

可以通过环境变量覆盖配置文件中的设置：

```bash
# Windows
set SERVER_PORT=9999
java -jar xiangqi-server.jar

# Linux/Mac
export SERVER_PORT=9999
java -jar xiangqi-server.jar
```

### JVM参数调优

#### 服务器JVM参数

```bash
# 基本配置
java -Xms512m -Xmx2g -jar xiangqi-server.jar

# 高级配置
java -Xms1g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/xiangqi/ \
     -jar xiangqi-server.jar
```

**参数说明：**
- `-Xms`: 初始堆内存
- `-Xmx`: 最大堆内存
- `-XX:+UseG1GC`: 使用G1垃圾回收器
- `-XX:MaxGCPauseMillis`: GC最大暂停时间
- `-XX:+HeapDumpOnOutOfMemoryError`: OOM时导出堆转储

#### 客户端JVM参数

```bash
# 标准配置
java -Xms256m -Xmx512m -jar xiangqi-client.jar

# 优化图形性能
java -Xms256m -Xmx512m \
     -Dsun.java2d.opengl=true \
     -Dsun.java2d.d3d=false \
     -jar xiangqi-client.jar
```

### 配置文件加密

对于敏感配置（如数据库密码），可以使用加密存储：

```properties
# 使用加密的密码
database.password=ENC(加密后的密码)
```

项目中需要实现相应的解密逻辑。

---

## 故障排除

### 配置文件未生效

**问题：** 修改配置后没有变化

**排查步骤：**
1. 确认配置文件路径正确
2. 检查配置文件格式是否正确（没有多余空格、特殊字符）
3. 确认已重启服务器或客户端
4. 查看日志确认配置是否被加载

### 端口冲突

**问题：** 服务器启动失败，提示端口已被占用

**解决方案：**
```bash
# Windows - 查看端口占用
netstat -ano | findstr :8888

# 修改配置使用其他端口
server.port=8889
```

### 配置值类型错误

**问题：** 启动时报错：NumberFormatException 或 类似错误

**原因：** 配置值类型不匹配

**示例：**
```properties
# 错误：端口应该是数字
server.port=eight-eight-eight-eight

# 正确
server.port=8888

# 错误：布尔值应该是true/false
audio.enabled=yes

# 正确
audio.enabled=true
```

### 中文乱码

**问题：** 配置文件中的中文显示乱码

**解决方案：**
1. 确保配置文件使用UTF-8编码保存
2. 或者使用Unicode转义序列：
   ```properties
   # 直接使用中文（文件需UTF-8编码）
   server.description=象棋服务器
   
   # 使用Unicode转义（不依赖编码）
   server.description=\u8C61\u68CB\u670D\u52A1\u5668
   ```

### 配置验证

创建一个简单的验证脚本来检查配置：

```java
// 配置验证示例
public class ConfigValidator {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.load(new FileInputStream("server.properties"));
        
        // 验证端口范围
        int port = Integer.parseInt(props.getProperty("server.port"));
        if (port < 1024 || port > 65535) {
            System.err.println("端口号无效：" + port);
        }
        
        // 验证必需配置
        if (!props.containsKey("server.maxConnections")) {
            System.err.println("缺少配置：server.maxConnections");
        }
        
        System.out.println("配置验证通过！");
    }
}
```

---

## 配置参数速查表

### 服务器配置速查

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `server.port` | 整数 | 8888 | 监听端口 |
| `server.maxConnections` | 整数 | 100 | 最大连接数 |
| `server.threadPoolSize` | 整数 | 20 | 线程池大小 |
| `server.debug` | 布尔 | false | 调试模式 |
| `game.heartbeatInterval` | 整数 | 30 | 心跳间隔（秒） |
| `logging.level` | 字符串 | INFO | 日志级别 |

### 客户端配置速查

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `client.serverHost` | 字符串 | localhost | 服务器地址 |
| `client.serverPort` | 整数 | 8888 | 服务器端口 |
| `ui.windowWidth` | 整数 | 800 | 窗口宽度 |
| `ui.windowHeight` | 整数 | 600 | 窗口高度 |
| `audio.enabled` | 布尔 | true | 启用音效 |
| `audio.volume` | 小数 | 0.8 | 音量 (0.0-1.0) |

---

## 相关文档

- [README.md](README.md) - 项目概述
- [QUICK_START.md](QUICK_START.md) - 快速启动指南
- [USER_GUIDE.md](USER_GUIDE.md) - 用户使用手册
- [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) - 开发者文档

---

**配置完成后，祝您使用愉快！** 🎉