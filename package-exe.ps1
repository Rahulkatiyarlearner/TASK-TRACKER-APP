$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$jar = Join-Path $root "task-tracker-app.jar"
if (-not (Test-Path $jar)) {
    Write-Error "Missing $jar. Run .\build.ps1 first."
}

function Get-JpackageExe {
    $javaProps = cmd /c "java -XshowSettings:properties -version 2>&1"
    $javaHomeLine = ($javaProps | Select-String -Pattern '^\s*java\.home\s*=' | Select-Object -First 1).Line
    if (-not $javaHomeLine) {
        Write-Error "Could not read java.home from java -XshowSettings:properties"
    }
    $javaHome = ($javaHomeLine -split '=', 2)[1].Trim()
    $jp = Join-Path $javaHome "bin\jpackage.exe"
    if (-not (Test-Path $jp)) {
        Write-Error "jpackage.exe not found at $jp. Install a full JDK (Java 14+) with jpackage."
    }
    return $jp
}

$jpackage = Get-JpackageExe
$inputDir = Join-Path $root "jpackage-input"
$outDir = Join-Path $root "dist"

New-Item -ItemType Directory -Force $inputDir | Out-Null
Copy-Item $jar (Join-Path $inputDir "task-tracker-app.jar") -Force

if (Test-Path $outDir) {
    Remove-Item $outDir -Recurse -Force
}

& $jpackage `
    --type app-image `
    --input $inputDir `
    --main-jar task-tracker-app.jar `
    --name TaskTracker `
    --app-version 1.0.0 `
    --vendor "Task Tracker" `
    --dest $outDir `
    --java-options "-Dfile.encoding=UTF-8"

$exe = Join-Path $outDir "TaskTracker\TaskTracker.exe"
if (-not (Test-Path $exe)) {
    Write-Error "Expected launcher not found: $exe"
}
Write-Host "Runnable app: $exe"
