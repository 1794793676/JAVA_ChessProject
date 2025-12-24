
# Upgrade Java Project

## 🖥️ Project Information
- **Project path**: c:\Users\dolphin chan\Desktop\JAVA_ChessProject
- **Java version**: 21
- **Build tool type**: Maven
- **Build tool path**: C:\apache-maven-3.9.11\bin

## 🎯 Goals

- Upgrade Java to 21

## 🔀 Changes

### Test Changes
|     | Total | Passed | Failed | Skipped | Errors |
|-----|-------|--------|--------|---------|--------|
| Before | 86 | 82 | 4 | 0 | 0 |
| After | 86 | 86 | 0 | 0 | 0 |
### Dependency Changes


#### Upgraded Dependencies
| Dependency | Original Version | Current Version | Module |
|------------|------------------|-----------------|--------|
| Java | 8 | 21 | Root Module |

### Code commits

> There are uncommitted changes in the project before upgrading, which have been stashed according to user setting "appModernization.uncommittedChangesAction".

All code changes have been committed to branch `appmod/java-upgrade-20251217091327`, here are the details:
142 files changed, 36 insertions(+), 217 deletions(-)

- 6e045fd -- 升级 Java 版本从 8 到 21

- 7cb362d -- 修复测试中棋子颜色设置

- 9e43f57 -- 清空测试棋盘以避免干扰
### Potential Issues

#### Behavior Changes
- [ChessEngine.java](../../../xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java)
  - [Severity: **MINOR**] [Confidence: **HIGH**] The new implementation adds explicit side configuration for each piece during board initialization. Previously, pieces were placed without explicitly setting their `redSide` property, which could lead to pieces not knowing which side they belong to. This ensures that all pieces have their side properly configured when placed on the board.
- [ChessEngineTest.java](../../../xiangqi-shared/src/test/java/com/xiangqi/shared/engine/ChessEngineTest.java)
  - [Severity: **MINOR**] [Confidence: **HIGH**] The test now ensures a clean board state and explicitly configures piece sides. This provides better test isolation by clearing any pre-initialized pieces and ensures pieces have their side properly configured, making the test more reliable and explicit about its setup.
