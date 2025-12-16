# 快速启动指南 / Quick Start Guide

## 🚀 快速开始

### 1️⃣ 构建项目
双击运行 `build.bat` 文件，或在命令行中执行：
```bash
build.bat
```

构建过程包括：
- 清理项目
- 编译代码
- 运行测试
- 打包JAR文件
- 安装到本地仓库

### 2️⃣ 启动服务器
双击运行 `start-server.bat` 文件，或在命令行中执行：
```bash
start-server.bat
```

服务器默认配置（可在 `server.properties` 中修改）：
- 端口：8888
- 最大玩家数：100
- 超时时间：30000ms

### 3️⃣ 启动客户端
双击运行 `start-client.bat` 文件，或在命令行中执行：
```bash
start-client.bat
```

客户端默认配置（可在 `client.properties` 中修改）：
- 服务器地址：localhost
- 端口：8888

---

## 📋 系统要求

- ☕ Java 8 或更高版本
- 📦 Maven 3.6 或更高版本
- 💻 Windows 操作系统

---

## 🎮 使用说明

1. **启动服务器**：首先启动服务器，等待客户端连接
2. **启动客户端**：可以启动多个客户端实例
3. **登录**：在登录界面输入用户名
4. **进入大厅**：选择创建房间或加入现有房间
5. **开始游戏**：等待对手加入后开始对弈

---

## 🛠️ 开发命令

### 仅编译（不运行测试）
```bash
mvn compile
```

### 仅运行测试
```bash
mvn test
```

### 清理构建
```bash
mvn clean
```

### 完整构建
```bash
mvn clean install
```

---

## 📁 生成的文件位置

构建成功后，可执行JAR文件位于：
- **服务器**：`xiangqi-server\target\xiangqi-server.jar`
- **客户端**：`xiangqi-client\target\xiangqi-client.jar`

---

## ⚠️ 常见问题

### 问题1：Maven 未找到
**解决方案**：
1. 下载并安装 Maven
2. 配置环境变量 `MAVEN_HOME`
3. 将 Maven 的 bin 目录添加到 `PATH`

### 问题2：Java 未找到
**解决方案**：
1. 下载并安装 JDK 8 或更高版本
2. 配置环境变量 `JAVA_HOME`
3. 将 JDK 的 bin 目录添加到 `PATH`

### 问题3：编译失败
**解决方案**：
1. 检查是否有语法错误
2. 运行 `mvn clean` 清理项目
3. 重新运行 `build.bat`

### 问题4：无法连接服务器
**解决方案**：
1. 确保服务器已启动
2. 检查防火墙设置
3. 验证 `client.properties` 中的服务器地址和端口

---

## 📚 更多文档

- [用户指南](USER_GUIDE.md) - 详细的使用说明
- [开发者指南](DEVELOPER_GUIDE.md) - 代码架构和开发指南
- [配置说明](CONFIG_README.md) - 配置文件详解

---

## 💡 提示

- 可以同时启动多个客户端进行测试
- 服务器日志会显示连接状态和游戏信息
- 修改配置文件后需要重启服务器或客户端
