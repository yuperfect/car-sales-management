@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 汽车销售管理系统 - 一键关闭

echo ========================================
echo    汽车销售管理系统 - 一键关闭
echo ========================================
echo.
echo 正在关闭所有服务...

set PORTS=8080 5173 3000
set STOPPED=0

for %%p in (%PORTS%) do (
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p "') do (
    if not "%%a"=="" (
      taskkill /f /pid %%a >nul 2>&1
      set /a STOPPED+=1
      echo   ✓ 端口 %%p 已关闭
    )
  )
)

:: 也尝试通过窗口标题关闭
taskkill /f /fi "WINDOWTITLE eq CarSales-Backend*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq CarSales-Admin*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq CarSales-Client*" >nul 2>&1

echo.
if !STOPPED! equ 0 (
  echo  没有运行中的服务，或服务已关闭。
) else (
  echo  成功关闭 !STOPPED! 个服务端口。
)
echo ========================================
echo.
pause
