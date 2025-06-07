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
import androidx.compose.ui.unit.dp
import com.mediaplayer.data.models.PlayerAction
import com.mediaplayer.ui.components.MediaItemListItem
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel
import com.mediaplayer.ui.viewmodels.PlaylistViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoritesScreen(
    viewModel: MediaPlayerViewModel,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val favoriteItems by viewModel.favoriteItems.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
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
                text = "${favoriteItems.size} favorites",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row {
                IconButton(
                    onClick = { 
                        if (favoriteItems.isNotEmpty()) {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayQueue(favoriteItems.shuffled(), 0)
                            )
                        }
                    },
                    enabled = favoriteItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle all favorites"
                    )
                }
                
                IconButton(
                    onClick = { 
                        if (favoriteItems.isNotEmpty()) {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayQueue(favoriteItems, 0)
                            )
                        }
                    },
                    enabled = favoriteItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play all favorites"
                    )
                }
            }
        }
        
        Divider()
        
        // Favorites list
        if (favoriteItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No favorites yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "Tap the heart icon on any song or video to add it to favorites",
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
                    items = favoriteItems,
                    key = { it.id }
                ) { favoriteItem ->
                    MediaItemListItem(
                        mediaItem = favoriteItem,
                        onPlay = {
                            viewModel.handlePlayerAction(
                                PlayerAction.PlayMedia(favoriteItem, favoriteItems)
                            )
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(favoriteItem)
                        },
                        onAddToPlaylist = {
                            playlistViewModel.showAddToPlaylistDialog(true, favoriteItem)
                        },
                        isCurrentlyPlaying = playerState.currentMedia?.id == favoriteItem.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
