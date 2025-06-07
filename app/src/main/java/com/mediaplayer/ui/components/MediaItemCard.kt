package com.mediaplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.mediaplayer.data.models.MediaItem

@Composable
fun MediaItemCard(
    mediaItem: MediaItem,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    showAlbumArt: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art or media type icon
            if (showAlbumArt) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaItem.albumArt != null) {
                        AsyncImage(
                            model = mediaItem.albumArt,
                            contentDescription = if (mediaItem.isVideo) "Video Thumbnail" else "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mediaItem.isVideo)
                                        Icons.Default.VideoFile
                                    else
                                        Icons.Default.AudioFile,
                                    contentDescription = if (mediaItem.isVideo) "Video" else "Audio",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Media info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mediaItem.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (mediaItem.artist != null) {
                    Text(
                        text = mediaItem.displayArtist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mediaItem.isVideo) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = "Video",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = mediaItem.formattedDuration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    if (mediaItem.playCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${mediaItem.playCount} plays",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Action buttons
            Row {
                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (mediaItem.isFavorite) 
                            Icons.Default.Favorite 
                        else 
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (mediaItem.isFavorite) 
                            "Remove from favorites" 
                        else 
                            "Add to favorites",
                        tint = if (mediaItem.isFavorite) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(
                    onClick = onAddToPlaylist
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaItemListItem(
    mediaItem: MediaItem,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentlyPlaying: Boolean = false,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = mediaItem.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentlyPlaying) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Column {
                if (mediaItem.artist != null) {
                    Text(
                        text = mediaItem.displayArtist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mediaItem.isVideo) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = "Video",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = mediaItem.formattedDuration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (mediaItem.albumArt != null) {
                    AsyncImage(
                        model = mediaItem.albumArt,
                        contentDescription = if (mediaItem.isVideo) "Video Thumbnail" else "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (mediaItem.isVideo)
                                    Icons.Default.VideoFile
                                else
                                    Icons.Default.AudioFile,
                                contentDescription = if (mediaItem.isVideo) "Video" else "Audio",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                if (isCurrentlyPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Currently Playing",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (mediaItem.isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (mediaItem.isFavorite)
                            "Remove from favorites"
                        else
                            "Add to favorites",
                        tint = if (mediaItem.isFavorite)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Three dots menu
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Add to playlist
                        DropdownMenuItem(
                            text = { Text("Add to playlist") },
                            onClick = {
                                onAddToPlaylist()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                            }
                        )

                        // Share (if callback provided)
                        onShare?.let { shareCallback ->
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    shareCallback()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                }
                            )
                        }

                        // Remove from playlist (if callback provided)
                        onRemoveFromPlaylist?.let { removeCallback ->
                            DropdownMenuItem(
                                text = { Text("Remove from playlist") },
                                onClick = {
                                    removeCallback()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlaylistRemove, contentDescription = null)
                                }
                            )
                        }

                        // Delete (if callback provided)
                        onDelete?.let { deleteCallback ->
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    deleteCallback()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                    leadingIconColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.clickable { onPlay() }
    )
}
