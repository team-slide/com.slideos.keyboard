# LeanKeyboard Compact - iPod Classic Style

## Overview

This is a refactored version of LeanKeyboard optimized for the Innioasis Y1 device (480x360 display, 150MB RAM, Android 4.2.2). The keyboard has been redesigned to mimic the iPod Classic's compact, efficient input method.

## Key Features

### iPod Classic-Style UI
- **Single horizontal character bar** at the bottom of the screen
- **White pill-shaped input field** to the left showing current text
- **Alphabetical character arrangement** (A-Z) for letters
- **Interchangeable character sets**: Letters → Numbers → Symbols → Special characters
- **Compact footprint** - minimal screen space usage

### Memory Optimized (150MB RAM Target)
- **Object pooling** - reuse Paint objects, avoid allocations in onDraw()
- **Primitive arrays** - use int[] instead of ArrayList<Integer>
- **Minimal view hierarchy** - flat structure for better performance
- **Efficient canvas operations** - direct drawing, no intermediate objects
- **Aggressive ProGuard/R8** - strip unused code to stay under 20MB APK

### Scrollwheel Input Mapping
- **DPAD_LEFT/RIGHT**: Navigate through characters
- **DPAD_UP/DOWN**: Switch character sets (Letters/Numbers/Symbols/Special)
- **ENTER**: Select current character
- **BACK**: Dismiss keyboard
- **DEL**: Delete last character
- **SPACE**: Add space

### Character Sets
- **Letters**: A-Z (26 characters)
- **Numbers**: 0-9 (10 characters)
- **Symbols**: Common symbols (!@#$%^&*()_+-=[]{}|;':",./<>?)
- **Special**: Extended symbols, currency, etc.

## Setup Instructions

### Prerequisites
1. **Java Development Kit (JDK) 8 or 11**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Or use OpenJDK: https://adoptopenjdk.net/

2. **Android SDK**
   - Install Android Studio (recommended)
   - Or download command line tools: https://developer.android.com/studio#command-tools

### Quick Setup
1. Run the setup script:
   ```
   .\setup_environment.bat
   ```

2. Build and install:
   ```
   .\build_compact.bat
   ```

### Manual Setup
If the setup script doesn't work:

1. **Install Java JDK 8 or 11**
   - Download and install to default location
   - Ensure `java` command is available in PATH

2. **Install Android SDK**
   - Install Android Studio (easiest option)
   - Or extract command line tools to `C:\Android\Sdk`

3. **Build the project**
   ```
   .\gradlew.bat assembleRelease
   ```

4. **Install on device**
   ```
   adb install -r leankeykeyboard\build\outputs\apk\release\*.apk
   ```

## Usage

### Enable the Keyboard
1. Go to Settings > Language & Input
2. Select 'LeanKeyboard' as your default keyboard
3. Test the keyboard in any text field

### Keyboard Controls
- **DPAD_LEFT/RIGHT**: Scroll through characters in the current set
- **DPAD_UP/DOWN**: Switch between character sets
- **ENTER**: Select the highlighted character
- **BACK**: Dismiss the keyboard
- **DEL**: Delete the last character typed
- **SPACE**: Add a space character

### Character Set Navigation
- **Letters** (A-Z): Default set for general typing
- **Numbers** (0-9): For numeric input
- **Symbols**: Common punctuation and symbols
- **Special**: Extended symbols and special characters

## Technical Details

### Architecture
- **CompactKeyboardView**: Custom view implementing the iPod Classic-style UI
- **CompactKeyboardController**: Controller integrating with the IME service
- **LeanbackImeService**: Modified to use the compact keyboard

### Memory Optimization
- **Object pooling**: Reuse Paint and Rect objects
- **Minimal allocations**: Avoid creating objects in drawing loops
- **Efficient rendering**: Single-pass canvas operations
- **Compact data structures**: Use primitive arrays where possible

### Android 4.2.2 Compatibility
- **API Level 17**: Uses only compatible APIs
- **No AndroidX**: Uses old support libraries or pure framework
- **Dalvik VM**: Optimized for the older runtime
- **Basic animations**: Simple ViewPropertyAnimator usage

## Files Modified/Created

### New Files
- `CompactKeyboardView.java` - Main keyboard view implementation
- `CompactKeyboardController.java` - Controller for IME integration
- `input_compact.xml` - Layout for the compact keyboard
- `build_compact.bat` - Build automation script
- `setup_environment.bat` - Environment setup script

### Modified Files
- `LeanbackImeService.java` - Updated to use compact keyboard
- `dimens.xml` - Added compact keyboard dimensions
- `integers.xml` - Added visible characters count

## Troubleshooting

### Build Issues
- **"JAVA_HOME is not set"**: Install Java JDK and ensure it's in PATH
- **"Android SDK not found"**: Install Android Studio or SDK tools
- **Gradle errors**: Run `.\gradlew.bat clean` and try again

### Installation Issues
- **"Device not found"**: Ensure device is connected via USB with ADB enabled
- **"Installation failed"**: Check if device has enough storage space
- **"App not installed"**: Try uninstalling previous version first

### Keyboard Issues
- **Keyboard not appearing**: Check if it's set as default in Settings
- **Keys not responding**: Ensure device has proper d-pad/scrollwheel input
- **Characters not typing**: Check input connection in the app

## Performance Notes

The compact keyboard is designed to be extremely lightweight:
- **APK size**: Target < 20MB
- **Memory usage**: < 10MB during operation
- **CPU usage**: Minimal, optimized for 1.2GHz dual-core
- **Battery impact**: Negligible, no background processing

## Future Enhancements

Potential improvements for future versions:
- **Custom themes**: Different color schemes
- **Character set customization**: User-defined character sets
- **Input prediction**: Basic word completion
- **Haptic feedback**: Vibration on character selection
- **Sound effects**: Audio feedback for interactions 