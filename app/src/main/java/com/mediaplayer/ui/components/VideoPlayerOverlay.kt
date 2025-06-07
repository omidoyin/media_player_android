package com.mediaplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediaplayer.data.models.PlayerAction
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerOverlay(
    onAction: (PlayerAction) -> Unit,
    onShowControls: () -> Unit,
    modifier: Modifier = Modifier,
    seekSeconds: Int = 10
) {
    var showLeftFeedback by remember { mutableStateOf(false) }
    var showRightFeedback by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Center tap zone (show controls)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onShowControls()
                }
        )

        // Left tap zone (seek backward)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onAction(PlayerAction.SeekBackward(seekSeconds))
                    showLeftFeedback = true
                    onShowControls() // Also show controls when seeking
                },
            contentAlignment = Alignment.Center
        ) {
            // Left feedback animation
            AnimatedVisibility(
                visible = showLeftFeedback,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
            ) {
                SeekFeedback(
                    icon = Icons.Default.FastRewind,
                    text = "-${seekSeconds}s",
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }

        // Right tap zone (seek forward)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onAction(PlayerAction.SeekForward(seekSeconds))
                    showRightFeedback = true
                    onShowControls() // Also show controls when seeking
                },
            contentAlignment = Alignment.Center
        ) {
            // Right feedback animation
            AnimatedVisibility(
                visible = showRightFeedback,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
            ) {
                SeekFeedback(
                    icon = Icons.Default.FastForward,
                    text = "+${seekSeconds}s",
                    modifier = Modifier.padding(end = 32.dp)
                )
            }
        }

        // Small seek buttons in corners
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Backward button
            IconButton(
                onClick = {
                    onAction(PlayerAction.SeekBackward(seekSeconds))
                    showLeftFeedback = true
                    onShowControls()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Seek backward $seekSeconds seconds",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Forward button
            IconButton(
                onClick = {
                    onAction(PlayerAction.SeekForward(seekSeconds))
                    showRightFeedback = true
                    onShowControls()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Seek forward $seekSeconds seconds",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Auto-hide feedback after delay
    LaunchedEffect(showLeftFeedback) {
        if (showLeftFeedback) {
            delay(1000)
            showLeftFeedback = false
        }
    }

    LaunchedEffect(showRightFeedback) {
        if (showRightFeedback) {
            delay(1000)
            showRightFeedback = false
        }
    }
}

@Composable
private fun SeekFeedback(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
