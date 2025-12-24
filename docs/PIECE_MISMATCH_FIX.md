# 棋子不匹配问题修复说明

## 问题描述

在将军状态下，玩家无法移动棋子。错误信息：
```
服务端: Reason: Piece mismatch at source position
客户端: 移动失败: Invalid move
```

## 问题根源

当游戏状态更新后（特别是调用 `GameState.copy()` 时），棋盘上的棋子对象被重新创建。但客户端发送的 `Move` 对象中保存的 `piece` 引用仍然是旧的对象引用。

服务器端验证时使用 `pieceAtSource.equals(piece)` 比较对象，由于对象引用不同导致验证失败，即使棋子的类型、位置和所有者都相同。

## 解决方案

修改验证逻辑，不依赖对象引用比较，而是比较棋子的属性（类型和所有者）：

### 1. RuleValidator.java 修改

**之前的代码:**
```java
ChessPiece pieceAtSource = state.getPiece(from);
if (pieceAtSource == null || !pieceAtSource.equals(piece)) {
    return false;
}
```

**修改后:**
```java
ChessPiece pieceAtSource = state.getPiece(from);
if (pieceAtSource == null) {
    return false;
}

// Verify piece type and owner match (don't rely on object equality)
if (pieceAtSource.getType() != piece.getType() || 
    !pieceAtSource.getOwner().equals(piece.getOwner())) {
    return false;
}
```

同时，创建新的 Move 对象使用当前状态的实际棋子：
```java
// Create a new move with the actual piece from current state for checking
Move actualMove = new Move(from, to, pieceAtSource, state.getPiece(to));

// Check if this move would leave own general in check
if (wouldLeaveGeneralInCheck(actualMove, state)) {
    return false;
}
```

### 2. ChessEngine.java 修改

#### applyMove() 方法
**之前的代码:**
```java
private void applyMove(Move move) {
    currentState.setPiece(move.getFrom(), null);
    move.getPiece().setPosition(move.getTo());
    currentState.setPiece(move.getTo(), move.getPiece());
}
```

**修改后:**
```java
private void applyMove(Move move) {
    // Get the actual piece from current state (not from the move object)
    ChessPiece actualPiece = currentState.getPiece(move.getFrom());
    
    if (actualPiece == null) {
        LOGGER.warning("No piece found at source position: " + move.getFrom());
        return;
    }
    
    currentState.setPiece(move.getFrom(), null);
    actualPiece.setPosition(move.getTo());
    currentState.setPiece(move.getTo(), actualPiece);
}
```

#### getInvalidMoveReason() 方法
类似地修改为使用当前状态的实际棋子而不是 Move 对象中的引用。

## 修改的文件

1. `xiangqi-shared/src/main/java/com/xiangqi/shared/engine/RuleValidator.java`
   - 修改 `isValidMove()` 方法，使用属性比较而不是对象引用比较
   
2. `xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java`
   - 修改 `applyMove()` 方法，使用当前状态的实际棋子
   - 修改 `getInvalidMoveReason()` 方法，使用属性比较

## 编译和部署

```bash
# 重新编译和安装
cd "c:\Users\dolphin chan\Desktop\JAVA_ChessProject"
mvn install -DskipTests
```

## 测试验证

1. 重启服务器和客户端
2. 开始新游戏
3. 创造将军局面
4. 验证被将军方能够正常移动棋子解除将军
5. 验证不能走出送将的棋步

## 技术细节

### 为什么会出现对象不匹配？

1. **GameState.copy()**: 创建游戏状态副本时，所有棋子对象都被重新创建
2. **状态同步**: 服务器每次移动后通过 `GameStateUpdateMessage` 广播新状态
3. **客户端创建Move**: 客户端使用本地 `gameState.getPiece(from)` 获取棋子创建 Move 对象
4. **状态更新延迟**: 在状态同步完成前，客户端可能已经创建了持有旧引用的 Move 对象

### 解决方案的优势

1. **属性比较**: 比较棋子的类型和所有者，而不是对象引用
2. **使用实际棋子**: 服务器端始终使用当前状态中的实际棋子对象进行操作
3. **兼容性**: 修改后的逻辑对正常游戏流程无影响，只修复了对象引用不匹配的问题

## 日期

修复日期: 2025年12月22日
项目版本: 1.0.0
