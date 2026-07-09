@echo off
chcp 65001 >nul
title 汽车销售管理系统 - 一键启动
echo ========================================
echo    汽车销售管理系统 - 一键启动
echo ========================================
echo.

:: 设置 JAVA_HOME
set JAVA_HOME=D:\develop\jdk25
set PATH=%JAVA_HOME%\bin;%PATH%

:: 项目根目录
set ROOT=D:\MIS_Design\workspace\car-sales-management

echo [1/3] 启动后端 (Spring Boot :8080)...
start "backend" cmd /c "%JAVA_HOME%\bin\java -jar %ROOT%\code\car-sales-backend\target\car-sales-backend-1.0.0.jar --server.port=8080"
timeout /t 15 /nobreak >nul

echo [2/3] 启动管理端 (Vite :5173)...
start "admin" cmd /c "cd /d %ROOT%\code\car-sales-admin && npm run dev"
timeout /t 5 /nobreak >nul

echo [3/3] 启动客户端 (Vite :3000)...
start "client" cmd /c "cd /d %ROOT%\code\car-sales-client && npm run dev -- --port 3000"
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo  启动完成!
echo  后端:    http://localhost:8080
echo  管理端:  http://localhost:5173/admin
echo  客户端:  http://localhost:3000
echo ========================================
echo.
echo 关闭所有服务请运行 stop-all.ps1
echo 按任意键退出此窗口（服务将在后台继续运行）...
pause >nul
