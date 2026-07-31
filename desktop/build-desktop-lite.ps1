$Script = Join-Path $PSScriptRoot "build-desktop.ps1"
& $Script -Flavor lite @args
