@echo off
REM Build script for LeanKeyboard Compact (iPod Classic-style)
REM Optimized for Innioasis Y1 (480x360, 150MB RAM, Android 4.2.2)

echo === LeanKeyboard Compact Build Script ===
echo Target: Innioasis Y1 (Android 4.2.2, API 17)
echo.

REM Check if we're in the right directory
if not exist "build.gradle" (
    echo Error: build.gradle not found. Please run this script from the project root.
    pause
    exit /b 1
)

REM Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean

REM Build the APK
echo Building APK...
call gradlew.bat assembleRelease

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    
    REM Find the APK
    for /r "leankeykeyboard\build\outputs\apk\release" %%i in (*.apk) do (
        set APK_PATH=%%i
        goto :found_apk
    )
    
    :found_apk
    if defined APK_PATH (
        echo APK found at: %APK_PATH%
        
        REM Check APK size
        for %%A in ("%APK_PATH%") do set APK_SIZE=%%~zA
        echo APK size: %APK_SIZE% bytes
        
        REM Check if device is connected
        echo Checking for connected devices...
        adb devices | findstr "device$" >nul
        
        if %ERRORLEVEL% EQU 0 (
            echo Device found! Installing APK...
            adb install -r "%APK_PATH%"
            
            if %ERRORLEVEL% EQU 0 (
                echo Installation successful!
                echo.
                echo === Installation Complete ===
                echo The compact keyboard has been installed on your device.
                echo To enable it:
                echo 1. Go to Settings ^> Language ^& Input
                echo 2. Select 'LeanKeyboard' as your default keyboard
                echo 3. Test the keyboard in any text field
                echo.
                echo Keyboard controls:
                echo - DPAD_LEFT/RIGHT: Navigate through characters
                echo - DPAD_UP/DOWN: Switch character sets (Letters/Numbers/Symbols/Special)
                echo - ENTER: Select current character
                echo - BACK: Dismiss keyboard
                echo - DEL: Delete last character
                echo - SPACE: Add space
            ) else (
                echo Installation failed!
                pause
                exit /b 1
            )
        ) else (
            echo No device connected. Please connect your device and run:
            echo adb install -r "%APK_PATH%"
        )
    ) else (
        echo Error: APK not found!
        pause
        exit /b 1
    )
) else (
    echo Build failed!
    pause
    exit /b 1
)

pause 