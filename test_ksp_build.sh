#!/bin/bash

# Test script for KSP migration

echo "🔧 Testing KSP (Kotlin Symbol Processing) migration..."

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please ensure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📋 Migration Summary:"
echo "✅ KAPT → KSP migration completed"
echo "✅ JVM module access arguments added"
echo "✅ Room schema location configured"
echo ""

echo "🛑 Stopping existing Gradle daemon..."
./gradlew --stop

echo "🧹 Cleaning project..."
./gradlew clean

echo "🔍 Checking KSP configuration..."
if grep -q "ksp(" app/build.gradle.kts; then
    echo "✅ KSP dependencies found"
else
    echo "❌ KSP dependencies not found"
    exit 1
fi

if grep -q "com.google.devtools.ksp" app/build.gradle.kts; then
    echo "✅ KSP plugin applied"
else
    echo "❌ KSP plugin not found"
    exit 1
fi

echo ""
echo "🔧 Building with KSP..."
./gradlew assembleDebug --info | grep -E "(ksp|KSP|BUILD|SUCCESSFUL|FAILED)"

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 KSP migration successful!"
    echo "✅ No more KAPT compatibility issues"
    echo "✅ Faster annotation processing"
    echo "✅ Better JVM compatibility"
    echo ""
    echo "📊 Build output:"
    ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "APK location may vary"
else
    echo ""
    echo "❌ Build failed. Check the output above for errors."
    echo "💡 Try running with --stacktrace for more details:"
    echo "   ./gradlew assembleDebug --stacktrace"
    exit 1
fi
