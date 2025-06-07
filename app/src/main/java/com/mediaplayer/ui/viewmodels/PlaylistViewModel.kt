package com.mediaplayer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaplayer.data.models.MediaItem
import com.mediaplayer.data.models.Playlist
import com.mediaplayer.data.models.PlaylistWithMedia
import com.mediaplayer.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()
    
    val playlists = mediaRepository.getAllPlaylistsWithMedia()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                mediaRepository.createPlaylist(name, description)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                mediaRepository.updatePlaylist(playlist)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                mediaRepository.deletePlaylist(playlist)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addMediaToPlaylist(playlistId: String, mediaItem: MediaItem) {
        viewModelScope.launch {
            try {
                mediaRepository.addMediaToPlaylist(playlistId, mediaItem.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun removeMediaFromPlaylist(playlistId: String, mediaItem: MediaItem) {
        viewModelScope.launch {
            try {
                mediaRepository.removeMediaFromPlaylist(playlistId, mediaItem.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun getPlaylistMedia(playlistId: String): Flow<List<MediaItem>> {
        return mediaRepository.getPlaylistMedia(playlistId)
    }
    
    fun setSelectedPlaylist(playlist: PlaylistWithMedia?) {
        _uiState.value = _uiState.value.copy(selectedPlaylist = playlist)
    }
    
    fun showCreateDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDialog = show)
    }
    
    fun showAddToPlaylistDialog(show: Boolean, mediaItem: MediaItem? = null) {
        _uiState.value = _uiState.value.copy(
            showAddToPlaylistDialog = show,
            selectedMediaForPlaylist = mediaItem
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PlaylistUiState(
    val selectedPlaylist: PlaylistWithMedia? = null,
    val showCreateDialog: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val selectedMediaForPlaylist: MediaItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
