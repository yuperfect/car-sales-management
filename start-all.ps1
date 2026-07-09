<#
╔══════════════════════════════════════════════════════════════╗
║      汽车销售管理系统 — 一键启动脚本                        ║
║      启动顺序: MySQL → 后端 → 客户端 → 管理端              ║
╚══════════════════════════════════════════════════════════════╝
#>

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendPath = "$ProjectRoot\code\car-sales-backend"
$ClientPath  = "$ProjectRoot\code\car-sales-client"
$AdminPath   = "$ProjectRoot\code\car-sales-admin"

$LogFile = "$ProjectRoot\start-log.txt"
$Stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

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
    param([int]$Port, [int]$TimeoutSeconds = 30, [string]$ServiceName)
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (CheckPort -Port $Port) {
            Log "✅ $ServiceName 已就绪 (端口 $Port)"
            return $true
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    Log "❌ $ServiceName 启动超时 (${TimeoutSeconds}s)"
    return $false
}

# ========== 清理上次日志 ==========
Remove-Item -Path $LogFile -ErrorAction SilentlyContinue

Log "========================================"
Log "  汽车销售管理系统 — 一键启动"
Log "========================================"

# ========== Step 0: 检查 MySQL ==========
Log "正在检查 MySQL 服务..."
$mysqlService = Get-Service MySQL80 -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -ne 'Running') {
    Log "MySQL 未运行，正在启动..."
    Start-Service MySQL80
    Start-Sleep -Seconds 3
}
if (CheckPort -Port 3306) {
    Log "✅ MySQL 已运行 (端口 3306)"
} else {
    Log "⚠️ 请手动启动 MySQL 服务 (端口 3306)"
    Write-Host "  运行: Start-Service MySQL80" -ForegroundColor Yellow
}

# ========== Step 1: 编译后端 ==========
Log "正在编译后端..."
Push-Location $BackendPath
$env:MAVEN_OPTS = "-Dmaven.multiModuleProjectDirectory=$BackendPath"
$buildResult = cmd /c "mvnw.cmd package -DskipTests -q 2>&1"
if ($LASTEXITCODE -eq 0) {
    Log "✅ 后端编译成功"
} else {
    Log "❌ 后端编译失败"
    Write-Host "  编译错误信息:" -ForegroundColor Red
    Write-Host "  $buildResult" -ForegroundColor Red
    Pop-Location
    exit 1
}

# ========== Step 2: 启动后端 ==========
Log "正在启动后端服务 (端口 8080)..."
# 先停掉旧的进程
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

$jarFile = "$BackendPath\target\car-sales-backend-1.0.0.jar"
if (Test-Path $jarFile) {
    Start-Process -FilePath "java" -ArgumentList "-jar",$jarFile -WindowStyle Normal
    $backendOk = WaitForPort -Port 8080 -ServiceName "后端 API"
    if (-not $backendOk) {
        Pop-Location
        exit 1
    }
} else {
    Log "❌ JAR 包未找到: $jarFile"
    Pop-Location
    exit 1
}
Pop-Location

# ========== Step 3: 启动客户端 ==========
Log "正在启动客户端前端 (端口 3000)..."
Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
}
Start-Process -FilePath "cmd" -ArgumentList "/c","npm","run","dev" -WorkingDirectory $ClientPath

# ========== Step 4: 启动管理端 ==========
Log "正在启动管理端前端 (端口 5173)..."
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
}
Start-Process -FilePath "cmd" -ArgumentList "/c","npm","run","dev" -WorkingDirectory $AdminPath

# ========== 等待前端就绪 ==========
Start-Sleep -Seconds 5

$clientOk = $false
$adminOk = $false
$elapsed = 0
while ($elapsed -lt 15) {
    if (-not $clientOk) { $clientOk = CheckPort -Port 3000 }
    if (-not $adminOk)  { $adminOk  = CheckPort -Port 5173 }
    if ($clientOk -and $adminOk) { break }
    Start-Sleep -Seconds 2
    $elapsed += 2
}

if ($clientOk) { Log "✅ 客户端前端已就绪 (端口 3000)" }
else           { Log "⚠️ 客户端前端可能未完全启动" }

if ($adminOk)  { Log "✅ 管理端前端已就绪 (端口 5173)" }
else           { Log "⚠️ 管理端前端可能未完全启动" }

# ========== 验证 API ==========
Start-Sleep -Seconds 2
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/cars" -UseBasicParsing -TimeoutSec 5
    $data = $r.Content | ConvertFrom-Json
    Log "✅ 后端 API 验证: 返回 $($data.data.Count) 辆车"
} catch {
    Log "⚠️ 后端 API 验证超时，请稍后手动检查"
}

# ========== 完成 ==========
$Stopwatch.Stop()
Log "========================================"
Log "  🚀 全部启动完成! (耗时 $($Stopwatch.Elapsed.TotalSeconds.ToString('0.0')) 秒)"
Log "========================================"
Write-Host ""
Write-Host "  🌐 客户端: http://localhost:3000"   -ForegroundColor Green
Write-Host "  🌐 管理端: http://localhost:5173"   -ForegroundColor Green
Write-Host "  🔗 后端 API: http://localhost:8080" -ForegroundColor Green
Write-Host ""
Write-Host "  📋 日志文件: $LogFile" -ForegroundColor Gray
Write-Host ""

# ========== 测试 API ==========
Write-Host "  接口快速测试:" -ForegroundColor Yellow
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/cars/1" -UseBasicParsing -TimeoutSec 3
    Write-Host "  ✅ GET /api/cars/1 — OK" -ForegroundColor Green
} catch {
    Write-Host "  ❌ GET /api/cars/1 — 失败" -ForegroundColor Red
}
try {
    $r = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 3
    Write-Host "  ✅ 客户端首页可访问" -ForegroundColor Green
} catch {
    Write-Host "  ❌ 客户端首页不可访问" -ForegroundColor Red
}
try {
    $r = Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing -TimeoutSec 3
    Write-Host "  ✅ 管理端首页可访问" -ForegroundColor Green
} catch {
    Write-Host "  ❌ 管理端首页不可访问" -ForegroundColor Red
}
Write-Host ""
Write-Host "  ⏹ 关闭所有服务请运行: .\stop-all.ps1" -ForegroundColor Gray
