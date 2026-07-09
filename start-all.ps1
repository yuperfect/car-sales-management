# Car Sales Management System - Start All Services
# 一键启动：后端 + 管理端 + 客户端，自动打开浏览器
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  汽车销售管理系统 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$root = "D:\MIS_Design\workspace\car-sales-management"
$javaHome = "D:\develop\jdk25"

Write-Host "[1/3] 启动后端 (Spring Boot :8080)..." -ForegroundColor Yellow
$backend = Start-Process -FilePath "cmd" -ArgumentList "/c","$javaHome\bin\java -jar `"$root\code\car-sales-backend\target\car-sales-backend-1.0.0.jar`" --server.port=8080" -WindowStyle Normal -PassThru
Write-Host "  ✓ 后端正在启动（等待 15 秒）..." -ForegroundColor Green
Start-Sleep -Seconds 15

Write-Host "[2/3] 启动管理端 (Vite :5173)..." -ForegroundColor Yellow
$admin = Start-Process -FilePath "cmd" -ArgumentList "/k cd /d `"$root\code\car-sales-admin`" && npm run dev" -WindowStyle Normal -PassThru
Write-Host "  ✓ 管理端正在启动..." -ForegroundColor Green
Start-Sleep -Seconds 5

Write-Host "[3/3] 启动客户端 (Vite :3000)..." -ForegroundColor Yellow
$client = Start-Process -FilePath "cmd" -ArgumentList "/k cd /d `"$root\code\car-sales-client`" && npm run dev" -WindowStyle Normal -PassThru
Write-Host "  ✓ 客户端正在启动..." -ForegroundColor Green
Start-Sleep -Seconds 3

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  正在打开浏览器..." -ForegroundColor Cyan
Start-Sleep -Seconds 2
Start-Process "http://localhost:3000"
Start-Sleep -Seconds 1
Start-Process "http://localhost:5173/admin"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  启动完成! 三个服务已全部启动:" -ForegroundColor Green
Write-Host "`n  后端:    http://localhost:8080" -ForegroundColor White
Write-Host "  管理端:  http://localhost:5173/admin" -ForegroundColor White
Write-Host "  客户端:  http://localhost:3000" -ForegroundColor White
Write-Host "`n  已自动打开浏览器访问客户端和管理端。" -ForegroundColor Yellow
Write-Host "  关闭方式: 双击 关闭系统.exe 或 stop.bat" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan