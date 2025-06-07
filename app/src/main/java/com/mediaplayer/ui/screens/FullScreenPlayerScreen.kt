package com.mediaplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mediaplayer.data.models.PlayerAction
import com.mediaplayer.ui.components.PlayerControls
import com.mediaplayer.ui.components.VideoPlayerOverlay
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel

@Composable
fun FullScreenPlayerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MediaPlayerViewModel = hiltViewModel(),
    lyricsViewModel: com.mediaplayer.ui.viewmodels.LyricsViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val lyricsState by lyricsViewModel.lyricsState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    var showLyrics by remember { mutableStateOf(false) }

    // Control visibility state for video player
    var showControls by remember { mutableStateOf(true) }
    val isVideoPlaying = playerState.currentMedia?.isVideo == true &&
                        !playerState.audioOnlyMode &&
                        playerState.isPlaying

    // Auto-hide controls during video playback
    LaunchedEffect(showControls, isVideoPlaying) {
        if (showControls && isVideoPlaying) {
            delay(3000) // Hide after 3 seconds
            showControls = false
        }
    }

    // Show controls when video is paused
    LaunchedEffect(playerState.isPlaying) {
        if (!playerState.isPlaying) {
            showControls = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        playerState.currentMedia?.let { media ->
            if (media.isVideo && !playerState.audioOnlyMode) {
                // Video player view with overlay
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                useController = false // We'll use our custom controls
                                player = viewModel.getExoPlayerInstance()
                            }
                        },
                        update = { playerView ->
                            playerView.player = viewModel.getExoPlayerInstance()
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video player overlay for seek gestures
                    VideoPlayerOverlay(
                        onAction = viewModel::handlePlayerAction,
                        onShowControls = { showControls = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Audio visualization or album art with optional lyrics
                if (showLyrics && lyricsState.lyrics.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Album art on the left
                        AudioVisualization(
                            mediaItem = media,
                            isLandscape = false,
                            modifier = Modifier.weight(1f)
                        )

                        // Lyrics on the right
                        com.mediaplayer.ui.components.LyricsDisplay(
                            lyricsState = lyricsState,
                            onAction = lyricsViewModel::handleLyricsAction,
                            onSeekToPosition = { position ->
                                viewModel.handlePlayerAction(PlayerAction.SeekTo(position))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    AudioVisualization(
                        mediaItem = media,
                        isLandscape = isLandscape,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Custom controls overlay - only show for audio or when controls are visible for video
        val shouldShowControlsOverlay = !isVideoPlaying || showControls

        AnimatedVisibility(
            visible = shouldShowControlsOverlay,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Lyrics toggle
                    IconButton(
                        onClick = { showLyrics = !showLyrics },
                        enabled = lyricsState.lyrics.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Toggle Lyrics",
                            tint = if (showLyrics) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Player controls
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    PlayerControls(
                        playerState = playerState,
                        onAction = viewModel::handlePlayerAction,
                        modifier = Modifier.padding(24.dp),
                        showFullControls = true
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioVisualization(
    mediaItem: com.mediaplayer.data.models.MediaItem,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        // Landscape layout
        Row(
            modifier = modifier.padding(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            AlbumArtDisplay(
                mediaItem = mediaItem,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.width(32.dp))

            // Media info
            MediaInfoDisplay(
                mediaItem = mediaItem,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // Portrait layout
        Column(
            modifier = modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Album art
            AlbumArtDisplay(
                mediaItem = mediaItem,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Media info
            MediaInfoDisplay(
                mediaItem = mediaItem,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AlbumArtDisplay(
    mediaItem: com.mediaplayer.data.models.MediaItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (mediaItem.albumArt != null) {
                AsyncImage(
                    model = mediaItem.albumArt,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Default album art
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mediaItem.isVideo)
                            Icons.Default.VideoFile
                        else
                            Icons.Default.AudioFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaInfoDisplay(
    mediaItem: com.mediaplayer.data.models.MediaItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mediaItem.displayTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mediaItem.displayArtist,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        if (mediaItem.album != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mediaItem.displayAlbum,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Additional info
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (mediaItem.isVideo) {
                AssistChip(
                    onClick = { },
                    label = { Text("Video") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            AssistChip(
                onClick = { },
                label = { Text(mediaItem.formattedDuration) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            if (mediaItem.playCount > 0) {
                AssistChip(
                    onClick = { },
                    label = { Text("${mediaItem.playCount} plays") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}
