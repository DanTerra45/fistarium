# https://keystore-explorer.org/downloads.html
# VERY IMPORTANT NOTE!
# Dont forget to define the vars in local.properties and NEVER push it accidently into production!
# Follow this steps:
# Create a folder named "keys" and inside it place your keys file
# KEYSTORE_FILE=[your_keystore_file]
# KEYSTORE_PASSWORD=[your_keystore_password]
# KEYSTORE_KEY_ALIAS=[your_keystore_alias]
# KEYSTORE_KEY_PASSWORD=[your_keystore_password]
# -----------------
# Launch this with: 
# powershell -ExecutionPolicy Bypass -File ".\powershell_keystore_definitions.ps1"

param(
    [switch]$NoLaunch,
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\powershell_keystore_defs.ps1 [-NoLaunch] [-Help]"
    Write-Host "  -NoLaunch: Set environment variables without launching Android Studio"
    Write-Host "  -Help: Show this help message"
    exit 0
}

$localPropsPath = "$PSScriptRoot\..\local.properties"

if (-not (Test-Path $localPropsPath)) {
    Write-Warning "local.properties not found at $localPropsPath"
    Write-Host "Please ensure local.properties exists with your keystore configuration"
    exit 1
}

$localProps = Get-Content $localPropsPath | Where-Object { $_ -match '^KEYSTORE_' }

foreach ($line in $localProps) {
    if ($line -match 'KEYSTORE_([^=]+)=(.*)') {
        $key = $matches[1]
        $value = $matches[2]

        if ($key -eq "FILE") {
            $fullPath = "$PSScriptRoot\$value"
            Set-Item -Path "env:KEYSTORE_PATH" -Value $fullPath
            Write-Host "Set KEYSTORE_PATH: $fullPath"
        } else {
            $envVarName = "KEYSTORE_$key"
            Set-Item -Path "env:$envVarName" -Value $value
            Write-Host "Set $envVarName"
        }
    }
}

Write-Host "Environment variables set for this launch."

if (-not $NoLaunch) {
    $studioPath = "bin\studio64.exe"
    $projectPath = "$PSScriptRoot\.."

    if (Test-Path $studioPath) {
        Write-Host "Launching Android Studio from: $studioPath"
        Write-Host "Project: $projectPath"
        & $studioPath $projectPath
    } else {
        Write-Warning "Android Studio not found at: $studioPath"
        Write-Host "Please update the path in this script or launch Android Studio manually."
    }
} else {
    Write-Host "Environment variables set. Launch Android Studio manually to use them."
}