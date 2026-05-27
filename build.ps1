$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

function Resolve-JdkBin {
    $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacCmd -and (Test-Path (Join-Path (Split-Path $javacCmd.Source) "jar.exe"))) {
        return (Split-Path $javacCmd.Source)
    }

    $jarCmd = Get-Command jar -ErrorAction SilentlyContinue
    $javacCmd2 = Get-Command javac -ErrorAction SilentlyContinue
    if ($jarCmd -and $javacCmd2) {
        $jarDir = Split-Path $jarCmd.Source
        $javacDir = Split-Path $javacCmd2.Source
        if ($jarDir -eq $javacDir) {
            return $jarDir
        }
    }

    # java prints these settings to stderr; use cmd to capture without triggering PS native stderr errors
    $javaProps = cmd /c "java -XshowSettings:properties -version 2>&1"
    $javaHomeLine = ($javaProps | Select-String -Pattern '^\s*java\.home\s*=' | Select-Object -First 1).Line
    if ($javaHomeLine) {
        $javaHome = ($javaHomeLine -split '=', 2)[1].Trim()
        if ($javaHome) {
            $bin = Join-Path $javaHome "bin"
            if ((Test-Path (Join-Path $bin "javac.exe")) -and (Test-Path (Join-Path $bin "jar.exe"))) {
                return $bin
            }
            if ($javaHome -like "*\jre") {
                $parentBin = Join-Path (Split-Path $javaHome -Parent) "bin"
                if ((Test-Path (Join-Path $parentBin "javac.exe")) -and (Test-Path (Join-Path $parentBin "jar.exe"))) {
                    return $parentBin
                }
            }
        }
    }

    Write-Error "Could not locate a JDK bin folder containing javac.exe and jar.exe. Install a JDK (not just a JRE) and ensure java/javac/jar are available."
}

$javaBin = Resolve-JdkBin
$jarExe = Join-Path $javaBin "jar.exe"

New-Item -ItemType Directory -Force lib, build\classes | Out-Null
$gsonJar = Join-Path $root "lib\gson-2.10.1.jar"
if (-not (Test-Path $gsonJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" -OutFile $gsonJar -UseBasicParsing
}

$sources = Get-ChildItem -Path (Join-Path $root "src\main\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& (Join-Path $javaBin "javac.exe") -encoding UTF-8 -d (Join-Path $root "build\classes") -cp $gsonJar @sources

$classes = Join-Path $root "build\classes"
Set-Location $classes
& $jarExe xf $gsonJar
Set-Location $root
$outJar = Join-Path $root "task-tracker-app.jar"
Remove-Item $outJar -Force -ErrorAction SilentlyContinue
& $jarExe --create --file $outJar --main-class com.tasktracker.TaskTrackerApp -C $classes .
Write-Host "Built: $outJar"
