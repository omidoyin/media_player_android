# Android Media Player

A comprehensive Android media player built with Jetpack Compose and Kotlin, featuring audio and video playback with advanced functionality.

## Features

### Core Functionality

- **Audio Player** - Play various audio formats (MP3, AAC, FLAC, etc.)
- **Video Player** - Play video files with full video controls
- **Audio-only mode** - Extract and play audio from video files
- **Search** - Search through your media library
- **Playlists** - Create, edit, and manage custom playlists
- **Favorites** - Mark and organize favorite tracks
- **Shuffle & Repeat** - Multiple playback modes (off, one, all)
- **Speed Control** - Adjust playback speed (0.5x to 2.0x)
- **Lyrics Display** - Time-synchronized lyrics with auto-scroll and manual editing
- **Tabbed Interface** - Separate tabs for Audio, Video, Playlists, and Favorites

### Technical Features

- **Modern UI** - Built with Jetpack Compose and Material 3
- **Background Playback** - Continues playing when app is in background
- **Media Session** - Integration with system media controls
- **Room Database** - Local storage for playlists and favorites
- **ExoPlayer** - High-performance media playback
- **Hilt Dependency Injection** - Clean architecture with DI
- **MVVM Architecture** - Reactive UI with ViewModels and StateFlow

## Architecture

The app follows Clean Architecture principles with MVVM pattern:

```
├── data/
│   ├── models/          # Data classes and enums
│   ├── database/        # Room database and DAOs
│   └── repository/      # Repository pattern implementation
├── domain/              # Business logic (if needed)
├── player/              # Media player management
├── ui/
│   ├── components/      # Reusable UI components
│   ├── screens/         # Screen composables
│   ├── viewmodels/      # ViewModels for state management
│   └── theme/           # UI theme and styling
└── di/                  # Dependency injection modules
```

## Technologies Used

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **ExoPlayer/Media3** - Media playback
- **Room** - Local database
- **Hilt** - Dependency injection
- **KSP** - Kotlin Symbol Processing (modern KAPT replacement)
- **Coroutines & Flow** - Asynchronous programming
- **ViewModel** - UI state management
- **Navigation Compose** - Navigation (if extended)

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9.20+
- Gradle 8.9+
- JVM 11-20 (recommended: JVM 17)
- **Minimum 4GB RAM** for build process (8GB recommended)

### Building the Project

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd MediaPlayer
   ```

2. **Open in Android Studio**

   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory and select it

3. **Sync the project**

   - Android Studio will automatically sync Gradle files
   - Wait for the sync to complete

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press Shift+F10

### Alternative: Command Line Build

**Linux/macOS:**

```bash
chmod +x build_and_test.sh
./build_and_test.sh
```

**Windows:**

```cmd
build_and_test.bat
```

**Manual Gradle Commands:**

```bash
# Linux/macOS
./gradlew clean assembleDebug
./gradlew installDebug

# Windows
gradlew.bat clean assembleDebug
gradlew.bat installDebug
```

### Permissions

The app requires the following permissions:

- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `READ_MEDIA_AUDIO` (Android 13+)
- `READ_MEDIA_VIDEO` (Android 13+)
- `WAKE_LOCK` (for background playback)
- `FOREGROUND_SERVICE` (for media service)

## Usage

### First Launch

1. Grant storage permissions when prompted
2. The app will automatically scan for media files
3. Use the refresh button to rescan if needed

### Playing Media

- Tap any audio or video file to start playback
- Use the mini player at the bottom for basic controls
- Tap the mini player to expand to full player controls

### Creating Playlists

1. Go to the Playlists tab
2. Tap "Create Playlist"
3. Enter name and optional description
4. Add songs using the "+" button on any media item

### Search

- Tap the search icon in the top bar
- Search by title, artist, or album
- Results update as you type

### Speed Control

- In the full player, tap the speed button (1.0x)
- Select from 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x

### Audio-only Mode for Videos

- When playing a video, tap the audio-only button
- Video will continue playing but only audio will be heard

### Lyrics Display

- Tap the lyrics button in player controls to view lyrics
- Supports both plain text and time-synchronized (LRC) lyrics
- Auto-scroll follows the current playback position
- Tap any lyrics line to seek to that position
- Adjust font size with +/- buttons
- Toggle auto-scroll on/off

### Managing Lyrics

- **Auto-detection**: App automatically searches for .lrc or .txt files
- **Manual editing**: Add or edit lyrics with built-in editor
- **Online search**: Search for lyrics from online sources (placeholder)
- **Export/Import**: Save lyrics as .lrc files
- **Format support**: Plain text and LRC time-synchronized format

## Customization

### Themes

The app uses Material 3 dynamic theming. You can customize colors in:

- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`

### Adding New Features

The modular architecture makes it easy to add new features:

1. Add data models in `data/models/`
2. Update database schema in `data/database/`
3. Add business logic in repository
4. Create UI components and screens
5. Wire everything together with ViewModels

## Performance Optimizations

- **Lazy Loading** - Media lists use LazyColumn for efficient scrolling
- **State Management** - Efficient state updates with StateFlow
- **Memory Management** - Proper lifecycle management for ExoPlayer
- **Background Processing** - Media scanning on background threads
- **Caching** - Room database for offline data

## Troubleshooting

### Common Issues

1. **No media files found**

   - Check storage permissions
   - Ensure media files are in standard directories
   - Try the refresh button

2. **Playback issues**

   - Check file format compatibility
   - Ensure files are not corrupted
   - Restart the app if needed

3. **Build errors**
   - Clean and rebuild project
   - Check Gradle sync
   - Update Android Studio if needed
   - See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for detailed solutions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- ExoPlayer team for excellent media playback library
- Jetpack Compose team for modern UI toolkit
- Material Design team for design guidelines
