@echo off
setlocal enabledelayedexpansion

:: Get the directory where the script is located (Java project root)
set "BASE_DIR=%~dp0"
set "WEB_DIR=%BASE_DIR%..\EcoSpot-Web"

echo ==========================================
echo   EcoSpot - Full Ecosystem Launcher
echo ==========================================
echo.

:: Change directory to Java project root to ensure Maven finds the pom.xml
cd /d "%BASE_DIR%"

:: 1. Start AI Microservices
echo [1/4] Starting AI Microservices...
start "EcoSpot Face ID" /min cmd /c "python face_service.py"
start "EcoSpot Translate" /min cmd /c "python translate_service.py"

:: 2. Start the Symfony Web Server
echo [2/4] Starting Symfony Web Server...
if exist "%WEB_DIR%\bin\console" (
    start "EcoSpot Symfony Web" /min cmd /c "cd /d "%WEB_DIR%" && symfony server:start"
) else (
    echo [!] Symfony project not found in %WEB_DIR%.
)

:: Give background services a few seconds to initialize
echo.
echo Waiting for services to initialize...
timeout /t 3 /nobreak

:: 3. Start the Java Application
echo.
echo [3/4] Launching EcoSpot Java Application...
echo.

if exist "mvnw.cmd" (
    call mvnw.cmd javafx:run
) else (
    mvn javafx:run
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Java application exited with error code %ERRORLEVEL%.
    pause
)

echo.
echo All services stopped.
endlocal
