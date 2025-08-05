@echo off
echo Setting device time using ADB with root access...

REM Get current time in format YYYYMMDD.HHMMSS
for /f "tokens=1-6 delims=/: " %%a in ('echo %date% %time%') do (
    set year=%%c
    set month=%%a
    set day=%%b
    set hour=%%d
    set minute=%%e
    set second=%%f
)

REM Format the date and time
set formatted_date=%year%%month%%day%
set formatted_time=%hour%:%minute%:%second%

echo Current time: %formatted_date%.%formatted_time%

REM Set the device time using ADB with root access
"C:\Users\itsry\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell "su -c 'date -s %formatted_date%.%formatted_time%'"

echo Time set successfully!
pause 