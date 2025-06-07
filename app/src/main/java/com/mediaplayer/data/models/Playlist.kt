package com.mediaplayer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val coverArt: String? = null
)

@Entity(
    tableName = "playlist_media_cross_ref",
    primaryKeys = ["playlistId", "mediaId"]
)
data class PlaylistMediaCrossRef(
    val playlistId: String,
    val mediaId: String,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

data class PlaylistWithMedia(
    @androidx.room.Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistMediaCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "mediaId"
        )
    )
    val mediaItems: List<MediaItem>
) {
    val totalDuration: Long get() = mediaItems.sumOf { it.duration }
    val itemCount: Int get() = mediaItems.size
    
    val formattedDuration: String get() {
        val hours = totalDuration / 3600000
        val minutes = (totalDuration % 3600000) / 60000
        return if (hours > 0) {
            String.format("%d:%02d hours", hours, minutes)
        } else {
            String.format("%d minutes", minutes)
        }
    }
}
