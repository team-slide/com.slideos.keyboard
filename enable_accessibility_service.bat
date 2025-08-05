@echo off
echo Enabling slideOS System Status Bar Accessibility Service...
echo.

echo 1. Opening Accessibility Settings...
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -a android.settings.ACCESSIBILITY_SETTINGS

echo.
echo 2. Please manually enable the "slideOS System Status Bar" service in the accessibility settings.
echo    - Look for "slideOS System Status Bar" in the list
echo    - Tap on it and enable it
echo    - Grant any required permissions

echo.
echo 3. Testing the system status bar...
echo    - The status bar should now appear over all apps
echo    - It will show the current app name, time, battery, Wi-Fi, and Bluetooth status
echo    - It will automatically hide in full-screen apps

echo.
echo 4. Testing keyboard navigation...
echo    - Press Menu button to see the keyboard toggle toast
echo    - The status bar title should update when navigating between different screens

echo.
pause 