@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "DRY_RUN=0"
if /I "%~1"=="--dry-run" set "DRY_RUN=1"

echo.
echo ==========================================
echo   EcoSpot Unified Relaunch
echo ==========================================
echo.

:: 1) Start Face Recognition Service if needed
echo [1/3] Checking Face Recognition Service...
netstat -ano | findstr :8001 >nul
if %errorlevel% neq 0 (
    echo [INFO] Face Service is not running. Starting it in a new window...
    start "EcoSpot Face Service" /min cmd /c run_face_service.bat
    timeout /t 2 >nul
) else (
    echo [INFO] Face Service is already running on port 8001.
)

:: 2) Kill only EcoSpot Java processes (faster + safer)
echo [2/3] Terminating existing instances...
set "KILLED=0"

for /f %%p in ('powershell -NoProfile -Command "Get-CimInstance Win32_Process | Where-Object { ($_.Name -eq 'java.exe' -or $_.Name -eq 'javaw.exe') -and ($_.CommandLine -like '*Ecospot-Java*' -or $_.CommandLine -like '*tn.esprit.MainFX*' -or $_.CommandLine -like '*javafx:run*') } | Select-Object -ExpandProperty ProcessId"') do (
    set "KILLED=1"
    if "!DRY_RUN!"=="1" (
        echo [dry-run] Would kill PID %%p
    ) else (
        taskkill /F /PID %%p >nul 2>&1
        echo Killed PID %%p
    )
)

if "!KILLED!"=="0" echo No running EcoSpot instance detected.

:: 3) Launch with fastest available Maven command
echo [3/3] Launching new instance...
echo.

set "MVN_CMD="
if exist "%~dp0mvnw.cmd" set "MVN_CMD=%~dp0mvnw.cmd"
if not defined MVN_CMD (
    where mvn >nul 2>&1
    if !ERRORLEVEL! EQU 0 set "MVN_CMD=mvn"
)
if not defined MVN_CMD (
    for /d %%d in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
        if exist "%%d\plugins\maven\lib\maven3\bin\mvn.cmd" set "MVN_CMD=%%d\plugins\maven\lib\maven3\bin\mvn.cmd"
    )
)

if not defined MVN_CMD (
    echo ERROR: Maven not found. Install Maven or add it to PATH.
    exit /b 1
)

echo Using Maven: %MVN_CMD%
if "!DRY_RUN!"=="1" (
    echo [dry-run] Would run: "%MVN_CMD%" -DskipTests javafx:run
    exit /b 0
)

"%MVN_CMD%" -DskipTests javafx:run

endlocal
