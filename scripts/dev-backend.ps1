# 本地后端开发启动（读取项目根目录 .env）
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
if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "dev" }
if (-not $env:SERVER_PORT) { $env:SERVER_PORT = "8081" }
Set-Location (Join-Path $root "backend")
.\mvnw.cmd spring-boot:run
