package com.mediaplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediaplayer.data.models.PlayerAction
import com.mediaplayer.ui.components.MediaItemListItem
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel
import com.mediaplayer.ui.viewmodels.PlaylistViewModel

@Composable
fun PlaylistScreen(
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    mediaPlayerViewModel: MediaPlayerViewModel = hiltViewModel()
) {
    val playlists by playlistViewModel.playlists.collectAsState()
    val uiState by playlistViewModel.uiState.collectAsState()
    val playerState by mediaPlayerViewModel.playerState.collectAsState()
    
    Column(modifier = modifier) {
        // Header with create playlist button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${playlists.size} playlists",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Button(
                onClick = { playlistViewModel.showCreateDialog(true) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Playlist")
            }
        }
        
        Divider()
        
        // Show playlist detail if one is selected
        uiState.selectedPlaylist?.let { playlistWithMedia ->
            PlaylistDetailView(
                playlistWithMedia = playlistWithMedia,
                onBack = { playlistViewModel.setSelectedPlaylist(null) },
                onPlayMedia = { mediaItem ->
                    mediaPlayerViewModel.handlePlayerAction(
                        PlayerAction.PlayMedia(mediaItem, playlistWithMedia.mediaItems)
                    )
                },
                onRemoveFromPlaylist = { mediaItem ->
                    playlistViewModel.removeMediaFromPlaylist(
                        playlistWithMedia.playlist.id,
                        mediaItem
                    )
                },
                onToggleFavorite = { mediaItem ->
                    mediaPlayerViewModel.toggleFavorite(mediaItem)
                },
                currentlyPlayingId = playerState.currentMedia?.id,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            // Show playlists list
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No playlists yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Text(
                            text = "Create your first playlist to organize your music",
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
                        items = playlists,
                        key = { it.playlist.id }
                    ) { playlistWithMedia ->
                        PlaylistCard(
                            playlistWithMedia = playlistWithMedia,
                            onClick = { playlistViewModel.setSelectedPlaylist(playlistWithMedia) },
                            onPlay = {
                                if (playlistWithMedia.mediaItems.isNotEmpty()) {
                                    mediaPlayerViewModel.handlePlayerAction(
                                        PlayerAction.PlayQueue(playlistWithMedia.mediaItems, 0)
                                    )
                                }
                            },
                            onDelete = { playlistViewModel.deletePlaylist(playlistWithMedia.playlist) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Create playlist dialog
    if (uiState.showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { playlistViewModel.showCreateDialog(false) },
            onConfirm = { name, description ->
                playlistViewModel.createPlaylist(name, description)
                playlistViewModel.showCreateDialog(false)
            }
        )
    }
    
    // Add to playlist dialog
    if (uiState.showAddToPlaylistDialog && uiState.selectedMediaForPlaylist != null) {
        AddToPlaylistDialog(
            playlists = playlists.map { it.playlist },
            onDismiss = { playlistViewModel.showAddToPlaylistDialog(false) },
            onAddToPlaylist = { playlist ->
                playlistViewModel.addMediaToPlaylist(
                    playlist.id,
                    uiState.selectedMediaForPlaylist!!
                )
                playlistViewModel.showAddToPlaylistDialog(false)
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlistWithMedia: com.mediaplayer.data.models.PlaylistWithMedia,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = "Playlist",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlistWithMedia.playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${playlistWithMedia.itemCount} songs • ${playlistWithMedia.formattedDuration}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                playlistWithMedia.playlist.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Action buttons
            Row {
                IconButton(
                    onClick = onPlay,
                    enabled = playlistWithMedia.mediaItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play playlist"
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete playlist",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlistWithMedia: com.mediaplayer.data.models.PlaylistWithMedia,
    onBack: () -> Unit,
    onPlayMedia: (com.mediaplayer.data.models.MediaItem) -> Unit,
    onRemoveFromPlaylist: (com.mediaplayer.data.models.MediaItem) -> Unit,
    onToggleFavorite: (com.mediaplayer.data.models.MediaItem) -> Unit,
    currentlyPlayingId: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlistWithMedia.playlist.name,
                    style = MaterialTheme.typography.titleLarge
                )
                
                Text(
                    text = "${playlistWithMedia.itemCount} songs • ${playlistWithMedia.formattedDuration}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(
                onClick = { 
                    if (playlistWithMedia.mediaItems.isNotEmpty()) {
                        onPlayMedia(playlistWithMedia.mediaItems.first())
                    }
                },
                enabled = playlistWithMedia.mediaItems.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play playlist"
                )
            }
        }
        
        Divider()
        
        // Media items
        if (playlistWithMedia.mediaItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "This playlist is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = playlistWithMedia.mediaItems,
                    key = { it.id }
                ) { mediaItem ->
                    MediaItemListItem(
                        mediaItem = mediaItem,
                        onPlay = { onPlayMedia(mediaItem) },
                        onToggleFavorite = { onToggleFavorite(mediaItem) },
                        onAddToPlaylist = { /* Add to another playlist */ },
                        onRemoveFromPlaylist = { onRemoveFromPlaylist(mediaItem) },
                        isCurrentlyPlaying = currentlyPlayingId == mediaItem.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (name.isNotBlank()) {
                        onConfirm(name, description.takeIf { it.isNotBlank() })
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<com.mediaplayer.data.models.Playlist>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (com.mediaplayer.data.models.Playlist) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            LazyColumn {
                items(playlists) { playlist ->
                    TextButton(
                        onClick = { onAddToPlaylist(playlist) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = playlist.name,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
