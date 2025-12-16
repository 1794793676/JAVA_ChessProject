@echo off
chcp 65001 >nul 2>&1
REM 启动象棋游戏客户端
REM Start Xiangqi Game Client

echo ========================================
echo 启动象棋游戏客户端
echo Starting Xiangqi Game Client
echo ========================================
echo.

REM 检查JAR文件是否存在
if not exist "xiangqi-client\target\xiangqi-client.jar" (
    echo 错误: 未找到客户端JAR文件，请先运行 build.bat 构建项目
    echo Error: Client JAR file not found, please run build.bat first
    pause
    exit /b 1
)

echo 正在启动客户端...
echo Starting client...
echo.

REM 启动客户端
java -jar xiangqi-client\target\xiangqi-client.jar

pause
