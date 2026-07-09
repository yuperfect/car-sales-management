<#
╔══════════════════════════════════════════════════════════════╗
║      汽车销售管理系统 — 一键停止脚本                        ║
╚══════════════════════════════════════════════════════════════╝
#>

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  正在关闭所有服务..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. 停后端 (所有 Java 进程)
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force
    Write-Host "  ✅ 后端 (Java) 已停止" -ForegroundColor Green
} else {
    Write-Host "  ℹ️ 后端 (Java) 未运行" -ForegroundColor Gray
}

# 2. 停客户端 (端口 3000)
$p3000 = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue
if ($p3000) {
    Stop-Process -Id $p3000.OwningProcess -Force
    Write-Host "  ✅ 客户端 (端口 3000) 已停止" -ForegroundColor Green
} else {
    Write-Host "  ℹ️ 客户端 (端口 3000) 未运行" -ForegroundColor Gray
}

# 3. 停管理端 (端口 5173)
$p5173 = Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue
if ($p5173) {
    Stop-Process -Id $p5173.OwningProcess -Force
    Write-Host "  ✅ 管理端 (端口 5173) 已停止" -ForegroundColor Green
} else {
    Write-Host "  ℹ️ 管理端 (端口 5173) 未运行" -ForegroundColor Gray
}

# 4. 停 Node.js 进程 (Vite)
$nodeProcs = Get-Process node -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $nodeProcs | Stop-Process -Force
    Write-Host "  ✅ Node.js 进程已停止" -ForegroundColor Green
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ 所有服务已关闭" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
