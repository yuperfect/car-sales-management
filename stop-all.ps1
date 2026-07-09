# Car Sales Management System - Stop All Services
Write-Host "Stopping all services..." -ForegroundColor Cyan

# 1. Stop backend (Java processes)
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force
    Write-Host "  [OK] Backend (Java) stopped" -ForegroundColor Green
} else {
    Write-Host "  [..] Backend not running" -ForegroundColor Gray
}

# 2. Stop client (port 3000)
$p3000 = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue
if ($p3000) {
    Stop-Process -Id $p3000.OwningProcess -Force
    Write-Host "  [OK] Client (port 3000) stopped" -ForegroundColor Green
} else {
    Write-Host "  [..] Client not running" -ForegroundColor Gray
}

# 3. Stop admin (port 5173)
$p5173 = Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue
if ($p5173) {
    Stop-Process -Id $p5173.OwningProcess -Force
    Write-Host "  [OK] Admin (port 5173) stopped" -ForegroundColor Green
} else {
    Write-Host "  [..] Admin not running" -ForegroundColor Gray
}

# 4. Stop Node.js (Vite)
$nodeProcs = Get-Process node -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $nodeProcs | Stop-Process -Force
    Write-Host "  [OK] Node.js processes stopped" -ForegroundColor Green
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services stopped" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
