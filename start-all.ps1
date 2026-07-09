# Car Sales Management System - Start All Services
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendPath = "$ProjectRoot\code\car-sales-backend"
$ClientPath  = "$ProjectRoot\code\car-sales-client"
$AdminPath   = "$ProjectRoot\code\car-sales-admin"
$LogFile     = "$ProjectRoot\start-log.txt"
$Stopwatch   = [System.Diagnostics.Stopwatch]::StartNew()

function Log {
    param([string]$Msg)
    $time = Get-Date -Format "HH:mm:ss"
    $line = "[$time] $Msg"
    Write-Host $line -ForegroundColor Cyan
    Add-Content -Path $LogFile -Value $line
}

function CheckPort {
    param([int]$Port)
    $conn = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    return ($null -ne $conn)
}

function WaitForPort {
    param([int]$Port, [int]$TimeoutSeconds = 60, [string]$ServiceName)
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (CheckPort -Port $Port) {
            Log "[OK] $ServiceName ready (port $Port)"
            return $true
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    Log "[FAIL] $ServiceName timeout (${TimeoutSeconds}s)"
    return $false
}

Remove-Item -Path $LogFile -ErrorAction SilentlyContinue
Log "========================================="
Log "  Car Sales Management System - Starting"
Log "========================================="

# Step 0: MySQL check
Log "Checking MySQL..."
$mysqlService = Get-Service MySQL80 -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -ne 'Running') {
    Log "Starting MySQL service..."
    Start-Service MySQL80
    Start-Sleep -Seconds 5
}
if (CheckPort -Port 3306) {
    Log "[OK] MySQL running (port 3306)"
} else {
    Log "[WARN] MySQL not found on port 3306"
}

# Step 1: Build backend
Log "Building backend..."
Push-Location $BackendPath
$env:MAVEN_OPTS = "-Dmaven.multiModuleProjectDirectory=$BackendPath"
$buildOutput = cmd /c "mvnw.cmd package -DskipTests -q 2>&1"
if ($LASTEXITCODE -eq 0) {
    Log "[OK] Backend build success"
} else {
    Log "[FAIL] Backend build failed"
    Write-Host "Build error:" -ForegroundColor Red
    Write-Host $buildOutput -ForegroundColor Red
    Pop-Location
    exit 1
}

# Step 2: Start backend
Log "Starting backend (port 8080)..."
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

$jarFile = "$BackendPath\target\car-sales-backend-1.0.0.jar"
if (Test-Path $jarFile) {
    Start-Process -FilePath "java" -ArgumentList "-jar",$jarFile -WindowStyle Normal
    $backendOk = WaitForPort -Port 8080 -ServiceName "Backend API"
    if (-not $backendOk) {
        Pop-Location
        exit 1
    }
} else {
    Log "[FAIL] JAR not found: $jarFile"
    Pop-Location
    exit 1
}
Pop-Location

# Step 3: Start client
Log "Starting client frontend (port 3000)..."
Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
}
Start-Process -FilePath "cmd" -ArgumentList "/c","npm","run","dev" -WorkingDirectory $ClientPath

# Step 4: Start admin
Log "Starting admin frontend (port 5173)..."
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
}
Start-Process -FilePath "cmd" -ArgumentList "/c","npm","run","dev" -WorkingDirectory $AdminPath

# Wait for frontends
Start-Sleep -Seconds 8
$clientOk = $false; $adminOk = $false
$elapsed = 0
while ($elapsed -lt 20) {
    if (-not $clientOk) { $clientOk = CheckPort -Port 3000 }
    if (-not $adminOk)  { $adminOk  = CheckPort -Port 5173 }
    if ($clientOk -and $adminOk) { break }
    Start-Sleep -Seconds 2
    $elapsed += 2
}

if ($clientOk) { Log "[OK] Client frontend ready (port 3000)" }
else           { Log "[WARN] Client frontend may not be ready" }
if ($adminOk)  { Log "[OK] Admin frontend ready (port 5173)" }
else           { Log "[WARN] Admin frontend may not be ready" }

# Verify API
Start-Sleep -Seconds 3
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/cars" -UseBasicParsing -TimeoutSec 5
    $data = $r.Content | ConvertFrom-Json
    Log "[OK] API verified: $($data.data.Count) cars returned"
} catch {
    Log "[WARN] API verification timed out"
}

$Stopwatch.Stop()
Log "========================================="
Log "  All services started! ($($Stopwatch.Elapsed.TotalSeconds.ToString('0.0'))s)"
Log "========================================="
Write-Host ""
Write-Host "  Client: http://localhost:3000"  -ForegroundColor Green
Write-Host "  Admin:  http://localhost:5173"  -ForegroundColor Green
Write-Host "  API:    http://localhost:8080"  -ForegroundColor Green
Write-Host "  Log:    $LogFile"               -ForegroundColor Gray
Write-Host ""
Write-Host "  Stop: .\stop-all.ps1"           -ForegroundColor Gray
Write-Host ""
