package com.mediaplayer.data.models

data class PlayerState(
    val currentMedia: MediaItem? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val queue: List<MediaItem> = emptyList(),
    val currentIndex: Int = -1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val audioOnlyMode: Boolean = false // For playing video as audio
) {
    val progress: Float get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
    
    val hasNext: Boolean get() = when {
        shuffleEnabled -> queue.isNotEmpty()
        repeatMode == RepeatMode.ONE -> true
        else -> currentIndex < queue.size - 1
    }
    
    val hasPrevious: Boolean get() = when {
        shuffleEnabled -> queue.isNotEmpty()
        repeatMode == RepeatMode.ONE -> true
        else -> currentIndex > 0
    }
    
    val formattedPosition: String get() {
        val minutes = currentPosition / 60000
        val seconds = (currentPosition % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
    
    val formattedDuration: String get() {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
}

enum class RepeatMode {
    OFF, ONE, ALL
}

sealed class PlayerAction {
    object Play : PlayerAction()
    object Pause : PlayerAction()
    object Next : PlayerAction()
    object Previous : PlayerAction()
    object ToggleShuffle : PlayerAction()
    object ToggleRepeat : PlayerAction()
    object ToggleAudioOnlyMode : PlayerAction()
    data class SeekTo(val position: Long) : PlayerAction()
    data class SeekForward(val seconds: Int = 10) : PlayerAction()
    data class SeekBackward(val seconds: Int = 10) : PlayerAction()
    data class SetSpeed(val speed: Float) : PlayerAction()
    data class PlayMedia(val mediaItem: MediaItem, val queue: List<MediaItem> = emptyList()) : PlayerAction()
    data class PlayQueue(val queue: List<MediaItem>, val startIndex: Int = 0) : PlayerAction()
    data class AddToQueue(val mediaItem: MediaItem) : PlayerAction()
    data class RemoveFromQueue(val index: Int) : PlayerAction()
}
