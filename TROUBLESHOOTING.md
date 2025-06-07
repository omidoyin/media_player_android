# Troubleshooting Guide

## Common Build Issues

### 1. Gradle Version Compatibility

**Error:** `The minimum compatible Gradle version is 8.5`

**Solution:**

- The project is configured with Gradle 8.9
- Ensure you're using JVM 11-20 (recommended: JVM 17)
- Check your `gradle/wrapper/gradle-wrapper.properties` file

### 2. Theme Resource Issues

**Error:** `resource style/Theme.Material3.DynamicColors.DayNight not found`

**Solution:**

- The project now uses stable Material 3 themes
- Added AppCompat dependency for theme compatibility
- Created proper light/dark theme variants

**Files Fixed:**

- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-night/themes.xml`
- `app/src/main/res/values/colors.xml`

### 3. KAPT Compatibility Issues

**Error:** `java.lang.IllegalAccessError: superclass access check failed: class org.jetbrains.kotlin.kapt3.base.javac.KaptJavaCompiler`

**Root Cause:** KAPT has compatibility issues with newer JVM versions (17+) due to module access restrictions.

**Solution:** The project has been migrated to **KSP (Kotlin Symbol Processing)**:

- ✅ Replaced `kotlin-kapt` with `com.google.devtools.ksp`
- ✅ Updated all `kapt()` dependencies to `ksp()`
- ✅ Added JVM module access arguments as fallback
- ✅ Configured Room schema location for KSP

**Benefits of KSP:**

- Faster annotation processing
- Better JVM compatibility
- Future-proof (KAPT is deprecated)
- Better error messages

**Test KSP Migration:**

```bash
# Linux/macOS
chmod +x test_ksp_build.sh
./test_ksp_build.sh

# Windows
test_ksp_build.bat
```

### 4. Android Gradle Plugin Compatibility

**Error:** AGP version compatibility issues

**Current Configuration:**

- Android Gradle Plugin: 8.5.2
- Gradle: 8.9
- Kotlin: 1.9.20

### 5. JVM Version Issues

**Error:** Unsupported JVM version

**Requirements:**

- Minimum: JVM 11
- Maximum: JVM 20
- Recommended: JVM 17

**Check your JVM version:**

```bash
java -version
```

### 6. Permission Issues

**Error:** Storage permission denied

**Solution:**

1. Grant storage permissions when prompted
2. For Android 13+: Grant both audio and video permissions
3. Check app settings if permissions were denied

### 7. ExoPlayer Issues

**Error:** Media playback failures

**Common Causes:**

- Unsupported media format
- Corrupted media files
- Missing codec support

**Solution:**

- Check supported formats in `MediaUtils.kt`
- Verify file integrity
- Restart the app

### 8. Room Database Issues

**Error:** Database migration failures

**Solution:**

- The app uses `fallbackToDestructiveMigration()`
- Clear app data if persistent issues occur
- Check database version in `MediaDatabase.kt`

### 9. Lyrics Loading Issues

**Error:** Lyrics not found or not loading

**Common Causes:**

- Missing .lrc or .txt files
- Incorrect file naming
- File encoding issues

**Solution:**

1. Place lyrics files next to media files
2. Use same filename as media file
3. Supported formats: `.lrc`, `.txt`
4. Ensure UTF-8 encoding

**Example:**

```
/Music/
  ├── song.mp3
  └── song.lrc  # or song.txt
```

### 10. Build Cache Issues

**Error:** Inconsistent build state

**Solution:**

```bash
# Clean build
./gradlew clean

# Clear Gradle cache
./gradlew --stop
rm -rf ~/.gradle/caches/

# Rebuild
./gradlew assembleDebug
```

### 11. Memory Issues

**Error:** `Daemon will be stopped at the end of the build after running out of JVM heap space`

**Solution:**
The project now includes a `gradle.properties` file with optimized memory settings:

```properties
org.gradle.jvmargs=-Xmx4g -Xms1g -XX:MaxMetaspaceSize=1g -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
```

**If you still have memory issues:**

1. Increase heap size: `-Xmx6g` or `-Xmx8g`
2. Close other applications
3. Restart your IDE
4. Run: `./gradlew --stop` then rebuild

**Quick Test:**

```bash
# Linux/macOS
chmod +x test_build.sh
./test_build.sh

# Windows
test_build.bat
```

## IDE-Specific Issues

### Android Studio

1. **Sync Issues:**

   - File → Sync Project with Gradle Files
   - Invalidate Caches and Restart

2. **Indexing Issues:**

   - Wait for indexing to complete
   - Check IDE memory settings

3. **Plugin Issues:**
   - Update Android Studio
   - Check plugin compatibility

### IntelliJ IDEA

1. **Gradle Import:**

   - Import as Gradle project
   - Use Gradle wrapper

2. **Kotlin Plugin:**
   - Ensure Kotlin plugin is enabled
   - Check version compatibility

## Performance Optimization

### Build Performance

1. **Enable Gradle Daemon:**

   ```properties
   org.gradle.daemon=true
   ```

2. **Parallel Builds:**

   ```properties
   org.gradle.parallel=true
   ```

3. **Build Cache:**
   ```properties
   org.gradle.caching=true
   ```

### Runtime Performance

1. **ProGuard/R8:**

   - Enabled in release builds
   - Check `proguard-rules.pro`

2. **Memory Management:**
   - Monitor memory usage
   - Check for memory leaks

## Getting Help

### Log Collection

1. **Build Logs:**

   ```bash
   ./gradlew assembleDebug --info > build.log 2>&1
   ```

2. **Runtime Logs:**
   ```bash
   adb logcat | grep MediaPlayer
   ```

### Reporting Issues

When reporting issues, include:

1. Android Studio version
2. Gradle version
3. JVM version
4. Full error message
5. Build logs
6. Device/emulator info

### Useful Commands

```bash
# Check Gradle version
./gradlew --version

# List all tasks
./gradlew tasks

# Build with debug info
./gradlew assembleDebug --debug

# Run tests with reports
./gradlew test --continue

# Install on device
./gradlew installDebug

# Uninstall from device
./gradlew uninstallDebug
```
