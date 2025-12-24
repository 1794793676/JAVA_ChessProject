# 象棋游戏快速启动指南

本指南帮助您快速构建和启动网络象棋游戏。整个过程只需要3步，几分钟即可完成！

## 🚀 三步快速启动

### 第一步：构建项目

双击运行项目根目录下的 `build.bat` 文件，或在命令行中执行：

```bash
build.bat
```

**构建过程说明：**

脚本会自动完成以下任务：
1. 清理之前的构建文件
2. 编译所有源代码
3. 运行单元测试
4. 打包生成JAR文件
5. 安装到本地Maven仓库

**预计耗时：** 首次构建约2-3分钟（需要下载依赖），后续构建约30秒

**成功标志：** 看到 `BUILD SUCCESS` 提示信息

### 第二步：启动服务器

双击运行 `start-server.bat` 文件，或在命令行中执行：

```bash
start-server.bat
```

**服务器默认配置：**
- 监听端口：`8888`
- 最大连接数：`100`
- 线程池大小：`20`
- 连接超时：`30000ms`

**成功标志：** 控制台显示 "服务器启动成功，监听端口: 8888"

> 💡 **提示：** 保持服务器窗口打开，不要关闭

### 第三步：启动客户端

双击运行 `start-client.bat` 文件，或在命令行中执行：

```bash
start-client.bat
```

**客户端默认配置：**
- 服务器地址：`localhost`
- 服务器端口：`8888`
- 连接超时：`10000ms`
- 资源路径：`source`

**成功标志：** 弹出登录窗口界面

> 💡 **提示：** 可以同时启动多个客户端进行测试

---

## 📋 系统要求

在开始之前，请确保您的系统满足以下要求：

| 项目 | 要求 |
|------|------|
| **操作系统** | Windows 7 或更高版本 |
| **Java版本** | JDK 21 或更高版本 |
| **Maven版本** | Maven 3.6 或更高版本 |
| **内存** | 至少 512MB 可用内存 |
| **磁盘空间** | 至少 200MB 可用空间 |
| **网络** | 需要网络连接（用于Maven下载依赖） |

### 检查Java版本

打开命令行窗口，输入以下命令：

```bash
java -version
```

您应该看到类似这样的输出：
```
java version "21.0.1" 2023-10-17 LTS
Java(TM) SE Runtime Environment (build 21.0.1+12-LTS-29)
```

### 检查Maven版本

在命令行窗口中输入：

```bash
mvn -version
```

您应该看到类似这样的输出：
```
Apache Maven 3.9.5 (57804ffe001d7215b5e7bcb531cf83df38f93546)
Maven home: C:\apache-maven-3.9.5
Java version: 21.0.1
```

---

## 🎮 开始使用

### 登录游戏

1. 启动客户端后，会看到登录界面
2. 输入您的用户名（无需密码）
3. 点击"登录"按钮
4. 等待连接到服务器

### 创建或加入游戏

登录成功后进入游戏大厅，您可以：

1. **查看在线玩家** - 右侧列表显示所有在线玩家
2. **发起游戏邀请** - 选择一个玩家，点击"邀请对战"
3. **接受游戏邀请** - 当收到邀请时，点击"接受"开始游戏
4. **查看游戏房间** - 左侧列表显示正在进行的游戏

### 开始对弈

进入游戏界面后：

1. **选择棋子** - 点击要移动的棋子（会高亮显示）
2. **移动棋子** - 点击目标位置完成移动
3. **取消选择** - 点击空白区域或再次点击已选棋子
4. **查看历史** - 右侧面板显示走棋历史
5. **聊天交流** - 底部输入框可以发送消息

---

## 🛠️ 高级操作

### 自定义服务器配置

编辑项目根目录下的 `server.properties` 文件：

```properties
# 修改服务器端口
server.port=9999

# 增加最大连接数
server.maxConnections=200

# 调整线程池大小
server.threadPoolSize=50
```

修改后重新启动服务器即可生效。

### 自定义客户端配置

编辑项目根目录下的 `client.properties` 文件：

```properties
# 连接到远程服务器
client.serverHost=192.168.1.100
client.serverPort=8888

# 关闭音效
audio.enabled=false

# 调整窗口大小
ui.windowWidth=1024
ui.windowHeight=768
```

修改后重新启动客户端即可生效。

### Maven开发命令

如果您是开发者，可以使用以下Maven命令：

```bash
# 仅编译（不运行测试）
mvn compile

# 仅运行测试
mvn test

# 清理构建产物
mvn clean

# 完整构建（推荐）
mvn clean install

# 跳过测试快速打包
mvn package -DskipTests
```

---

## 📁 生成的文件位置

构建成功后，可执行JAR文件位于：

- **服务器JAR**: `xiangqi-server\target\xiangqi-server-1.0.0.jar`
- **客户端JAR**: `xiangqi-client\target\xiangqi-client-1.0.0.jar`
- **共享库JAR**: `xiangqi-shared\target\xiangqi-shared-1.0.0.jar`

---

## ⚠️ 常见问题与解决方案

### 问题1：Maven 未找到

**错误提示：**
```
'mvn' 不是内部或外部命令，也不是可运行的程序
```

**解决方案：**
1. 从 [Maven官网](https://maven.apache.org/download.cgi) 下载Maven
2. 解压到目录（如 `C:\Program Files\apache-maven-3.9.5`）
3. 设置环境变量 `MAVEN_HOME` 指向Maven目录
4. 将 `%MAVEN_HOME%\bin` 添加到系统 `PATH` 环境变量
5. 重新打开命令行窗口验证

### 问题2：Java 未找到或版本过低

**错误提示：**
```
Error: JAVA_HOME is not defined correctly
或
Unsupported class file major version 65
```

**解决方案：**
1. 从 [Oracle官网](https://www.oracle.com/java/technologies/downloads/) 下载JDK 21
2. 安装JDK到指定目录
3. 设置环境变量 `JAVA_HOME` 指向JDK安装目录
4. 将 `%JAVA_HOME%\bin` 添加到系统 `PATH` 环境变量
5. 重新打开命令行窗口，运行 `java -version` 验证

### 问题3：编译失败

**错误提示：**
```
[ERROR] Failed to execute goal ... compilation failure
```

**解决方案：**
1. 确保使用Java 21或更高版本
2. 清理项目：`mvn clean`
3. 删除 `.m2` 目录下的缓存（可选）
4. 重新运行：`build.bat`
5. 检查网络连接，确保能下载Maven依赖

### 问题4：无法连接服务器

**错误提示：**
客户端显示"连接服务器失败"或超时

**解决方案：**
1. **确认服务器已启动** - 检查服务器窗口是否显示"服务器启动成功"
2. **检查端口占用** - 确保8888端口没有被其他程序占用
   ```bash
   netstat -ano | findstr 8888
   ```
3. **检查防火墙** - 临时关闭防火墙测试，或添加程序例外
4. **验证配置** - 确认 `client.properties` 中的地址和端口正确
5. **尝试重启** - 先关闭客户端和服务器，然后重新启动

### 问题5：界面显示异常或乱码

**问题描述：**
界面显示错位、乱码或图片无法加载

**解决方案：**
1. **检查资源文件** - 确保 `source` 目录存在且包含所有资源
2. **检查文件编码** - 确保配置文件使用UTF-8编码
3. **调整系统DPI** - 如果界面显示过大或过小，调整Windows显示缩放
4. **更新显卡驱动** - 确保显卡驱动是最新版本
5. **尝试不同主题** - 修改 `client.properties` 中的 `ui.theme` 设置

### 问题6：音效无法播放

**问题描述：**
游戏中没有声音

**解决方案：**
1. **检查音频文件** - 确保 `source/audio/` 目录下有音效文件
2. **检查音效开关** - 在 `client.properties` 中确认 `audio.enabled=true`
3. **调整音量** - 检查系统音量和游戏内音量设置
4. **检查音频格式** - 确保音频文件是.wav格式
5. **测试系统音频** - 播放其他音频确认系统音频正常

### 问题7：测试失败

**错误提示：**
```
[ERROR] Tests run: X, Failures: Y, Errors: Z
```

**解决方案：**
1. **查看测试日志** - 检查具体哪个测试失败
2. **跳过测试打包** - 如果只是想快速运行：
   ```bash
   mvn package -DskipTests
   ```
3. **单独运行测试** - 测试特定模块：
   ```bash
   mvn test -pl xiangqi-shared
   ```
4. **清理后重试** - 运行 `mvn clean test`

---

## 📚 更多文档

想了解更多信息？查看以下文档：

- 📖 **[README.md](README.md)** - 项目概述和架构说明
- 📘 **[USER_GUIDE.md](USER_GUIDE.md)** - 详细的用户使用手册，包含游戏规则和操作指南
- 👨‍💻 **[DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)** - 开发者文档，包含代码架构和开发规范
- ⚙️ **[CONFIG_README.md](CONFIG_README.md)** - 配置文件详细说明和调优指南

---

## 💡 使用技巧

### 技巧1：同时测试多个客户端

您可以同时打开多个客户端窗口进行测试：

1. 运行第一个客户端，输入用户名"玩家1"登录
2. 再次运行 `start-client.bat` 打开第二个客户端
3. 输入不同的用户名"玩家2"登录
4. 在游戏大厅中互相邀请对战

### 技巧2：查看服务器日志

服务器窗口会实时显示日志信息，包括：
- 客户端连接/断开信息
- 游戏创建和结束信息
- 错误和异常信息

如果需要保存日志，可以在 `server.properties` 中配置：
```properties
logging.file=xiangqi-server.log
```

### 技巧3：使用快捷键

在游戏界面中支持以下快捷键（如果已实现）：
- `Ctrl + Z` - 悔棋（需要对方同意）
- `Ctrl + S` - 保存棋局
- `Ctrl + L` - 加载棋局
- `Esc` - 取消选择

### 技巧4：远程联机

如果要通过互联网与朋友对战：

1. **服务器端**：
   - 确保路由器开启了端口转发（转发8888端口）
   - 获取公网IP地址
   
2. **客户端**：
   - 修改 `client.properties`：
     ```properties
     client.serverHost=服务器的公网IP
     ```

---

## 🎯 下一步

恭喜！您已经成功启动了象棋游戏。现在您可以：

- ✅ 邀请朋友一起玩
- ✅ 阅读[用户手册](USER_GUIDE.md)了解详细玩法
- ✅ 自定义配置文件调整游戏设置
- ✅ 查看[开发者文档](DEVELOPER_GUIDE.md)了解如何扩展功能

---

## ❓ 获取帮助

如果遇到其他问题：

1. **查看日志** - 检查服务器和客户端的控制台输出
2. **阅读文档** - 浏览本项目的其他Markdown文档
3. **检查配置** - 确认 `server.properties` 和 `client.properties` 配置正确
4. **提交Issue** - 在项目仓库提交Issue描述问题

---

**祝您游戏愉快！** 🎉
