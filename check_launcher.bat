@echo off
echo Checking slideOS System app launcher status...
echo.

echo 1. Checking if app is installed:
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell pm list packages | findstr slideos
echo.

echo 2. Checking if app can be launched directly:
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.slideos.system/com.liskovsoft.leankeyboard.activity.settings.SlideOSSettingsActivity
echo.

echo 3. Clearing launcher cache to refresh app list:
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell pm clear com.android.launcher
echo.

echo 4. Restarting launcher:
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am force-stop com.android.launcher
echo.

echo Troubleshooting steps if app still doesn't appear:
echo - Check your device's app drawer or launcher
echo - Look for "slideOS System" in the app list
echo - Try searching for "slideOS" or "System" in the launcher search
echo - If using Android TV, check the app row
echo - Try rebooting the device
echo.

echo The app should now appear as "slideOS System" in your launcher.
echo If it still doesn't appear, try rebooting your device.
pause 