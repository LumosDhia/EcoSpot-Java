@echo off
echo Terminating existing EcoSpot instances...
taskkill /F /IM java.exe /T 2>nul

set "MVN_PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

if exist "%MVN_PATH%" (
    echo Launching EcoSpot using IntelliJ Maven...
    "%MVN_PATH%" javafx:run
) else (
    echo Error: Maven not found at %MVN_PATH%
    echo Trying system mvn...
    mvn javafx:run
)
pause
