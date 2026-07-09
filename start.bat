@echo off
chcp 65001 >nul
title 汽车销售管理系统 - 一键启动
cd /d D:\MIS_Design\workspace\car-sales-management

echo ========================================
echo    汽车销售管理系统 - 一键启动
echo ========================================
echo.

:: 设置 JAVA_HOME
set JAVA_HOME=D:\develop\jdk25
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1/3] 启动后端 (Spring Boot :8080)...
start "CarSales-Backend" cmd /c "%JAVA_HOME%\bin\java -jar code\car-sales-backend\target\car-sales-backend-1.0.0.jar --server.port=8080"
echo   ✓ 后端正在启动（等待 15 秒）...
timeout /t 15 /nobreak >nul

echo [2/3] 启动管理端 (Vite :5173)...
start "CarSales-Admin" cmd /c "cd /d code\car-sales-admin && npm run dev"
echo   ✓ 管理端正在启动...
timeout /t 5 /nobreak >nul

echo [3/3] 启动客户端 (Vite :3000)...
start "CarSales-Client" cmd /c "cd /d code\car-sales-client && npm run dev"
echo   ✓ 客户端正在启动...
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo  正在打开浏览器...
echo ========================================
timeout /t 5 /nobreak >nul
start "" http://localhost:3000
timeout /t 2 /nobreak >nul
start "" http://localhost:5173/admin

echo.
echo ========================================
echo  启动完成! 三个服务已全部启动:
echo.
echo  后端:    http://localhost:8080
echo  管理端:  http://localhost:5173/admin
echo  客户端:  http://localhost:3000
echo.
echo  系统已自动打开浏览器访问客户端和管理端。
echo  每个服务都有独立的命令行窗口（CarSales-*）。
echo  关闭方式: 双击 关闭系统.exe 或 stop.bat
echo ========================================
echo.
echo 按任意键退出本窗口（服务继续后台运行）...
pause >nul