package com.mediaplayer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mediaplayer.data.models.*

@Composable
fun PlayerControls(
    playerState: PlayerState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
    showFullControls: Boolean = true,
    onShowLyrics: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current media info
        playerState.currentMedia?.let { media ->
            MediaInfo(
                mediaItem = media,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Progress bar
        if (showFullControls) {
            ProgressBar(
                progress = playerState.progress,
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                onSeek = { position -> onAction(PlayerAction.SeekTo(position)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            if (showFullControls) {
                IconButton(
                    onClick = { onAction(PlayerAction.ToggleShuffle) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.shuffleEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Previous
            IconButton(
                onClick = { onAction(PlayerAction.Previous) },
                enabled = playerState.hasPrevious
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause
            FloatingActionButton(
                onClick = {
                    if (playerState.isPlaying) {
                        onAction(PlayerAction.Pause)
                    } else {
                        onAction(PlayerAction.Play)
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                if (playerState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = if (playerState.isPlaying)
                            Icons.Default.Pause
                        else
                            Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Next
            IconButton(
                onClick = { onAction(PlayerAction.Next) },
                enabled = playerState.hasNext
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Repeat
            if (showFullControls) {
                RepeatControl(
                    playerState = playerState,
                    onToggleRepeat = { onAction(PlayerAction.ToggleRepeat) },
                    onSetCustomRepeat = { count -> onAction(PlayerAction.SetCustomRepeat(count)) }
                )
            }
        }

        // Additional controls
        if (showFullControls) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed control
                SpeedControl(
                    currentSpeed = playerState.playbackSpeed,
                    onSpeedChange = { speed -> onAction(PlayerAction.SetSpeed(speed)) }
                )

                // Lyrics toggle
                if (onShowLyrics != null) {
                    IconButton(
                        onClick = onShowLyrics
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Show Lyrics",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Audio-only mode for videos
                if (playerState.currentMedia?.isVideo == true) {
                    IconButton(
                        onClick = { onAction(PlayerAction.ToggleAudioOnlyMode) }
                    ) {
                        Icon(
                            imageVector = if (playerState.audioOnlyMode)
                                Icons.Default.AudioFile
                            else
                                Icons.Default.VideoFile,
                            contentDescription = "Audio Only Mode",
                            tint = if (playerState.audioOnlyMode)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaInfo(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mediaItem.displayTitle,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = mediaItem.displayArtist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (mediaItem.album != null) {
            Text(
                text = mediaItem.displayAlbum,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Slider(
            value = progress,
            onValueChange = { newProgress ->
                val newPosition = (newProgress * duration).toLong()
                onSeek(newPosition)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SpeedControl(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpeedDialog by remember { mutableStateOf(false) }

    TextButton(
        onClick = { showSpeedDialog = true },
        modifier = modifier
    ) {
        Text("${currentSpeed}x")
    }

    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed") },
            text = {
                Column {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    speeds.forEach { speed ->
                        TextButton(
                            onClick = {
                                onSpeedChange(speed)
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (speed == currentSpeed)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RepeatControl(
    playerState: PlayerState,
    onToggleRepeat: () -> Unit,
    onSetCustomRepeat: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRepeatDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = {
                        if (playerState.repeatMode == RepeatMode.CUSTOM) {
                            showRepeatDialog = true
                        } else {
                            onToggleRepeat()
                        }
                    },
                    onLongClick = {
                        showRepeatDialog = true
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (playerState.repeatMode) {
                    RepeatMode.OFF -> Icons.Default.Repeat
                    RepeatMode.ALL -> Icons.Default.Repeat
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    RepeatMode.CUSTOM -> Icons.Default.Repeat
                },
                contentDescription = "Repeat",
                tint = if (playerState.repeatMode != RepeatMode.OFF)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Show repeat count for custom mode
        if (playerState.repeatMode == RepeatMode.CUSTOM) {
            Text(
                text = "${playerState.currentRepeatCount + 1}/${playerState.customRepeatCount}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    // Repeat count dialog
    if (showRepeatDialog) {
        RepeatCountDialog(
            currentCount = playerState.customRepeatCount,
            onDismiss = { showRepeatDialog = false },
            onConfirm = { count ->
                onSetCustomRepeat(count)
                showRepeatDialog = false
            }
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val minutes = timeMs / 60000
    val seconds = (timeMs % 60000) / 1000
    return String.format("%d:%02d", minutes, seconds)
}
