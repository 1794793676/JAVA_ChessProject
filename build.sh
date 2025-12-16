#!/bin/bash
# 象棋游戏构建脚本 - Linux/macOS版本
# Xiangqi Game Build Script - Linux/macOS Version

echo "========================================"
echo "象棋游戏构建脚本"
echo "Xiangqi Game Build Script"
echo "========================================"

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请确保Maven已安装并添加到PATH"
    echo "Error: Maven not found, please ensure Maven is installed and added to PATH"
    exit 1
fi

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java，请确保Java已安装并添加到PATH"
    echo "Error: Java not found, please ensure Java is installed and added to PATH"
    exit 1
fi

echo "开始构建项目..."
echo "Starting project build..."
echo

# 清理项目
echo "[1/5] 清理项目 / Cleaning project..."
mvn clean
if [ $? -ne 0 ]; then
    echo "清理失败 / Clean failed"
    exit 1
fi

# 编译项目
echo "[2/5] 编译项目 / Compiling project..."
mvn compile
if [ $? -ne 0 ]; then
    echo "编译失败 / Compilation failed"
    exit 1
fi

# 运行测试
echo "[3/5] 运行测试 / Running tests..."
mvn test
if [ $? -ne 0 ]; then
    echo "测试失败 / Tests failed"
    exit 1
fi

# 打包项目
echo "[4/5] 打包项目 / Packaging project..."
mvn package
if [ $? -ne 0 ]; then
    echo "打包失败 / Packaging failed"
    exit 1
fi

# 安装到本地仓库
echo "[5/5] 安装到本地仓库 / Installing to local repository..."
mvn install
if [ $? -ne 0 ]; then
    echo "安装失败 / Installation failed"
    exit 1
fi

echo
echo "========================================"
echo "构建完成! / Build completed!"
echo "========================================"
echo
echo "生成的文件 / Generated files:"
echo "- 服务器JAR / Server JAR: xiangqi-server/target/xiangqi-server-1.0-SNAPSHOT.jar"
echo "- 客户端JAR / Client JAR: xiangqi-client/target/xiangqi-client-1.0-SNAPSHOT.jar"
echo
echo "启动服务器 / Start server:"
echo "  java -jar xiangqi-server/target/xiangqi-server-1.0-SNAPSHOT.jar"
echo
echo "启动客户端 / Start client:"
echo "  java -jar xiangqi-client/target/xiangqi-client-1.0-SNAPSHOT.jar"
echo