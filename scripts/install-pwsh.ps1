$ErrorActionPreference = "Stop"

$version = "7.5.4"
$msiName = "PowerShell-$version-win-x64.msi"
$msiPath = Join-Path $env:TEMP $msiName
$url = "https://github.com/PowerShell/PowerShell/releases/download/v$version/$msiName"

Write-Host "Downloading $url"
Invoke-WebRequest -Uri $url -OutFile $msiPath

$args = @(
    "/i", "`"$msiPath`"",
    "/qn",
    "/norestart",
    "ADD_PATH=1",
    "REGISTER_MANIFEST=1",
    "ENABLE_PSREMOTING=0",
    "USE_MU=0",
    "ENABLE_MU=0",
    "ALLUSERS=2",
    "MSIINSTALLPERUSER=1"
) -join " "

Write-Host "Installing $msiPath"
$proc = Start-Process -FilePath "msiexec.exe" -ArgumentList $args -Wait -PassThru
Write-Host "msiexec exit code: $($proc.ExitCode)"

if ($proc.ExitCode -ne 0) {
    throw "MSI install failed with exit code $($proc.ExitCode)"
}

Write-Host "PowerShell install completed."
