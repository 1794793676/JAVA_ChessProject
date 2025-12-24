# 将军和将死功能实现说明

## 实现内容

本次更新实现了完整的中国象棋将军和将死判定功能，包括：

### 1. 将军检测 (Check Detection)
- **功能**: 每次移动后自动检测当前玩家是否处于被将军状态
- **实现位置**: `ChessEngine.checkGameEndConditions()`
- **状态更新**: 当检测到将军时，游戏状态会自动更新为 `GameStatus.CHECK`

### 2. 将军提示 (Check Notification)
- **功能**: 当玩家处于被将军状态时，会弹出警告对话框提示
- **实现位置**: `GameFrame.showCheckNotification()`
- **提示内容**: 
  - 对本地玩家：显示"警告：您的将/帅正在被将军！请移动棋子解除将军状态。"
  - 对对手：在聊天区显示玩家正处于被将军状态

### 3. 不能送将 (Cannot Put Own General in Check)
- **功能**: 玩家不能走出让自己将军处于被将军状态的棋步
- **实现位置**: `RuleValidator.wouldLeaveGeneralInCheck()`
- **已有实现**: 此功能之前已经实现，本次更新确保其正常工作

### 4. 将死检测 (Checkmate Detection)
- **功能**: 自动检测将死局面（将军且无法解除）
- **实现位置**: `RuleValidator.isCheckmate()`
- **工作原理**:
  1. 检查当前玩家是否处于被将军状态
  2. 遍历所有己方棋子的所有可能移动
  3. 如果没有任何合法移动可以解除将军，则判定为将死

### 5. 将死提示和游戏结束 (Checkmate Notification & Game End)
- **功能**: 将死时立即结束游戏并显示结果对话框
- **实现位置**: 
  - 服务器端: `GameServer.handleMoveMessage()` 使用 `ChessEngine` 处理移动
  - 客户端: `GameFrame.showGameEndDialog()` 显示游戏结束对话框
- **提示内容**:
  - 获胜玩家：显示"🎉 恭喜您获胜！您成功将死了对手！"
  - 失败玩家：显示"💔 很遗憾，您输了！您被将死了！"

## 关键代码修改

### 1. ChessEngine.java
```java
// 在 checkGameEndConditions() 方法中添加将军状态检测
} else if (isInCheck(currentPlayer)) {
    // Set CHECK status when current player is in check
    currentState.setStatus(GameStatus.CHECK);
    LOGGER.info(currentPlayer.getUsername() + " is in check!");
} else {
    // Game continues normally
    currentState.setStatus(GameStatus.IN_PROGRESS);
}
```

### 2. GameFrame.java
```java
// 添加将军通知方法
private void showCheckNotification(Player playerInCheck) {
    if (playerInCheck.equals(localPlayer)) {
        JOptionPane.showMessageDialog(this,
            "警告：您的将/帅正在被将军！\n请移动棋子解除将军状态。",
            "将军提示",
            JOptionPane.WARNING_MESSAGE);
    }
}

// 在 updateGameState() 中调用将军提示
if (newState.getStatus() == GameStatus.CHECK) {
    showCheckNotification(newState.getCurrentPlayer());
}
```

### 3. GameServer.java (重要修改)
**问题根源**: 服务器之前直接使用 `GameState.executeMove()` 而不是 `ChessEngine.executeMove()`，导致将军将死检测逻辑没有被执行。

**解决方案**:
1. 为每个游戏会话创建 `ChessEngine` 实例
2. 使用 `ChessEngine.executeMove()` 处理所有移动
3. 添加事件监听器处理游戏结束事件

```java
// 创建游戏时同时创建 ChessEngine
ChessEngine engine = new ChessEngine(session.getGameState());
engine.addEventListener(new GameEventListener() {
    @Override
    public void onGameEnded(GameResult result) {
        GameEndMessage endMessage = new GameEndMessage(gameId, result);
        broadcastToGame(gameId, endMessage);
    }
    // ... 其他方法
});
gameEngines.put(gameId, engine);

// 处理移动时使用 ChessEngine
boolean moveExecuted = engine.executeMove(move);
```

## 测试说明

### 测试步骤
1. 启动服务器: 运行 `start-server.bat`
2. 启动两个客户端: 运行 `start-client.bat` 两次
3. 两个玩家登录并开始游戏
4. 尝试以下场景:
   - **将军场景**: 移动棋子使对方将军处于被攻击状态，观察是否显示将军提示
   - **送将场景**: 尝试移动己方棋子导致自己的将军被攻击，观察是否被禁止
   - **将死场景**: 创造将死局面，观察游戏是否立即结束并显示正确的结果对话框

### 预期结果
- ✅ 将军时显示警告对话框
- ✅ 游戏状态标签显示"将军"
- ✅ 不能走出送将的棋步
- ✅ 将死时游戏立即结束
- ✅ 显示胜负结果对话框
- ✅ 游戏结束后返回大厅

## 已实现的象棋规则

- ✅ 基本棋子移动规则（将、士、象、马、车、炮、兵）
- ✅ 将帅不能照面
- ✅ 行棋必须走合法棋步
- ✅ 将军检测
- ✅ 不能送将
- ✅ 将死检测
- ✅ 困毙检测（无子可动）
- ✅ 游戏结束提示

## 文件修改清单

1. `xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java`
   - 添加将军状态检测和设置

2. `xiangqi-client/src/main/java/com/xiangqi/client/ui/GameFrame.java`
   - 添加将军提示对话框
   - 优化将死结果显示

3. `xiangqi-server/src/main/java/com/xiangqi/server/network/GameServer.java`
   - 添加 ChessEngine 支持
   - 修改移动处理逻辑使用 ChessEngine
   - 添加游戏结束事件处理

## 编译和运行

```bash
# 编译项目
mvn clean compile

# 打包项目
mvn package -DskipTests

# 启动服务器
start-server.bat

# 启动客户端
start-client.bat
```

## 注意事项

1. 服务器必须先于客户端启动
2. 需要至少两个客户端才能进行游戏
3. 将军和将死检测在服务器端执行，确保游戏规则的权威性
4. 客户端只负责显示状态和提示，不做游戏逻辑判断

## 版本信息

- 实现日期: 2025年12月22日
- 项目版本: 1.0.0
- Java 版本: 21
