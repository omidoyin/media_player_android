@echo off
REM Quick build test to verify theme fixes

echo 🎨 Testing theme and resource fixes...

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found. Please ensure you're in the project root directory.
    exit /b 1
)

echo 🧹 Cleaning project...
call gradlew.bat clean

echo 🔧 Testing resource compilation...
call gradlew.bat processDebugResources

if %ERRORLEVEL% equ 0 (
    echo ✅ Resources compiled successfully!
    
    echo 📦 Building APK...
    call gradlew.bat assembleDebug
    
    if %ERRORLEVEL% equ 0 (
        echo 🎉 Build successful! Theme issues resolved.
        echo.
        echo 📊 APK created:
        dir app\build\outputs\apk\debug\*.apk 2>nul || echo APK location may vary
    ) else (
        echo ❌ APK build failed. Check output above.
        exit /b 1
    )
) else (
    echo ❌ Resource compilation failed. Check theme/resource files.
    exit /b 1
)
