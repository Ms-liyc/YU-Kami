# 本地前端开发启动（API 代理到后端 8081）
$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot ".."
$envFile = Join-Path $root ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
        $pair = $_ -split '=', 2
        if ($pair.Length -eq 2) {
            Set-Item -Path "Env:$($pair[0].Trim())" -Value $pair[1].Trim()
        }
    }
}
$port = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8081" }
$env:VITE_API_PROXY = "http://localhost:$port"
Set-Location (Join-Path $root "frontend")
if (-not (Test-Path "node_modules")) { npm install }
npm run dev
