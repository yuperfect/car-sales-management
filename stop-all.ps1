# Car Sales Management System - Stop All Services
# 仅停止本项目相关进程（按端口查找），不会误杀其他 Java/Node 进程
Write-Host "Stopping all services..." -ForegroundColor Cyan

$ports = @(3306, 8080, 3000, 5173)
$stopped = 0

foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn -and $conn.OwningProcess -gt 0) {
        try {
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            Write-Host "  [OK] Port $port stopped" -ForegroundColor Green
            $stopped++
        } catch {
            Write-Host "  [..] Port $port - unable to stop" -ForegroundColor Gray
        }
    } else {
        $name = switch ($port) { 3306 { "MySQL" } 8080 { "Backend" } 3000 { "Client" } 5173 { "Admin" } }
        Write-Host "  [..] $name (port $port) not running" -ForegroundColor Gray
    }
}

if ($stopped -eq 0) {
    Write-Host "  No project services were running." -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Done" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
