@echo off
chcp 65001 >nul 2>&1
REM 快速构建脚本 - 跳过测试
REM Quick Build Script - Skip Tests

echo ========================================
echo 快速构建（跳过测试）
echo Quick Build (Skip Tests)
echo ========================================

REM 检查Maven是否安装
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到Maven
    echo Error: Maven not found
    pause
    exit /b 1
)

echo 开始快速构建...
echo Starting quick build...
echo.

REM 清理、编译、打包、安装 - 一次完成，跳过测试
echo 正在构建项目（跳过测试）...
echo Building project (skipping tests)...
call mvn clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 构建失败 / Build failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo 快速构建完成! / Quick build completed!
echo ========================================
echo.
echo ⚠️  注意: 测试已被跳过
echo ⚠️  Warning: Tests were skipped
echo.
echo 生成的文件 / Generated files:
echo - 服务器JAR / Server JAR: xiangqi-server\target\xiangqi-server.jar
echo - 客户端JAR / Client JAR: xiangqi-client\target\xiangqi-client.jar
echo.
echo 快速启动 / Quick Start:
echo - 启动服务器 / Start server: start-server.bat
echo - 启动客户端 / Start client: start-client.bat
echo.

pause
