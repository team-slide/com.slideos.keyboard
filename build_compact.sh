#!/bin/bash

# Build script for LeanKeyboard Compact (iPod Classic-style)
# Optimized for Innioasis Y1 (480x360, 150MB RAM, Android 4.2.2)

echo "=== LeanKeyboard Compact Build Script ==="
echo "Target: Innioasis Y1 (Android 4.2.2, API 17)"
echo ""

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo "Error: build.gradle not found. Please run this script from the project root."
    exit 1
fi

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build the APK
echo "Building APK..."
./gradlew assembleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Find the APK
    APK_PATH=$(find . -name "*.apk" -path "*/build/outputs/apk/release/*" | head -1)
    
    if [ -n "$APK_PATH" ]; then
        echo "APK found at: $APK_PATH"
        
        # Check APK size
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "APK size: $APK_SIZE"
        
        # Check if device is connected
        echo "Checking for connected devices..."
        adb devices | grep -q "device$"
        
        if [ $? -eq 0 ]; then
            echo "Device found! Installing APK..."
            adb install -r "$APK_PATH"
            
            if [ $? -eq 0 ]; then
                echo "Installation successful!"
                echo ""
                echo "=== Installation Complete ==="
                echo "The compact keyboard has been installed on your device."
                echo "To enable it:"
                echo "1. Go to Settings > Language & Input"
                echo "2. Select 'LeanKeyboard' as your default keyboard"
                echo "3. Test the keyboard in any text field"
                echo ""
                echo "Keyboard controls:"
                echo "- DPAD_LEFT/RIGHT: Navigate through characters"
                echo "- DPAD_UP/DOWN: Switch character sets (Letters/Numbers/Symbols/Special)"
                echo "- ENTER: Select current character"
                echo "- BACK: Dismiss keyboard"
                echo "- DEL: Delete last character"
                echo "- SPACE: Add space"
            else
                echo "Installation failed!"
                exit 1
            fi
        else
            echo "No device connected. Please connect your device and run:"
            echo "adb install -r $APK_PATH"
        fi
    else
        echo "Error: APK not found!"
        exit 1
    fi
else
    echo "Build failed!"
    exit 1
fi 