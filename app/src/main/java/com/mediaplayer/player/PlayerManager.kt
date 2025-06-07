package com.mediaplayer.player

import android.content.Context
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.C
import com.mediaplayer.data.models.*
import com.mediaplayer.utils.ScreenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenManager: ScreenManager
) {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var currentQueue: List<MediaItem> = emptyList()
    private var shuffledQueue: List<MediaItem> = emptyList()
    private var originalIndex: Int = -1

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState {
                it.copy(
                    isLoading = playbackState == Player.STATE_BUFFERING,
                    isPlaying = exoPlayer?.isPlaying == true
                )
            }

            // Handle playback completion
            if (playbackState == Player.STATE_ENDED) {
                handlePlaybackCompletion()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayerState { it.copy(isPlaying = isPlaying) }
            updateScreenWakeLock()
        }

        override fun onMediaItemTransition(mediaItem: ExoMediaItem?, reason: Int) {
            mediaItem?.let {
                updateCurrentMedia()
                updateScreenWakeLock()
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            updatePlayerState { it.copy(error = error.message) }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateCurrentMedia()
        }
    }

    fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context)
                .build()
                .apply {
                    addListener(playerListener)
                }
        }
    }

    fun releasePlayer() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    fun getExoPlayerInstance() = exoPlayer

    fun handleAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.Play -> play()
            is PlayerAction.Pause -> pause()
            is PlayerAction.Next -> next()
            is PlayerAction.Previous -> previous()
            is PlayerAction.ToggleShuffle -> toggleShuffle()
            is PlayerAction.ToggleRepeat -> toggleRepeat()
            is PlayerAction.ToggleAudioOnlyMode -> toggleAudioOnlyMode()
            is PlayerAction.SeekTo -> seekTo(action.position)
            is PlayerAction.SetSpeed -> setPlaybackSpeed(action.speed)
            is PlayerAction.PlayMedia -> playMedia(action.mediaItem, action.queue)
            is PlayerAction.PlayQueue -> playQueue(action.queue, action.startIndex)
            is PlayerAction.AddToQueue -> addToQueue(action.mediaItem)
            is PlayerAction.RemoveFromQueue -> removeFromQueue(action.index)
        }
    }

    private fun play() {
        val state = _playerState.value
        if (state.currentMedia != null) {
            exoPlayer?.play()
        } else {
            // If no media is loaded, try to play the first item in queue
            if (currentQueue.isNotEmpty()) {
                playMediaInternal(currentQueue[0])
            }
        }
    }

    private fun pause() {
        exoPlayer?.pause()
    }

    private fun next() {
        val state = _playerState.value
        when {
            state.repeatMode == RepeatMode.ONE -> {
                exoPlayer?.seekTo(0)
                exoPlayer?.play()
            }
            state.shuffleEnabled -> {
                val currentIndex = shuffledQueue.indexOfFirst { it.id == state.currentMedia?.id }
                if (currentIndex < shuffledQueue.size - 1) {
                    val nextMedia = shuffledQueue[currentIndex + 1]
                    playMediaInternal(nextMedia)
                }
            }
            else -> {
                if (state.currentIndex < currentQueue.size - 1) {
                    val nextMedia = currentQueue[state.currentIndex + 1]
                    playMediaInternal(nextMedia)
                } else if (state.repeatMode == RepeatMode.ALL) {
                    val firstMedia = currentQueue.firstOrNull()
                    firstMedia?.let { playMediaInternal(it) }
                }
            }
        }
    }

    private fun previous() {
        val state = _playerState.value
        when {
            state.repeatMode == RepeatMode.ONE -> {
                exoPlayer?.seekTo(0)
                exoPlayer?.play()
            }
            state.shuffleEnabled -> {
                val currentIndex = shuffledQueue.indexOfFirst { it.id == state.currentMedia?.id }
                if (currentIndex > 0) {
                    val prevMedia = shuffledQueue[currentIndex - 1]
                    playMediaInternal(prevMedia)
                }
            }
            else -> {
                if (state.currentIndex > 0) {
                    val prevMedia = currentQueue[state.currentIndex - 1]
                    playMediaInternal(prevMedia)
                } else if (state.repeatMode == RepeatMode.ALL) {
                    val lastMedia = currentQueue.lastOrNull()
                    lastMedia?.let { playMediaInternal(it) }
                }
            }
        }
    }

    private fun toggleShuffle() {
        val newShuffleEnabled = !_playerState.value.shuffleEnabled
        if (newShuffleEnabled) {
            shuffledQueue = currentQueue.shuffled()
        }
        updatePlayerState { it.copy(shuffleEnabled = newShuffleEnabled) }
    }

    private fun toggleRepeat() {
        val currentMode = _playerState.value.repeatMode
        val newMode = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        updatePlayerState { it.copy(repeatMode = newMode) }
    }

    private fun toggleAudioOnlyMode() {
        val newAudioOnlyMode = !_playerState.value.audioOnlyMode
        updatePlayerState { it.copy(audioOnlyMode = newAudioOnlyMode) }
        updateScreenWakeLock()
    }

    private fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    private fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackParameters(
            PlaybackParameters(speed)
        )
        updatePlayerState { it.copy(playbackSpeed = speed) }
    }

    private fun playMedia(mediaItem: MediaItem, queue: List<MediaItem>) {
        currentQueue = if (queue.isNotEmpty()) queue else listOf(mediaItem)
        val index = currentQueue.indexOfFirst { it.id == mediaItem.id }
        playQueue(currentQueue, index)
    }

    private fun playQueue(queue: List<MediaItem>, startIndex: Int) {
        currentQueue = queue
        if (startIndex in queue.indices) {
            val mediaItem = queue[startIndex]
            playMediaInternal(mediaItem)
            updatePlayerState {
                it.copy(
                    queue = queue,
                    currentIndex = startIndex
                )
            }
        }
    }

    private fun playMediaInternal(mediaItem: MediaItem) {
        val exoMediaItem = ExoMediaItem.Builder()
            .setUri(mediaItem.path)
            .setMediaId(mediaItem.id)
            .build()

        exoPlayer?.setMediaItem(exoMediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        updatePlayerState {
            it.copy(
                currentMedia = mediaItem,
                currentIndex = currentQueue.indexOfFirst { item -> item.id == mediaItem.id }
            )
        }
    }

    private fun addToQueue(mediaItem: MediaItem) {
        currentQueue = currentQueue + mediaItem
        updatePlayerState { it.copy(queue = currentQueue) }
    }

    private fun removeFromQueue(index: Int) {
        if (index in currentQueue.indices) {
            currentQueue = currentQueue.toMutableList().apply { removeAt(index) }
            updatePlayerState {
                it.copy(
                    queue = currentQueue,
                    currentIndex = if (it.currentIndex > index) it.currentIndex - 1 else it.currentIndex
                )
            }
        }
    }

    private fun updateCurrentMedia() {
        exoPlayer?.let { player ->
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < currentQueue.size) {
                val mediaItem = currentQueue[currentIndex]
                updatePlayerState {
                    it.copy(
                        currentMedia = mediaItem,
                        currentIndex = currentIndex,
                        duration = player.duration.takeIf { duration -> duration != C.TIME_UNSET } ?: 0L
                    )
                }
            }
        }
    }

    fun updatePosition() {
        exoPlayer?.let { player ->
            updatePlayerState {
                it.copy(
                    currentPosition = player.currentPosition,
                    duration = player.duration.takeIf { duration -> duration != C.TIME_UNSET } ?: 0L
                )
            }
        }
    }

    private fun handlePlaybackCompletion() {
        val state = _playerState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                // Repeat current song
                exoPlayer?.seekTo(0)
                exoPlayer?.play()
            }
            RepeatMode.ALL -> {
                // Move to next song, or restart queue if at end
                if (state.currentIndex < currentQueue.size - 1) {
                    next()
                } else {
                    // Restart from beginning
                    val firstMedia = currentQueue.firstOrNull()
                    firstMedia?.let { playMediaInternal(it) }
                }
            }
            RepeatMode.OFF -> {
                // Move to next song if available, otherwise stop
                if (state.currentIndex < currentQueue.size - 1) {
                    next()
                } else {
                    // End of queue, stop playback
                    updatePlayerState { it.copy(isPlaying = false) }
                }
            }
        }
    }

    private fun updatePlayerState(update: (PlayerState) -> PlayerState) {
        _playerState.value = update(_playerState.value)
    }

    private fun updateScreenWakeLock() {
        val state = _playerState.value
        val isVideoPlaying = state.isPlaying &&
                           state.currentMedia?.isVideo == true &&
                           !state.audioOnlyMode
        screenManager.keepScreenOn(isVideoPlaying, state.currentMedia?.isVideo == true)
    }
}
