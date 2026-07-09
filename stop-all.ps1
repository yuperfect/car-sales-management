# Car Sales Management System - Stop All Services
# 关闭本项目所有服务（后端:8080, 管理端:5173, 客户端:3000）
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  汽车销售管理系统 - 一键关闭" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$ports = @(8080, 5173, 3000)
$stopped = 0

foreach ($port in $ports) {
    try {
        $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($conn -and $conn.OwningProcess -gt 0) {
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            Write-Host "  [OK] 端口 $port 已关闭" -ForegroundColor Green
            $stopped++
        } else {
            $name = switch ($port) { 8080 { "后端" } 5173 { "管理端" } 3000 { "客户端" } }
            Write-Host "  [..] $name (端口 $port) 未运行" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  [!!] 端口 $port 关闭失败: $_" -ForegroundColor Red
    }
}

Write-Host "----------------------------------------" -ForegroundColor Cyan
if ($stopped -eq 0) {
    Write-Host "  没有运行中的项目服务" -ForegroundColor Yellow
} else {
    Write-Host "  成功关闭 $stopped 个服务" -ForegroundColor Green
}
Write-Host "========================================" -ForegroundColor Cyan

# 也尝试通过窗口标题杀死进程
Get-Process | Where-Object { $_.MainWindowTitle -match "CarSales-" } | Stop-Process -Force -ErrorAction SilentlyContinue
