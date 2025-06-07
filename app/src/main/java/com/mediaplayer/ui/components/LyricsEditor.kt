package com.mediaplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mediaplayer.data.models.MediaItem
import com.mediaplayer.utils.LyricsUtils

@Composable
fun LyricsEditorDialog(
    mediaItem: MediaItem,
    initialLyrics: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lyricsText by remember { mutableStateOf(initialLyrics) }
    var isLrcFormat by remember { mutableStateOf(LyricsUtils.isValidLrcFormat(initialLyrics)) }
    var showPreview by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                LyricsEditorHeader(
                    mediaItem = mediaItem,
                    showPreview = showPreview,
                    onTogglePreview = { showPreview = !showPreview },
                    onDismiss = onDismiss,
                    onSave = { onSave(lyricsText) },
                    canSave = lyricsText.isNotBlank()
                )
                
                Divider()
                
                if (showPreview) {
                    // Preview mode
                    LyricsPreview(
                        lyricsText = lyricsText,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Edit mode
                    LyricsEditArea(
                        lyricsText = lyricsText,
                        onLyricsChange = { 
                            lyricsText = it
                            isLrcFormat = LyricsUtils.isValidLrcFormat(it)
                        },
                        isLrcFormat = isLrcFormat,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Divider()
                
                // Footer with format info and tools
                LyricsEditorFooter(
                    isLrcFormat = isLrcFormat,
                    onInsertTimestamp = { timestamp ->
                        val cursor = lyricsText.length // Simple append for now
                        lyricsText = lyricsText + "\n[${LyricsUtils.formatTimestamp(timestamp)}]"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsEditorHeader(
    mediaItem: MediaItem,
    showPreview: Boolean,
    onTogglePreview: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Edit Lyrics",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${mediaItem.displayTitle} - ${mediaItem.displayArtist}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        },
        actions = {
            IconButton(onClick = onTogglePreview) {
                Icon(
                    imageVector = if (showPreview) Icons.Default.Edit else Icons.Default.Preview,
                    contentDescription = if (showPreview) "Edit" else "Preview"
                )
            }
            
            TextButton(
                onClick = onSave,
                enabled = canSave
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun LyricsEditArea(
    lyricsText: String,
    onLyricsChange: (String) -> Unit,
    isLrcFormat: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Format indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isLrcFormat) Icons.Default.Schedule else Icons.Default.TextFields,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isLrcFormat) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = if (isLrcFormat) "Time-synced lyrics (LRC format)" else "Plain text lyrics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Text editor
        OutlinedTextField(
            value = lyricsText,
            onValueChange = onLyricsChange,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            placeholder = {
                Text(
                    text = if (isLrcFormat) {
                        "[00:12.34]First line of lyrics\n[00:15.67]Second line of lyrics\n..."
                    } else {
                        "Enter lyrics here...\n\nFor time-synced lyrics, use LRC format:\n[mm:ss.xx]lyrics text"
                    }
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            maxLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun LyricsPreview(
    lyricsText: String,
    modifier: Modifier = Modifier
) {
    val lyricsLines = remember(lyricsText) {
        LyricsUtils.parseLyrics(lyricsText)
    }
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (lyricsLines.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No lyrics to preview",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            lyricsLines.forEach { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (line.timestamp > 0) {
                        Text(
                            text = LyricsUtils.formatTimestamp(line.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(80.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsEditorFooter(
    isLrcFormat: Boolean,
    onInsertTimestamp: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        if (isLrcFormat) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LRC Format Tools",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                OutlinedButton(
                    onClick = { onInsertTimestamp(0) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Insert Timestamp")
                }
            }
        }
        
        // Format help
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Format Help",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "• Plain text: Just enter lyrics line by line\n" +
                           "• LRC format: [mm:ss.xx]lyrics text\n" +
                           "• Example: [01:23.45]This is a lyrics line",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
