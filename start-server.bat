@echo off
chcp 65001 >nul 2>&1
REM 启动象棋游戏服务器
REM Start Xiangqi Game Server

echo ========================================
echo 启动象棋游戏服务器
echo Starting Xiangqi Game Server
echo ========================================
echo.

REM 检查JAR文件是否存在
if not exist "xiangqi-server\target\xiangqi-server.jar" (
    echo 错误: 未找到服务器JAR文件，请先运行 build.bat 构建项目
    echo Error: Server JAR file not found, please run build.bat first
    pause
    exit /b 1
)

echo 正在启动服务器...
echo Starting server...
echo.

REM 启动服务器
java -jar xiangqi-server\target\xiangqi-server.jar

pause
