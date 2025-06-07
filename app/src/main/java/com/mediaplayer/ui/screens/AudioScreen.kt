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
fun AudioScreen(
    viewModel: MediaPlayerViewModel,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val audioItems by viewModel.audioItems.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var currentSortOption by remember { mutableStateOf(SortOption.TITLE_ASC) }
    var showSortDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Apply sorting
    val sortedAudioItems = remember(audioItems, currentSortOption) {
        audioItems.sortedBy(currentSortOption)
    }

    // Share function
    fun shareAudioFile(audioItem: com.mediaplayer.data.models.MediaItem) {
        try {
            val file = File(audioItem.path)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Sharing: ${audioItem.displayTitle}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Audio"))
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
                text = "${audioItems.size} songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Row {
                // Sort button
                IconButton(
                    onClick = { showSortDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort"
                    )
                }

                IconButton(
                    onClick = {
                        if (sortedAudioItems.isNotEmpty()) {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayQueue(sortedAudioItems.shuffled(), 0)
                            )
                        }
                    },
                    enabled = sortedAudioItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle all"
                    )
                }

                IconButton(
                    onClick = {
                        if (sortedAudioItems.isNotEmpty()) {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayQueue(sortedAudioItems, 0)
                            )
                        }
                    },
                    enabled = sortedAudioItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play all"
                    )
                }
            }
        }

        Divider()

        // Audio list
        if (sortedAudioItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No audio files found",
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
                    items = sortedAudioItems,
                    key = { it.id }
                ) { audioItem ->
                    MediaItemListItem(
                        mediaItem = audioItem,
                        onPlay = {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayMedia(audioItem, sortedAudioItems)
                            )
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(audioItem)
                        },
                        onAddToPlaylist = {
                            playlistViewModel.showAddToPlaylistDialog(true, audioItem)
                        },
                        onShare = {
                            shareAudioFile(audioItem)
                        },
                        isCurrentlyPlaying = playerState.currentMedia?.id == audioItem.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSortOption = currentSortOption,
            onSortOptionSelected = { sortOption ->
                currentSortOption = sortOption
            },
            onDismiss = { showSortDialog = false }
        )
    }
}
