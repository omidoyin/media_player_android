#!/bin/bash

# Quick build test to verify theme fixes

echo "🎨 Testing theme and resource fixes..."

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please ensure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "🧹 Cleaning project..."
./gradlew clean

echo "🔧 Testing resource compilation..."
./gradlew processDebugResources

if [ $? -eq 0 ]; then
    echo "✅ Resources compiled successfully!"
    
    echo "📦 Building APK..."
    ./gradlew assembleDebug
    
    if [ $? -eq 0 ]; then
        echo "🎉 Build successful! Theme issues resolved."
        echo ""
        echo "📊 APK created:"
        ls -la app/build/outputs/apk/debug/*.apk 2>/dev/null || echo "APK location may vary"
    else
        echo "❌ APK build failed. Check output above."
        exit 1
    fi
else
    echo "❌ Resource compilation failed. Check theme/resource files."
    exit 1
fi
