@echo off
REM Quick test build script to verify memory settings

echo 🧪 Testing build with memory optimizations...

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found. Please ensure you're in the project root directory.
    exit /b 1
)

echo 📋 Current Gradle settings:
if exist "gradle.properties" (
    echo ✅ gradle.properties found
    findstr "org.gradle.jvmargs" gradle.properties >nul || echo ⚠️  JVM args not found
    findstr "org.gradle.parallel" gradle.properties >nul || echo ⚠️  Parallel builds not configured
    findstr "org.gradle.caching" gradle.properties >nul || echo ⚠️  Caching not configured
) else (
    echo ❌ gradle.properties not found
)

echo.
echo 🔧 Starting test build...

REM Stop any existing daemon to ensure fresh start with new memory settings
call gradlew.bat --stop

echo 🧹 Cleaning project...
call gradlew.bat clean

echo 📦 Building debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    echo ✅ Build successful with memory optimizations!
    echo.
    echo 📊 Build artifacts:
    dir app\build\outputs\apk\debug\ 2>nul || echo No APK found
) else (
    echo ❌ Build failed. Check the output above for errors.
    exit /b 1
)
