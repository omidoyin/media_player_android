package com.mediaplayer.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaplayer.data.models.*
import com.mediaplayer.data.repository.MediaRepository
import com.mediaplayer.player.PlayerManager
import com.mediaplayer.utils.MediaUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaRepository: MediaRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    companion object {
        private const val TAG = "MediaPlayerViewModel"
    }

    private val _uiState = MutableStateFlow(MediaPlayerUiState())
    val uiState: StateFlow<MediaPlayerUiState> = _uiState.asStateFlow()

    val playerState = playerManager.playerState

    val audioItems = mediaRepository.getAllAudioItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val videoItems = mediaRepository.getAllVideoItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteItems = mediaRepository.getFavoriteItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists = mediaRepository.getAllPlaylistsWithMedia()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        playerManager.initializePlayer()
        scanForMediaFiles()

        // Update position every second
        viewModelScope.launch {
            while (true) {
                playerManager.updatePosition()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun handlePlayerAction(action: PlayerAction) {
        playerManager.handleAction(action)

        // Track play count when starting playback
        if (action is PlayerAction.PlayMedia) {
            viewModelScope.launch {
                mediaRepository.incrementPlayCount(action.mediaItem.id)
            }
        }
    }

    fun toggleFavorite(mediaItem: MediaItem) {
        viewModelScope.launch {
            mediaRepository.toggleFavorite(mediaItem)
        }
    }

    fun scanForMediaFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                mediaRepository.scanForMediaFiles()
                _uiState.value = _uiState.value.copy(isScanning = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message
                )
            }
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            try {
                mediaRepository.clearThumbnailCache()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun regenerateThumbnails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                mediaRepository.regenerateThumbnails()
                _uiState.value = _uiState.value.copy(isScanning = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message
                )
            }
        }
    }

    suspend fun getThumbnailCacheSize(): Long {
        return try {
            mediaRepository.getThumbnailCacheSize()
        } catch (e: Exception) {
            0L
        }
    }

    fun setSelectedTab(tab: MediaTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getExoPlayerInstance() = playerManager.getExoPlayerInstance()

    /**
     * Play a media file from external URI (e.g., opened from file manager)
     */
    fun playExternalFile(uri: Uri) {
        Log.d(TAG, "playExternalFile called with URI: $uri")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to play external file: $uri")

                // Ensure player is initialized
                if (playerManager.getExoPlayerInstance() == null) {
                    Log.d(TAG, "Player not initialized, initializing now...")
                    playerManager.initializePlayer()
                }

                // Create a temporary MediaItem from the URI
                val mediaItem = createMediaItemFromUri(uri)

                if (mediaItem != null) {
                    Log.d(TAG, "Created MediaItem: ${mediaItem.title}, path: ${mediaItem.path}")

                    // Play the media item directly
                    playerManager.handleAction(PlayerAction.PlayMedia(mediaItem))
                    Log.d(TAG, "Successfully sent PlayMedia action for external file: ${mediaItem.title}")

                    // Force update UI state to show that we're playing
                    _uiState.value = _uiState.value.copy(error = null)
                } else {
                    Log.e(TAG, "Failed to create MediaItem from URI: $uri")
                    _uiState.value = _uiState.value.copy(error = "Unable to play this file")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing external file: $uri", e)
                _uiState.value = _uiState.value.copy(error = "Error playing file: ${e.message}")
            }
        }
    }

    /**
     * Create a MediaItem from external URI
     */
    private suspend fun createMediaItemFromUri(uri: Uri): MediaItem? {
        return try {
            Log.d(TAG, "Creating MediaItem from URI: $uri")
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)

            Log.d(TAG, "Detected MIME type: $mimeType")

            if (mimeType == null) {
                Log.w(TAG, "Could not determine MIME type for URI: $uri")
                // Try to determine from file extension as fallback
                val path = uri.path ?: uri.toString()
                val extension = MediaUtils.getFileExtension(path).lowercase()
                Log.d(TAG, "Trying to determine type from extension: $extension")

                // Common audio extensions
                val audioExtensions = listOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma")
                // Common video extensions
                val videoExtensions = listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "3gp")

                when {
                    audioExtensions.contains(extension) -> {
                        Log.d(TAG, "Treating as audio file based on extension")
                        // Continue with audio processing
                    }
                    videoExtensions.contains(extension) -> {
                        Log.d(TAG, "Treating as video file based on extension")
                        // Continue with video processing
                    }
                    else -> {
                        Log.w(TAG, "Unknown file extension: $extension")
                        return null
                    }
                }
            } else {
                // Check if it's a supported media type
                if (!MediaUtils.isAudioFile(mimeType) && !MediaUtils.isVideoFile(mimeType)) {
                    Log.w(TAG, "Unsupported MIME type: $mimeType")
                    return null
                }
            }

            // Extract metadata
            val cursor = contentResolver.query(uri, null, null, null, null)
            var displayName = "Unknown"
            var size = 0L

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)

                    if (nameIndex != -1) {
                        displayName = it.getString(nameIndex) ?: "Unknown"
                    }
                    if (sizeIndex != -1) {
                        size = it.getLong(sizeIndex)
                    }
                }
            }

            Log.d(TAG, "File metadata - Name: $displayName, Size: $size")

            // Get duration
            val duration = MediaUtils.getMediaDuration(context, uri.toString())
            Log.d(TAG, "File duration: $duration ms")

            // Determine if it's video based on MIME type or extension
            val isVideo = if (mimeType != null) {
                MediaUtils.isVideoFile(mimeType)
            } else {
                val extension = MediaUtils.getFileExtension(uri.path ?: uri.toString()).lowercase()
                listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "3gp").contains(extension)
            }

            // Create MediaItem
            val mediaItem = MediaItem(
                id = "external_${uri.hashCode()}", // Temporary ID
                title = MediaUtils.getFileNameWithoutExtension(displayName),
                artist = "Unknown Artist",
                album = "Unknown Album",
                path = uri.toString(),
                duration = duration,
                size = size,
                mimeType = mimeType ?: "application/octet-stream",
                dateAdded = System.currentTimeMillis(),
                isVideo = isVideo
            )

            Log.d(TAG, "Created MediaItem: ${mediaItem.title} (${if (isVideo) "video" else "audio"})")
            mediaItem
        } catch (e: Exception) {
            Log.e(TAG, "Error creating MediaItem from URI: $uri", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.releasePlayer()
    }
}

data class MediaPlayerUiState(
    val selectedTab: MediaTab = MediaTab.AUDIO,
    val isScanning: Boolean = false,
    val error: String? = null
)

enum class MediaTab {
    AUDIO, VIDEO, PLAYLISTS, FAVORITES, SETTINGS
}
