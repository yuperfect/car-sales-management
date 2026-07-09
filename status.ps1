# Car Sales Management System - Status Check
Write-Host ""
Write-Host "====== System Status ======" -ForegroundColor Cyan
Write-Host ""

function CheckService {
    param([string]$Name, [int]$Port, [string]$Url)
    $portOk = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    if (-not $portOk) {
        Write-Host "  [STOPPED] $Name (port $Port)" -ForegroundColor Red
        return
    }
    if ($Url) {
        try {
            $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
            Write-Host "  [RUNNING] $Name (port $Port, HTTP $($r.StatusCode))" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN]    $Name (port $Port, no HTTP response)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  [RUNNING] $Name (port $Port)" -ForegroundColor Green
    }
}

CheckService -Name "MySQL"          -Port 3306
CheckService -Name "Backend API"    -Port 8080 -Url "http://localhost:8080/api/cars/1"
CheckService -Name "Client"         -Port 3000 -Url "http://localhost:3000"
CheckService -Name "Admin"          -Port 5173 -Url "http://localhost:5173"

Write-Host ""
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/cars" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    $data = $r.Content | ConvertFrom-Json
    Write-Host "  Car count: $($data.data.Count)" -ForegroundColor Gray
} catch {}

Write-Host ""
Write-Host "  Start: .\start-all.ps1" -ForegroundColor Gray
Write-Host "  Stop:  .\stop-all.ps1"  -ForegroundColor Gray
Write-Host ""
