package com.mediaplayer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaplayer.data.models.*
import com.mediaplayer.data.repository.MediaRepository
import com.mediaplayer.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

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
