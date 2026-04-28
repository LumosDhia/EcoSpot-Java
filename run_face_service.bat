@echo off
setlocal
echo ==========================================
echo   EcoSpot Face Recognition Service
echo ==========================================

:: Check if native python is available
python --version >nul 2>&1
if %errorlevel% neq 0 (
    py --version >nul 2>&1
    if %errorlevel% neq 0 (
        :: Try WSL fallback
        wsl --list >nul 2>&1
        if %errorlevel% equ 0 (
            echo [INFO] Native Python not found, using WSL fallback...
            :: Convert current path to WSL path
            set "WSL_PATH=/mnt/c%~p0"
            set "WSL_PATH=!WSL_PATH:\=/!"
            
            wsl bash -c "cd /mnt/c/Users/wiem/Desktop/EcoSpot-Java/face_service && python3 -m pip install -r requirements.txt && python3 main.py"
            if %errorlevel% neq 0 (
                echo [ERROR] Failed to start service in WSL. Ensure python3-pip is installed.
                pause
                exit /b 1
            )
            exit /b 0
        )
        echo [ERROR] Python not found. Please install Python 3.9+ or WSL Ubuntu.
        pause
        exit /b 1
    ) else (
        set PY_CMD=py
    )
) else (
    set PY_CMD=python
)

if defined PY_CMD (
    echo [INFO] Using %PY_CMD%...
    cd face_service
    %PY_CMD% -m pip install -r requirements.txt
    %PY_CMD% main.py
)

pause
