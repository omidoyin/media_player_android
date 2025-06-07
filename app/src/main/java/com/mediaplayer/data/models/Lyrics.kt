package com.mediaplayer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class Lyrics(
    @PrimaryKey val mediaId: String,
    val content: String,
    val isTimeSynced: Boolean = false,
    val source: LyricsSource = LyricsSource.LOCAL,
    val language: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class LyricsSource {
    LOCAL,      // From .lrc files or embedded in media
    ONLINE,     // Fetched from online services
    MANUAL      // User-provided
}

data class LyricsLine(
    val timestamp: Long,    // Time in milliseconds
    val text: String,
    val isHighlighted: Boolean = false
) {
    companion object {
        fun fromLrcLine(line: String): LyricsLine? {
            // Parse LRC format: [mm:ss.xx]lyrics text
            val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2})\](.*)""")
            val matchResult = regex.find(line.trim())
            
            return matchResult?.let { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0
                val seconds = match.groupValues[2].toLongOrNull() ?: 0
                val centiseconds = match.groupValues[3].toLongOrNull() ?: 0
                val text = match.groupValues[4].trim()
                
                val timestamp = (minutes * 60 + seconds) * 1000 + centiseconds * 10
                LyricsLine(timestamp, text)
            }
        }
    }
}

data class LyricsDisplayState(
    val lyrics: List<LyricsLine> = emptyList(),
    val currentLineIndex: Int = -1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLyrics: Boolean = false,
    val autoScroll: Boolean = true,
    val fontSize: LyricsFontSize = LyricsFontSize.MEDIUM
)

enum class LyricsFontSize(val scale: Float) {
    SMALL(0.8f),
    MEDIUM(1.0f),
    LARGE(1.2f),
    EXTRA_LARGE(1.4f)
}

sealed class LyricsAction {
    object ToggleLyrics : LyricsAction()
    object ToggleAutoScroll : LyricsAction()
    data class SetFontSize(val size: LyricsFontSize) : LyricsAction()
    data class SeekToLine(val lineIndex: Int) : LyricsAction()
    data class UpdateCurrentPosition(val position: Long) : LyricsAction()
    data class LoadLyrics(val mediaId: String) : LyricsAction()
    data class SaveLyrics(val mediaId: String, val lyrics: String) : LyricsAction()
}
