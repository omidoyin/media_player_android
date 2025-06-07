package com.mediaplayer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaplayer.data.models.*
import com.mediaplayer.data.repository.LyricsRepository
import com.mediaplayer.utils.LyricsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val lyricsRepository: LyricsRepository
) : ViewModel() {
    
    private val _lyricsState = MutableStateFlow(LyricsDisplayState())
    val lyricsState: StateFlow<LyricsDisplayState> = _lyricsState.asStateFlow()
    
    private val _currentMediaId = MutableStateFlow<String?>(null)
    
    fun handleLyricsAction(action: LyricsAction) {
        when (action) {
            is LyricsAction.LoadLyrics -> loadLyrics(action.mediaId)
            is LyricsAction.UpdateCurrentPosition -> updateCurrentPosition(action.position)
            is LyricsAction.ToggleLyrics -> toggleLyricsDisplay()
            is LyricsAction.ToggleAutoScroll -> toggleAutoScroll()
            is LyricsAction.SetFontSize -> setFontSize(action.size)
            is LyricsAction.SeekToLine -> seekToLine(action.lineIndex)
            is LyricsAction.SaveLyrics -> saveLyrics(action.mediaId, action.lyrics)
        }
    }
    
    private fun loadLyrics(mediaId: String) {
        if (_currentMediaId.value == mediaId && _lyricsState.value.lyrics.isNotEmpty()) {
            return // Already loaded
        }
        
        _currentMediaId.value = mediaId
        _lyricsState.value = _lyricsState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val lyrics = lyricsRepository.getLyrics(mediaId)
                if (lyrics != null) {
                    val lyricsLines = lyricsRepository.parseLyricsToLines(lyrics.content)
                    _lyricsState.value = _lyricsState.value.copy(
                        lyrics = lyricsLines,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _lyricsState.value = _lyricsState.value.copy(
                        lyrics = emptyList(),
                        isLoading = false,
                        error = "No lyrics found"
                    )
                }
            } catch (e: Exception) {
                _lyricsState.value = _lyricsState.value.copy(
                    lyrics = emptyList(),
                    isLoading = false,
                    error = e.message ?: "Failed to load lyrics"
                )
            }
        }
    }
    
    fun loadLyricsForMedia(mediaItem: MediaItem) {
        _currentMediaId.value = mediaItem.id
        _lyricsState.value = _lyricsState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val lyrics = lyricsRepository.loadLyricsForMedia(mediaItem)
                if (lyrics != null) {
                    val lyricsLines = lyricsRepository.parseLyricsToLines(lyrics.content)
                    _lyricsState.value = _lyricsState.value.copy(
                        lyrics = lyricsLines,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _lyricsState.value = _lyricsState.value.copy(
                        lyrics = emptyList(),
                        isLoading = false,
                        error = "No lyrics found for this track"
                    )
                }
            } catch (e: Exception) {
                _lyricsState.value = _lyricsState.value.copy(
                    lyrics = emptyList(),
                    isLoading = false,
                    error = e.message ?: "Failed to load lyrics"
                )
            }
        }
    }
    
    private fun updateCurrentPosition(position: Long) {
        val currentState = _lyricsState.value
        if (currentState.lyrics.isEmpty()) return
        
        val newLineIndex = LyricsUtils.getCurrentLineIndex(currentState.lyrics, position)
        if (newLineIndex != currentState.currentLineIndex) {
            _lyricsState.value = currentState.copy(currentLineIndex = newLineIndex)
        }
    }
    
    private fun toggleLyricsDisplay() {
        _lyricsState.value = _lyricsState.value.copy(
            showLyrics = !_lyricsState.value.showLyrics
        )
    }
    
    private fun toggleAutoScroll() {
        _lyricsState.value = _lyricsState.value.copy(
            autoScroll = !_lyricsState.value.autoScroll
        )
    }
    
    private fun setFontSize(size: LyricsFontSize) {
        _lyricsState.value = _lyricsState.value.copy(fontSize = size)
    }
    
    private fun seekToLine(lineIndex: Int) {
        val currentState = _lyricsState.value
        if (lineIndex in currentState.lyrics.indices) {
            // This would trigger a seek in the media player
            // The actual implementation would depend on how you handle player actions
            _lyricsState.value = currentState.copy(currentLineIndex = lineIndex)
        }
    }
    
    private fun saveLyrics(mediaId: String, lyricsText: String) {
        viewModelScope.launch {
            try {
                lyricsRepository.saveLyricsFromText(mediaId, lyricsText)
                val lyricsLines = lyricsRepository.parseLyricsToLines(lyricsText)
                _lyricsState.value = _lyricsState.value.copy(
                    lyrics = lyricsLines,
                    error = null
                )
            } catch (e: Exception) {
                _lyricsState.value = _lyricsState.value.copy(
                    error = e.message ?: "Failed to save lyrics"
                )
            }
        }
    }
    
    fun searchOnlineLyrics(title: String, artist: String, album: String? = null) {
        _lyricsState.value = _lyricsState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val lyrics = lyricsRepository.searchOnlineLyrics(title, artist, album)
                if (lyrics != null) {
                    val mediaId = _currentMediaId.value
                    if (mediaId != null) {
                        saveLyrics(mediaId, lyrics)
                    }
                } else {
                    _lyricsState.value = _lyricsState.value.copy(
                        isLoading = false,
                        error = "No lyrics found online"
                    )
                }
            } catch (e: Exception) {
                _lyricsState.value = _lyricsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to search lyrics online"
                )
            }
        }
    }
    
    fun clearError() {
        _lyricsState.value = _lyricsState.value.copy(error = null)
    }
    
    fun getSeekPositionForLine(lineIndex: Int): Long? {
        val currentState = _lyricsState.value
        return if (lineIndex in currentState.lyrics.indices) {
            currentState.lyrics[lineIndex].timestamp
        } else null
    }
    
    fun searchInLyrics(query: String): List<Int> {
        return LyricsUtils.searchInLyrics(_lyricsState.value.lyrics, query)
    }
    
    fun exportLyrics(mediaItem: MediaItem): Boolean {
        val currentState = _lyricsState.value
        if (currentState.lyrics.isEmpty()) return false
        
        viewModelScope.launch {
            try {
                val lyrics = Lyrics(
                    mediaId = mediaItem.id,
                    content = LyricsUtils.toLrcFormat(currentState.lyrics),
                    isTimeSynced = true,
                    source = LyricsSource.LOCAL
                )
                lyricsRepository.exportLyricsToFile(mediaItem, lyrics)
            } catch (e: Exception) {
                _lyricsState.value = _lyricsState.value.copy(
                    error = e.message ?: "Failed to export lyrics"
                )
            }
        }
        
        return true
    }
}
