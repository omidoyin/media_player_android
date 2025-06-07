package com.mediaplayer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long = 0L,
    val path: String,
    val mimeType: String,
    val size: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val albumArt: String? = null,
    val isVideo: Boolean = false,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
) {
    val isAudio: Boolean get() = !isVideo
    
    val displayTitle: String get() = title.ifEmpty { 
        path.substringAfterLast("/").substringBeforeLast(".")
    }
    
    val displayArtist: String get() = artist ?: "Unknown Artist"
    
    val displayAlbum: String get() = album ?: "Unknown Album"
    
    val formattedDuration: String get() {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
    
    val formattedSize: String get() {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
}

enum class MediaType {
    AUDIO, VIDEO, ALL
}

enum class SortOrder {
    TITLE_ASC, TITLE_DESC,
    ARTIST_ASC, ARTIST_DESC,
    ALBUM_ASC, ALBUM_DESC,
    DURATION_ASC, DURATION_DESC,
    DATE_ADDED_ASC, DATE_ADDED_DESC,
    PLAY_COUNT_ASC, PLAY_COUNT_DESC
}
