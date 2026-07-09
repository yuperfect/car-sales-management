<#
╔══════════════════════════════════════════════════════════════╗
║      汽车销售管理系统 — 状态查看脚本                        ║
╚══════════════════════════════════════════════════════════════╝
#>

Write-Host ""
Write-Host "📊 系统运行状态" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

function CheckService {
    param([string]$Name, [int]$Port, [string]$Url)
    $portOk = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    if (-not $portOk) {
        Write-Host "  ❌ $Name — 端口 $Port 未监听" -ForegroundColor Red
        return
    }
    if ($Url) {
        try {
            $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
            Write-Host "  ✅ $Name — 端口 $Port (HTTP $($r.StatusCode))" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠️ $Name — 端口 $Port (HTTP 无响应)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✅ $Name — 端口 $Port" -ForegroundColor Green
    }
}

CheckService -Name "MySQL"          -Port 3306
CheckService -Name "后端 API"       -Port 8080 -Url "http://localhost:8080/api/cars/1"
CheckService -Name "客户端前端"     -Port 3000 -Url "http://localhost:3000"
CheckService -Name "管理端前端"     -Port 5173 -Url "http://localhost:5173"

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""

# 如果后端在运行，显示车辆数量
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/cars" -UseBasicParsing -TimeoutSec 3
    $data = $r.Content | ConvertFrom-Json
    Write-Host "  车辆总数: $($data.data.Count) 辆" -ForegroundColor Gray
} catch {}

Write-Host "  启动: .\start-all.ps1"  -ForegroundColor Gray
Write-Host "  停止: .\stop-all.ps1"   -ForegroundColor Gray
Write-Host ""
