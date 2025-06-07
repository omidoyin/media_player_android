@echo off
REM Android Media Player Build and Test Script for Windows

echo 🎵 Building Android Media Player with Lyrics Display...
echo 📋 Using Gradle 8.9 with Kotlin 1.9.20 + KSP

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found. Please ensure you're in the project root directory.
    exit /b 1
)

echo 📦 Cleaning project...
call gradlew.bat clean

echo 🔧 Building project...
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    echo ✅ Build successful!

    echo 🧪 Running unit tests...
    call gradlew.bat testDebugUnitTest

    if %ERRORLEVEL% equ 0 (
        echo ✅ All tests passed!
        echo.
        echo 🎉 Android Media Player is ready!
        echo.
        echo 📱 To install on device/emulator:
        echo    gradlew.bat installDebug
        echo.
        echo 🔍 To run tests:
        echo    gradlew.bat test
        echo.
        echo 📊 To generate test report:
        echo    gradlew.bat testDebugUnitTest --continue
        echo    Open: app\build\reports\tests\testDebugUnitTest\index.html
        echo.
        echo 🚀 Features included:
        echo    ✓ Audio ^& Video playback
        echo    ✓ Playlists management
        echo    ✓ Favorites system
        echo    ✓ Search functionality
        echo    ✓ Lyrics display with time-sync
        echo    ✓ Shuffle ^& Repeat modes
        echo    ✓ Speed control ^(0.5x - 2.0x^)
        echo    ✓ Audio-only mode for videos
        echo    ✓ Background playback
        echo    ✓ Modern Material 3 UI
        echo    ✓ Clean Architecture ^(MVVM^)
        echo.
    ) else (
        echo ❌ Some tests failed. Check the output above.
        exit /b 1
    )
) else (
    echo ❌ Build failed. Check the output above.
    exit /b 1
)
