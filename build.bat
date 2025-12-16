@echo off
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

REM 清理项目
echo [1/5] 清理项目 / Cleaning project...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo 清理失败 / Clean failed
    pause
    exit /b 1
)

REM 编译项目
echo [2/5] 编译项目 / Compiling project...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败 / Compilation failed
    pause
    exit /b 1
)

REM 运行测试
echo [3/5] 运行测试 / Running tests...
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo 测试失败 / Tests failed
    pause
    exit /b 1
)

REM 打包项目
echo [4/5] 打包项目 / Packaging project...
call mvn package
if %ERRORLEVEL% NEQ 0 (
    echo 打包失败 / Packaging failed
    pause
    exit /b 1
)

REM 安装到本地仓库
echo [5/5] 安装到本地仓库 / Installing to local repository...
call mvn install
if %ERRORLEVEL% NEQ 0 (
    echo 安装失败 / Installation failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo 构建完成! / Build completed!
echo ========================================
echo.
echo 生成的文件 / Generated files:
echo - 服务器JAR / Server JAR: xiangqi-server\target\xiangqi-server-1.0-SNAPSHOT.jar
echo - 客户端JAR / Client JAR: xiangqi-client\target\xiangqi-client-1.0-SNAPSHOT.jar
echo.
echo 启动服务器 / Start server:
echo   java -jar xiangqi-server\target\xiangqi-server-1.0-SNAPSHOT.jar
echo.
echo 启动客户端 / Start client:
echo   java -jar xiangqi-client\target\xiangqi-client-1.0-SNAPSHOT.jar
echo.

pause