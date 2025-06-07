#!/bin/bash

# Android Media Player Build and Test Script

echo "🎵 Building Android Media Player with Lyrics Display..."
echo "📋 Using Gradle 8.9 with Kotlin 1.9.20 + KSP"

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please ensure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📦 Cleaning project..."
./gradlew clean

echo "🔧 Building project..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"

    echo "🧪 Running unit tests..."
    ./gradlew testDebugUnitTest

    if [ $? -eq 0 ]; then
        echo "✅ All tests passed!"
        echo ""
        echo "🎉 Android Media Player is ready!"
        echo ""
        echo "📱 To install on device/emulator:"
        echo "   ./gradlew installDebug"
        echo ""
        echo "🔍 To run tests:"
        echo "   ./gradlew test"
        echo ""
        echo "📊 To generate test report:"
        echo "   ./gradlew testDebugUnitTest --continue"
        echo "   Open: app/build/reports/tests/testDebugUnitTest/index.html"
        echo ""
        echo "🚀 Features included:"
        echo "   ✓ Audio & Video playback"
        echo "   ✓ Playlists management"
        echo "   ✓ Favorites system"
        echo "   ✓ Search functionality"
        echo "   ✓ Lyrics display with time-sync"
        echo "   ✓ Shuffle & Repeat modes"
        echo "   ✓ Speed control (0.5x - 2.0x)"
        echo "   ✓ Audio-only mode for videos"
        echo "   ✓ Background playback"
        echo "   ✓ Modern Material 3 UI"
        echo "   ✓ Clean Architecture (MVVM)"
        echo ""
    else
        echo "❌ Some tests failed. Check the output above."
        exit 1
    fi
else
    echo "❌ Build failed. Check the output above."
    exit 1
fi
