@echo off
echo Verifying Networked Xiangqi Game Project Structure...
echo.

echo Checking root project files:
if exist pom.xml (echo [OK] Root pom.xml found) else (echo [ERROR] Root pom.xml missing)
if exist README.md (echo [OK] README.md found) else (echo [ERROR] README.md missing)
echo.

echo Checking xiangqi-shared module:
if exist xiangqi-shared\pom.xml (echo [OK] Shared module pom.xml found) else (echo [ERROR] Shared module pom.xml missing)
if exist xiangqi-shared\src\main\java (echo [OK] Shared main source directory found) else (echo [ERROR] Shared main source directory missing)
if exist xiangqi-shared\src\test\java (echo [OK] Shared test source directory found) else (echo [ERROR] Shared test source directory missing)
echo.

echo Checking xiangqi-client module:
if exist xiangqi-client\pom.xml (echo [OK] Client module pom.xml found) else (echo [ERROR] Client module pom.xml missing)
if exist xiangqi-client\src\main\java (echo [OK] Client main source directory found) else (echo [ERROR] Client main source directory missing)
if exist xiangqi-client\src\test\java (echo [OK] Client test source directory found) else (echo [ERROR] Client test source directory missing)
echo.

echo Checking xiangqi-server module:
if exist xiangqi-server\pom.xml (echo [OK] Server module pom.xml found) else (echo [ERROR] Server module pom.xml missing)
if exist xiangqi-server\src\main\java (echo [OK] Server main source directory found) else (echo [ERROR] Server main source directory missing)
if exist xiangqi-server\src\test\java (echo [OK] Server test source directory found) else (echo [ERROR] Server test source directory missing)
echo.

echo Checking core model classes:
if exist xiangqi-shared\src\main\java\com\xiangqi\shared\model\Player.java (echo [OK] Player.java found) else (echo [ERROR] Player.java missing)
if exist xiangqi-shared\src\main\java\com\xiangqi\shared\model\Position.java (echo [OK] Position.java found) else (echo [ERROR] Position.java missing)
if exist xiangqi-shared\src\main\java\com\xiangqi\shared\model\ChessPiece.java (echo [OK] ChessPiece.java found) else (echo [ERROR] ChessPiece.java missing)
if exist xiangqi-shared\src\main\java\com\xiangqi\shared\model\GameState.java (echo [OK] GameState.java found) else (echo [ERROR] GameState.java missing)
echo.

echo Checking network classes:
if exist xiangqi-shared\src\main\java\com\xiangqi\shared\network\NetworkMessage.java (echo [OK] NetworkMessage.java found) else (echo [ERROR] NetworkMessage.java missing)
if exist xiangqi-shared\src\main\java\com\xiangqi\shared\network\NetworkMessageHandler.java (echo [OK] NetworkMessageHandler.java found) else (echo [ERROR] NetworkMessageHandler.java missing)
echo.

echo Checking test classes:
if exist xiangqi-shared\src\test\java\com\xiangqi\shared\model\PlayerTest.java (echo [OK] PlayerTest.java found) else (echo [ERROR] PlayerTest.java missing)
if exist xiangqi-shared\src\test\java\com\xiangqi\shared\model\PositionTest.java (echo [OK] PositionTest.java found) else (echo [ERROR] PositionTest.java missing)
echo.

echo Checking game assets:
if exist source\audio (echo [OK] Audio assets directory found) else (echo [ERROR] Audio assets directory missing)
if exist source\face (echo [OK] Face assets directory found) else (echo [ERROR] Face assets directory missing)
if exist source\img (echo [OK] Image assets directory found) else (echo [ERROR] Image assets directory missing)
if exist source\qizi (echo [OK] Chess piece assets directory found) else (echo [ERROR] Chess piece assets directory missing)
echo.

echo Project structure verification complete!
pause