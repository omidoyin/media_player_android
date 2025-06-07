package com.mediaplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mediaplayer.data.models.PlayerAction
import com.mediaplayer.data.models.SortOption
import com.mediaplayer.data.models.sortedBy
import com.mediaplayer.ui.components.MediaItemListItem
import com.mediaplayer.ui.components.SortDialog
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel
import com.mediaplayer.ui.viewmodels.PlaylistViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun VideoScreen(
    viewModel: MediaPlayerViewModel,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val videoItems by viewModel.videoItems.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val context = LocalContext.current

    // Share function
    fun shareVideoFile(videoItem: com.mediaplayer.data.models.MediaItem) {
        try {
            val file = File(videoItem.path)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Sharing: ${videoItem.displayTitle}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
        } catch (e: Exception) {
            // Handle error - could show a toast or snackbar
        }
    }

    Column(modifier = modifier) {
        // Controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${videoItems.size} videos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Row {
                IconButton(
                    onClick = {
                        if (videoItems.isNotEmpty()) {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayQueue(videoItems.shuffled(), 0)
                            )
                        }
                    },
                    enabled = videoItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle all"
                    )
                }

                IconButton(
                    onClick = {
                        if (videoItems.isNotEmpty()) {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayQueue(videoItems, 0)
                            )
                        }
                    },
                    enabled = videoItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play all"
                    )
                }
            }
        }

        Divider()

        // Video list
        if (videoItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No video files found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = "Tap the refresh button to scan for media files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = videoItems,
                    key = { it.id }
                ) { videoItem ->
                    MediaItemListItem(
                        mediaItem = videoItem,
                        onPlay = {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayMedia(videoItem, videoItems)
                            )
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(videoItem)
                        },
                        onAddToPlaylist = {
                            playlistViewModel.showAddToPlaylistDialog(true, videoItem)
                        },
                        onShare = {
                            shareVideoFile(videoItem)
                        },
                        isCurrentlyPlaying = playerState.currentMedia?.id == videoItem.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
