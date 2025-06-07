#!/bin/bash

# Quick test build script to verify memory settings

echo "🧪 Testing build with memory optimizations..."

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Please ensure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📋 Current Gradle settings:"
if [ -f "gradle.properties" ]; then
    echo "✅ gradle.properties found"
    grep "org.gradle.jvmargs" gradle.properties || echo "⚠️  JVM args not found"
    grep "org.gradle.parallel" gradle.properties || echo "⚠️  Parallel builds not configured"
    grep "org.gradle.caching" gradle.properties || echo "⚠️  Caching not configured"
else
    echo "❌ gradle.properties not found"
fi

echo ""
echo "🔧 Starting test build..."

# Stop any existing daemon to ensure fresh start with new memory settings
./gradlew --stop

echo "🧹 Cleaning project..."
./gradlew clean

echo "📦 Building debug APK..."
./gradlew assembleDebug --info | grep -E "(heap|memory|daemon|BUILD)"

if [ $? -eq 0 ]; then
    echo "✅ Build successful with memory optimizations!"
    echo ""
    echo "📊 Build artifacts:"
    ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "No APK found"
else
    echo "❌ Build failed. Check the output above for errors."
    exit 1
fi
