package com.mediaplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MediaPlayerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showRegenerateThumbnailsDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf(0L) }

    // Load cache size when screen is displayed
    LaunchedEffect(Unit) {
        cacheSize = viewModel.getThumbnailCacheSize()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsSection(title = "Thumbnails") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Cache size info
                    SettingsInfoItem(
                        title = "Cache Size",
                        subtitle = formatFileSize(cacheSize),
                        icon = Icons.Default.Storage
                    )

                    // Clear cache
                    SettingsActionItem(
                        title = "Clear Thumbnail Cache",
                        subtitle = "Remove all cached thumbnails",
                        icon = Icons.Default.Delete,
                        onClick = { showClearCacheDialog = true }
                    )

                    // Regenerate thumbnails
                    SettingsActionItem(
                        title = "Regenerate Thumbnails",
                        subtitle = "Re-scan and generate all thumbnails",
                        icon = Icons.Default.Refresh,
                        onClick = { showRegenerateThumbnailsDialog = true },
                        enabled = !uiState.isScanning
                    )
                }
            }
        }

        item {
            SettingsSection(title = "Media Library") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Scan media
                    SettingsActionItem(
                        title = "Scan for Media Files",
                        subtitle = "Search for new audio and video files",
                        icon = Icons.Default.Search,
                        onClick = { viewModel.scanForMediaFiles() },
                        enabled = !uiState.isScanning
                    )

                    if (uiState.isScanning) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scanning...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "About") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsInfoItem(
                        title = "Version",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info
                    )

                    SettingsInfoItem(
                        title = "Developer",
                        subtitle = "Media Player Team",
                        icon = Icons.Default.Person
                    )
                }
            }
        }
    }

    // Clear cache confirmation dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Thumbnail Cache") },
            text = { Text("This will remove all cached thumbnails. They will be regenerated when needed. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.clearThumbnailCache()
                            cacheSize = 0L
                        }
                        showClearCacheDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Regenerate thumbnails confirmation dialog
    if (showRegenerateThumbnailsDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateThumbnailsDialog = false },
            title = { Text("Regenerate Thumbnails") },
            text = { Text("This will clear the cache and regenerate all thumbnails. This may take some time. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.regenerateThumbnails()
                        showRegenerateThumbnailsDialog = false
                    }
                ) {
                    Text("Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateThumbnailsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        modifier = Modifier.clickable(enabled = enabled) { onClick() }
    )
}

@Composable
private fun SettingsInfoItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
