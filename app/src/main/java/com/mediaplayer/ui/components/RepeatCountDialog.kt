package com.mediaplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatCountDialog(
    currentCount: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var repeatCount by remember { mutableStateOf(currentCount.toString()) }
    var isError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Set Repeat Count",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "How many times should this track repeat?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(2, 3, 5, 10).forEach { count ->
                        FilterChip(
                            onClick = { 
                                repeatCount = count.toString()
                                isError = false
                            },
                            label = { Text("${count}x") },
                            selected = repeatCount == count.toString()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom input
                OutlinedTextField(
                    value = repeatCount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            repeatCount = newValue
                            isError = newValue.isEmpty() || newValue.toIntOrNull()?.let { it < 1 || it > 999 } == true
                        }
                    },
                    label = { Text("Custom count") },
                    placeholder = { Text("Enter number (1-999)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Please enter a number between 1 and 999") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val count = repeatCount.toIntOrNull()
                            if (count != null && count in 1..999) {
                                onConfirm(count)
                            }
                        },
                        enabled = !isError && repeatCount.isNotEmpty()
                    ) {
                        Text("Set Repeat")
                    }
                }
            }
        }
    }
}
