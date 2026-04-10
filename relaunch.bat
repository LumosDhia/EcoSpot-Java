@echo off
setlocal enabledelayedexpansion

echo.
echo ==========================================
echo    EcoSpot Application Reloader
echo ==========================================
echo.

:: 1. Try to find and kill the running Java application
echo [1/2] Terminating existing instances...

:: Look for MainFX in jcmd output
set PID=
for /f "tokens=1" %%a in ('jcmd ^| findstr "tn.esprit.MainFX"') do (
    set PID=%%a
)

if not "!PID!"=="" (
    echo Found application with PID !PID!. Killing...
    taskkill /F /PID !PID! >nul 2>&1
) else (
    :: Fallback: Kill by specific window title if PID not found
    :: Using the exact title from MainFX.java to avoid killing the IDE
    taskkill /F /FI "WINDOWTITLE eq EcoSpot Desktop App" /T >nul 2>&1
    echo No specifically identified instance found by PID. Attempted kill by window title.
)

:: 2. Start the new instance
echo [2/2] Launching new instance...
echo.

:: Since mvn might not be in the global path, we check for it
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    mvn javafx:run
) else (
    echo ERROR: 'mvn' command not found in PATH.
    echo Please ensure Maven is installed and added to your Environment Variables.
    echo.
    echo Press any key to exit...
    pause >nul
)

endlocal
