@echo off
echo Uninstalling slideOS Keyboard as system app...
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell pm uninstall com.slideos.system

echo Installing slideOS Keyboard as user app...
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r leankeykeyboard\build\outputs\apk\playstore\debug\slideOSSystem_v6.1.32_playstore_debug.apk

echo Restarting the app...
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.slideos.system/com.liskovsoft.leankeyboard.activity.settings.SlideOSSettingsActivity

echo Done!
pause 