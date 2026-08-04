@echo off
chcp 65001
:: 魔法俱乐部后端打包脚本
:: 需要已安装 Maven 并配置环境变量

echo ========================================
echo   魔法俱乐部后端打包脚本
echo ========================================
echo.

:: 检查 Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 mvn 命令
    echo.
    echo 请确保已安装 Maven 并添加到环境变量。
    echo.
    echo 或者使用 IDEA 打包：
    echo   1. 打开 IntelliJ IDEA
    echo   2. 右侧 Maven 面板 -> Lifecycle
    echo   3. 双击 clean，然后双击 package
    echo.
    pause
    exit /b 1
)

echo [1/3] 清理旧构建...
call mvn clean
if %errorlevel% neq 0 (
    echo [错误] 清理失败
    pause
    exit /b 1
)
echo √ 清理完成
echo.

echo [2/3] 编译打包（跳过测试）...
call mvn package -DskipTests
if %errorlevel% neq 0 (
    echo [错误] 打包失败
    pause
    exit /b 1
)
echo √ 打包完成
echo.

echo [3/3] 复制 JAR 包到 deploy 目录...
if not exist "..\..\Desktop\deploy" mkdir "..\..\Desktop\deploy"
copy "target\magic-club-1.0.0.jar" "..\..\Desktop\deploy\"
echo √ 复制完成
echo.

echo ========================================
echo   打包成功！
echo ========================================
echo.
echo JAR 包位置：
echo   target\magic-club-1.0.0.jar
echo   ..\..\Desktop\deploy\magic-club-1.0.0.jar
echo.
pause
