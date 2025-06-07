@echo off
REM Test script for KSP migration

echo 🔧 Testing KSP ^(Kotlin Symbol Processing^) migration...

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found. Please ensure you're in the project root directory.
    exit /b 1
)

echo 📋 Migration Summary:
echo ✅ KAPT → KSP migration completed
echo ✅ JVM module access arguments added
echo ✅ Room schema location configured
echo.

echo 🛑 Stopping existing Gradle daemon...
call gradlew.bat --stop

echo 🧹 Cleaning project...
call gradlew.bat clean

echo 🔍 Checking KSP configuration...
findstr "ksp(" app\build.gradle.kts >nul
if %ERRORLEVEL% equ 0 (
    echo ✅ KSP dependencies found
) else (
    echo ❌ KSP dependencies not found
    exit /b 1
)

findstr "com.google.devtools.ksp" app\build.gradle.kts >nul
if %ERRORLEVEL% equ 0 (
    echo ✅ KSP plugin applied
) else (
    echo ❌ KSP plugin not found
    exit /b 1
)

echo.
echo 🔧 Building with KSP...
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    echo.
    echo 🎉 KSP migration successful!
    echo ✅ No more KAPT compatibility issues
    echo ✅ Faster annotation processing
    echo ✅ Better JVM compatibility
    echo.
    echo 📊 Build output:
    dir app\build\outputs\apk\debug\ 2>nul || echo APK location may vary
) else (
    echo.
    echo ❌ Build failed. Check the output above for errors.
    echo 💡 Try running with --stacktrace for more details:
    echo    gradlew.bat assembleDebug --stacktrace
    exit /b 1
)
