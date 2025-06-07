package com.mediaplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediaplayer.data.models.*
import kotlinx.coroutines.launch

@Composable
fun LyricsDisplay(
    lyricsState: LyricsDisplayState,
    onAction: (LyricsAction) -> Unit,
    onSeekToPosition: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll to current line
    LaunchedEffect(lyricsState.currentLineIndex, lyricsState.autoScroll) {
        if (lyricsState.autoScroll && lyricsState.currentLineIndex >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = maxOf(0, lyricsState.currentLineIndex - 2),
                    scrollOffset = 0
                )
            }
        }
    }
    
    Column(modifier = modifier) {
        // Lyrics controls
        LyricsControls(
            lyricsState = lyricsState,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth()
        )
        
        when {
            lyricsState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            lyricsState.error != null -> {
                LyricsErrorState(
                    error = lyricsState.error,
                    onRetry = { /* Implement retry logic */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            lyricsState.lyrics.isEmpty() -> {
                LyricsEmptyState(
                    onSearchOnline = { /* Implement online search */ },
                    onAddManually = { /* Implement manual add */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = lyricsState.lyrics,
                        key = { index, _ -> index }
                    ) { index, lyricsLine ->
                        LyricsLineItem(
                            line = lyricsLine,
                            isCurrentLine = index == lyricsState.currentLineIndex,
                            fontSize = lyricsState.fontSize,
                            onClick = {
                                onSeekToPosition(lyricsLine.timestamp)
                                onAction(LyricsAction.SeekToLine(index))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsControls(
    lyricsState: LyricsDisplayState,
    onAction: (LyricsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto-scroll toggle
            IconButton(
                onClick = { onAction(LyricsAction.ToggleAutoScroll) }
            ) {
                Icon(
                    imageVector = if (lyricsState.autoScroll) 
                        Icons.Default.LockOpen 
                    else 
                        Icons.Default.Lock,
                    contentDescription = if (lyricsState.autoScroll) 
                        "Disable auto-scroll" 
                    else 
                        "Enable auto-scroll",
                    tint = if (lyricsState.autoScroll) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Font size controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val currentIndex = LyricsFontSize.values().indexOf(lyricsState.fontSize)
                        if (currentIndex > 0) {
                            onAction(LyricsAction.SetFontSize(LyricsFontSize.values()[currentIndex - 1]))
                        }
                    },
                    enabled = lyricsState.fontSize != LyricsFontSize.SMALL
                ) {
                    Icon(
                        imageVector = Icons.Default.TextDecrease,
                        contentDescription = "Decrease font size"
                    )
                }
                
                Text(
                    text = when (lyricsState.fontSize) {
                        LyricsFontSize.SMALL -> "A"
                        LyricsFontSize.MEDIUM -> "A"
                        LyricsFontSize.LARGE -> "A"
                        LyricsFontSize.EXTRA_LARGE -> "A"
                    },
                    fontSize = (16 * lyricsState.fontSize.scale).sp,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = {
                        val currentIndex = LyricsFontSize.values().indexOf(lyricsState.fontSize)
                        if (currentIndex < LyricsFontSize.values().size - 1) {
                            onAction(LyricsAction.SetFontSize(LyricsFontSize.values()[currentIndex + 1]))
                        }
                    },
                    enabled = lyricsState.fontSize != LyricsFontSize.EXTRA_LARGE
                ) {
                    Icon(
                        imageVector = Icons.Default.TextIncrease,
                        contentDescription = "Increase font size"
                    )
                }
            }
            
            // More options
            IconButton(
                onClick = { /* Show more options menu */ }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }
    }
}

@Composable
private fun LyricsLineItem(
    line: LyricsLine,
    isCurrentLine: Boolean,
    fontSize: LyricsFontSize,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrentLine) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else 
            Color.Transparent,
        animationSpec = tween(300),
        label = "background_color"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isCurrentLine) 
            MaterialTheme.colorScheme.primary
        else 
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "text_color"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = line.text,
            fontSize = (16 * fontSize.scale).sp,
            fontWeight = if (isCurrentLine) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LyricsEmptyState(
    onSearchOnline: () -> Unit,
    onAddManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
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
            text = "No lyrics available",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = "Search online or add lyrics manually",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onSearchOnline
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Online")
            }
            
            Button(
                onClick = onAddManually
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Manually")
            }
        }
    }
}

@Composable
private fun LyricsErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Failed to load lyrics",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
