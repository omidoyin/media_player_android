package com.mediaplayer.data.database

import androidx.room.*
import com.mediaplayer.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    // Media Items
    @Query("SELECT * FROM media_items WHERE isVideo = 0 ORDER BY title ASC")
    fun getAllAudioItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isVideo = 1 ORDER BY title ASC")
    fun getAllVideoItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items ORDER BY title ASC")
    fun getAllMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: String): MediaItem?

    @Query("""
        SELECT * FROM media_items
        WHERE title LIKE '%' || :query || '%'
        OR artist LIKE '%' || :query || '%'
        OR album LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchMediaItems(query: String): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(mediaItem: MediaItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(mediaItems: List<MediaItem>)

    @Update
    suspend fun updateMediaItem(mediaItem: MediaItem)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE media_items SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMediaItem(mediaItem: MediaItem)

    @Query("DELETE FROM media_items")
    suspend fun deleteAllMediaItems()

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: String): Playlist?

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistWithMedia(id: String): PlaylistWithMedia?

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylistsWithMedia(): Flow<List<PlaylistWithMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    // Playlist Media Relations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistMediaCrossRef(crossRef: PlaylistMediaCrossRef)

    @Delete
    suspend fun deletePlaylistMediaCrossRef(crossRef: PlaylistMediaCrossRef)

    @Query("DELETE FROM playlist_media_cross_ref WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistMedia(playlistId: String)

    @Query("DELETE FROM playlist_media_cross_ref WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun removeMediaFromPlaylist(playlistId: String, mediaId: String)

    @Query("""
        SELECT m.* FROM media_items m
        INNER JOIN playlist_media_cross_ref pm ON m.id = pm.mediaId
        WHERE pm.playlistId = :playlistId
        ORDER BY pm.position ASC
    """)
    fun getPlaylistMedia(playlistId: String): Flow<List<MediaItem>>

    // Lyrics
    @Query("SELECT * FROM lyrics WHERE mediaId = :mediaId")
    suspend fun getLyrics(mediaId: String): Lyrics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: Lyrics)

    @Update
    suspend fun updateLyrics(lyrics: Lyrics)

    @Delete
    suspend fun deleteLyrics(lyrics: Lyrics)

    @Query("DELETE FROM lyrics WHERE mediaId = :mediaId")
    suspend fun deleteLyricsByMediaId(mediaId: String)
}
