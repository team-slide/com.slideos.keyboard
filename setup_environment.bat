@echo off
REM Setup script for LeanKeyboard Compact development environment
REM This script will help set up Java and Android SDK for building

echo === LeanKeyboard Compact Environment Setup ===
echo.

REM Check if Java is already installed
java -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Java is already installed and available.
    goto :check_android
)

echo Java is not installed or not in PATH.
echo.
echo Please install Java Development Kit (JDK) 8 or 11:
echo.
echo Option 1: Download from Oracle
echo   - Go to: https://www.oracle.com/java/technologies/downloads/
echo   - Download JDK 8 or 11 for Windows x64
echo   - Install it to the default location
echo.
echo Option 2: Download from AdoptOpenJDK
echo   - Go to: https://adoptopenjdk.net/
echo   - Download OpenJDK 8 or 11 for Windows x64
echo   - Install it to the default location
echo.
echo Option 3: Use Chocolatey (if installed)
echo   - Run: choco install openjdk11
echo.
echo After installing Java, run this script again.
echo.
pause
exit /b 1

:check_android
echo Checking Android SDK...

REM Check for Android SDK
if exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
    echo Android SDK found at: %USERPROFILE%\AppData\Local\Android\Sdk
    set ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk
) else if exist "C:\Android\Sdk" (
    echo Android SDK found at: C:\Android\Sdk
    set ANDROID_HOME=C:\Android\Sdk
) else (
    echo Android SDK not found.
    echo.
    echo Please install Android Studio or Android SDK:
    echo.
    echo Option 1: Install Android Studio
    echo   - Go to: https://developer.android.com/studio
    echo   - Download and install Android Studio
    echo   - It will install the SDK automatically
    echo.
    echo Option 2: Install Android SDK Command Line Tools
    echo   - Go to: https://developer.android.com/studio#command-tools
    echo   - Download the command line tools
    echo   - Extract to C:\Android\Sdk
    echo.
    echo After installing Android SDK, run this script again.
    echo.
    pause
    exit /b 1
)

echo.
echo === Environment Setup Complete ===
echo.
echo Java and Android SDK are ready.
echo You can now run: .\build_compact.bat
echo.
pause 