param(
  [ValidateSet("lite", "full")]
  [string]$Flavor = "full",
  [string]$Maven = "D:\workSoft\envs\apache-maven-3.9.9\bin\mvn.cmd",
  [string]$Jlink = "D:\workSoft\envs\java\jdk-17.0.14\bin\jlink.exe"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Frontend = Join-Path $Root "frontend"
$Backend = Join-Path $Root "backend"
$StaticDir = Join-Path $Backend "src\main\resources\static"
$DesktopBackend = Join-Path $PSScriptRoot "resources\backend"
$DesktopRuntime = Join-Path $PSScriptRoot "resources\runtime"

function Invoke-Step {
  param(
    [scriptblock]$Command,
    [string]$Name
  )
  & $Command
  if ($LASTEXITCODE -ne 0) {
    throw "$Name failed with exit code $LASTEXITCODE"
  }
}

if (!(Test-Path $Maven)) {
  $Maven = "mvn.cmd"
}

Write-Host "Building $Flavor Windows installer..."

Write-Host "Building Vue frontend..."
Push-Location $Frontend
if (!(Test-Path "node_modules")) {
  Invoke-Step { npm.cmd install } "Frontend npm install"
}
Invoke-Step { npm.cmd run build } "Frontend build"
Pop-Location

Write-Host "Copying frontend dist into Spring Boot static resources..."
if (Test-Path $StaticDir) {
  Remove-Item -LiteralPath $StaticDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $StaticDir | Out-Null
Copy-Item -Path (Join-Path $Frontend "dist\*") -Destination $StaticDir -Recurse -Force

Write-Host "Building Spring Boot jar with desktop profile support..."
Push-Location $Backend
$MavenArgs = @()
if (Test-Path "settings.xml") {
  $MavenArgs += @("-s", "settings.xml")
}
$MavenArgs += @("clean", "package", "-DskipTests")
Invoke-Step { & $Maven @MavenArgs } "Backend package"
Pop-Location

Write-Host "Staging backend jar for Electron..."
New-Item -ItemType Directory -Force -Path $DesktopBackend | Out-Null
Copy-Item -Path (Join-Path $Backend "target\job-tracker-0.0.1-SNAPSHOT.jar") -Destination (Join-Path $DesktopBackend "app.jar") -Force

if ($Flavor -eq "full") {
  Write-Host "Preparing bundled Java runtime..."
  if (!(Test-Path $Jlink)) {
    $Jlink = "jlink.exe"
  }
  if (Test-Path $DesktopRuntime) {
    Remove-Item -LiteralPath $DesktopRuntime -Recurse -Force
  }
  $RuntimeModules = "java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.charsets,jdk.crypto.ec,jdk.unsupported,jdk.zipfs"
  Invoke-Step { & $Jlink --strip-debug --no-header-files --no-man-pages --compress=2 --add-modules $RuntimeModules --output $DesktopRuntime } "Bundled Java runtime"
} else {
  Write-Host "Skipping bundled Java runtime for lite package..."
}

Write-Host "Installing Electron dependencies and packaging Windows installer..."
Push-Location $PSScriptRoot
$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR = "https://npmmirror.com/mirrors/electron-builder-binaries/"
$env:npm_config_registry = "https://registry.npmmirror.com"
if (!(Test-Path "node_modules")) {
  Invoke-Step { npm.cmd install } "Desktop npm install"
}
Invoke-Step { npm.cmd run "dist:$Flavor" } "Electron $Flavor package"
Pop-Location

Write-Host "Done. $Flavor installer output is under: $PSScriptRoot\release"
