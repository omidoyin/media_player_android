package com.mediaplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediaplayer.data.models.PlayerAction
import com.mediaplayer.ui.components.MiniPlayerControls
import com.mediaplayer.ui.components.PlayerControls
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel
import com.mediaplayer.ui.viewmodels.MediaTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MediaPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showFullPlayer by remember { mutableStateOf(false) }

    when {
        showSearch -> {
            SearchScreen(
                onBack = { showSearch = false },
                modifier = Modifier.fillMaxSize()
            )
        }
        showLyrics -> {
            LyricsScreen(
                onBack = { showLyrics = false },
                modifier = Modifier.fillMaxSize()
            )
        }
        showFullPlayer -> {
            FullScreenPlayerScreen(
                onBack = { showFullPlayer = false },
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media Player") },
                actions = {
                    IconButton(
                        onClick = { viewModel.scanForMediaFiles() }
                    ) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan for media"
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSearch = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Mini player when media is playing
            if (playerState.currentMedia != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    MiniPlayerControls(
                        playerState = playerState,
                        onAction = viewModel::handlePlayerAction,
                        onExpandToFullPlayer = { showFullPlayer = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal
            ) {
                MediaTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    MediaTab.AUDIO -> "Audio"
                                    MediaTab.VIDEO -> "Video"
                                    MediaTab.PLAYLISTS -> "Playlists"
                                    MediaTab.FAVORITES -> "Favorites"
                                    MediaTab.SETTINGS -> "Settings"
                                }
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    MediaTab.AUDIO -> Icons.Default.AudioFile
                                    MediaTab.VIDEO -> Icons.Default.VideoFile
                                    MediaTab.PLAYLISTS -> Icons.Default.PlaylistPlay
                                    MediaTab.FAVORITES -> Icons.Default.Favorite
                                    MediaTab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            // Content based on selected tab
            when (uiState.selectedTab) {
                MediaTab.AUDIO -> {
                    AudioScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaTab.VIDEO -> {
                    VideoScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaTab.PLAYLISTS -> {
                    PlaylistScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaTab.FAVORITES -> {
                    FavoritesScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar or dialog
                viewModel.clearError()
            }
        }
    }
}
}
