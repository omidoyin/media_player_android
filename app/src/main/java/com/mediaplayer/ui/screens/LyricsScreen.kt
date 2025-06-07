package com.mediaplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediaplayer.data.models.*
import com.mediaplayer.ui.components.LyricsDisplay
import com.mediaplayer.ui.components.LyricsEditorDialog
import com.mediaplayer.ui.viewmodels.LyricsViewModel
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    lyricsViewModel: LyricsViewModel = hiltViewModel(),
    mediaPlayerViewModel: MediaPlayerViewModel = hiltViewModel()
) {
    val lyricsState by lyricsViewModel.lyricsState.collectAsState()
    val playerState by mediaPlayerViewModel.playerState.collectAsState()
    var showEditor by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    // Load lyrics when current media changes
    LaunchedEffect(playerState.currentMedia) {
        playerState.currentMedia?.let { media ->
            lyricsViewModel.loadLyricsForMedia(media)
        }
    }

    // Update lyrics position based on playback
    LaunchedEffect(playerState.currentPosition) {
        lyricsViewModel.handleLyricsAction(
            LyricsAction.UpdateCurrentPosition(playerState.currentPosition)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lyrics",
                            style = MaterialTheme.typography.titleMedium
                        )
                        playerState.currentMedia?.let { media ->
                            Text(
                                text = "${media.displayTitle} - ${media.displayArtist}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Search online
                    IconButton(
                        onClick = { showSearchDialog = true },
                        enabled = playerState.currentMedia != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Search online"
                        )
                    }

                    // Edit lyrics
                    IconButton(
                        onClick = { showEditor = true },
                        enabled = playerState.currentMedia != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit lyrics"
                        )
                    }

                    // More options
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export lyrics") },
                                onClick = {
                                    playerState.currentMedia?.let { media ->
                                        lyricsViewModel.exportLyrics(media)
                                    }
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                },
                                enabled = lyricsState.lyrics.isNotEmpty()
                            )

                            DropdownMenuItem(
                                text = { Text("Clear lyrics") },
                                onClick = {
                                    // Implement clear lyrics
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                },
                                enabled = lyricsState.lyrics.isNotEmpty()
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (playerState.currentMedia == null) {
                // No media playing
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No media playing",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = "Start playing a song to view lyrics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                // Show lyrics display
                LyricsDisplay(
                    lyricsState = lyricsState,
                    onAction = lyricsViewModel::handleLyricsAction,
                    onSeekToPosition = { position ->
                        mediaPlayerViewModel.handlePlayerAction(PlayerAction.SeekTo(position))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Lyrics editor dialog
    if (showEditor && playerState.currentMedia != null) {
        LyricsEditorDialog(
            mediaItem = playerState.currentMedia!!,
            initialLyrics = if (lyricsState.lyrics.isNotEmpty()) {
                com.mediaplayer.utils.LyricsUtils.toLrcFormat(lyricsState.lyrics)
            } else "",
            onDismiss = { showEditor = false },
            onSave = { lyricsText ->
                lyricsViewModel.handleLyricsAction(
                    LyricsAction.SaveLyrics(playerState.currentMedia!!.id, lyricsText)
                )
                showEditor = false
            }
        )
    }

    // Online search dialog
    if (showSearchDialog && playerState.currentMedia != null) {
        OnlineSearchDialog(
            mediaItem = playerState.currentMedia!!,
            onDismiss = { showSearchDialog = false },
            onSearch = { title, artist, album ->
                lyricsViewModel.searchOnlineLyrics(title, artist, album)
                showSearchDialog = false
            }
        )
    }

    // Error handling
    lyricsState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            lyricsViewModel.clearError()
        }
    }
}

@Composable
private fun OnlineSearchDialog(
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    onSearch: (String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf(mediaItem.displayTitle) }
    var artist by remember { mutableStateOf(mediaItem.displayArtist) }
    var album by remember { mutableStateOf(mediaItem.album ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Lyrics Online") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSearch(title, artist, album.takeIf { it.isNotBlank() })
                },
                enabled = title.isNotBlank() && artist.isNotBlank()
            ) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
