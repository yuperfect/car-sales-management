# Car Sales Management System - Status Check
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  汽车销售管理系统 - 状态检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$services = @(
    @{Port=8080; Name="后端 (Spring Boot)"; Url="http://localhost:8080/api/cars"},
    @{Port=5173; Name="管理端 (Vite Admin)"; Url="http://localhost:5173"},
    @{Port=3000; Name="客户端 (Vite Client)"; Url="http://localhost:3000"}
)

foreach ($svc in $services) {
    $port = $svc.Port
    $name = $svc.Name
    
    # Check port
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn -and $conn.State -eq "Listen") {
        # Check HTTP
        try {
            $r = Invoke-WebRequest -Uri $svc.Url -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
            Write-Host "  [RUNNING] $name ($port) - HTTP $($r.StatusCode)" -ForegroundColor Green
        } catch {
            Write-Host "  [STARTING] $name ($port) - Process running, HTTP not ready" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  [STOPPED] $name ($port)" -ForegroundColor Red
    }
}

Write-Host "========================================" -ForegroundColor Cyan
