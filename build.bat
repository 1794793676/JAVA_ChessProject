@echo off
chcp 65001 >nul 2>&1
REM 象棋游戏构建脚本 - Windows版本
REM Xiangqi Game Build Script - Windows Version

echo ========================================
echo 象棋游戏构建脚本
echo Xiangqi Game Build Script
echo ========================================

REM 检查Maven是否安装
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到Maven，请确保Maven已安装并添加到PATH
    echo Error: Maven not found, please ensure Maven is installed and added to PATH
    pause
    exit /b 1
)

REM 检查Java是否安装
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到Java，请确保Java已安装并添加到PATH
    echo Error: Java not found, please ensure Java is installed and added to PATH
    pause
    exit /b 1
)

echo 开始构建项目...
echo Starting project build...
echo.

REM 优化的构建流程 - 使用单一命令避免重复测试
REM Optimized build process - using single command to avoid redundant tests
echo [1/3] 清理和编译 / Clean and compile...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败 / Compilation failed
    pause
    exit /b 1
)

REM 运行测试（仅一次）
echo [2/3] 运行测试 / Running tests...
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo 测试失败 / Tests failed
    echo 提示: 如需跳过测试快速构建，请运行 quick-build.bat
    echo Tip: To skip tests for quick build, run quick-build.bat
    pause
    exit /b 1
)

REM 打包并安装（跳过重复测试）
echo [3/3] 打包并安装 / Package and install...
call mvn package install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 打包/安装失败 / Package/Install failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo 构建完成! / Build completed!
echo ========================================
echo.
echo 生成的文件 / Generated files:
echo - 服务器JAR / Server JAR: xiangqi-server\target\xiangqi-server.jar
echo - 客户端JAR / Client JAR: xiangqi-client\target\xiangqi-client.jar
echo.
echo 快速启动 / Quick Start:
echo - 启动服务器 / Start server: start-server.bat
echo - 启动客户端 / Start client: start-client.bat
echo.
echo 或手动启动 / Or manually:
echo   java -jar xiangqi-server\target\xiangqi-server.jar
echo   java -jar xiangqi-client\target\xiangqi-client.jar
echo.

pause