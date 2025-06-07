package com.mediaplayer.data.repository

import android.content.Context
import android.provider.MediaStore
import com.mediaplayer.data.database.MediaDao
import com.mediaplayer.data.models.*
import com.mediaplayer.utils.MediaUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val mediaDao: MediaDao,
    @ApplicationContext private val context: Context
) {

    // Media Items
    fun getAllAudioItems(): Flow<List<MediaItem>> = mediaDao.getAllAudioItems()
    fun getAllVideoItems(): Flow<List<MediaItem>> = mediaDao.getAllVideoItems()
    fun getAllMediaItems(): Flow<List<MediaItem>> = mediaDao.getAllMediaItems()
    fun getFavoriteItems(): Flow<List<MediaItem>> = mediaDao.getFavoriteItems()
    fun searchMediaItems(query: String): Flow<List<MediaItem>> = mediaDao.searchMediaItems(query)

    suspend fun getMediaItemById(id: String): MediaItem? = mediaDao.getMediaItemById(id)

    suspend fun toggleFavorite(mediaItem: MediaItem) {
        mediaDao.updateFavoriteStatus(mediaItem.id, !mediaItem.isFavorite)
    }

    suspend fun incrementPlayCount(mediaId: String) {
        mediaDao.incrementPlayCount(mediaId)
    }

    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>> = mediaDao.getAllPlaylists()
    fun getAllPlaylistsWithMedia(): Flow<List<PlaylistWithMedia>> = mediaDao.getAllPlaylistsWithMedia()
    fun getPlaylistMedia(playlistId: String): Flow<List<MediaItem>> = mediaDao.getPlaylistMedia(playlistId)

    suspend fun getPlaylistById(id: String): Playlist? = mediaDao.getPlaylistById(id)
    suspend fun getPlaylistWithMedia(id: String): PlaylistWithMedia? = mediaDao.getPlaylistWithMedia(id)

    suspend fun createPlaylist(name: String, description: String? = null): Playlist {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description
        )
        mediaDao.insertPlaylist(playlist)
        return playlist
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        mediaDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        mediaDao.deleteAllPlaylistMedia(playlist.id)
        mediaDao.deletePlaylist(playlist)
    }

    suspend fun addMediaToPlaylist(playlistId: String, mediaId: String) {
        val crossRef = PlaylistMediaCrossRef(
            playlistId = playlistId,
            mediaId = mediaId,
            position = 0 // You might want to calculate the actual position
        )
        mediaDao.insertPlaylistMediaCrossRef(crossRef)
    }

    suspend fun removeMediaFromPlaylist(playlistId: String, mediaId: String) {
        mediaDao.removeMediaFromPlaylist(playlistId, mediaId)
    }

    // Scan device for media files
    suspend fun scanForMediaFiles(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaItems = mutableListOf<MediaItem>()

        // Scan for audio files
        mediaItems.addAll(scanAudioFiles())

        // Scan for video files
        mediaItems.addAll(scanVideoFiles())

        // Save to database
        mediaDao.insertMediaItems(mediaItems)

        mediaItems
    }

    private suspend fun scanAudioFiles(): List<MediaItem> = withContext(Dispatchers.IO) {
        val audioItems = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn).toString()
                val title = it.getString(titleColumn) ?: ""
                val artist = it.getString(artistColumn)
                val album = it.getString(albumColumn)
                val duration = it.getLong(durationColumn)
                val path = it.getString(dataColumn) ?: ""
                val mimeType = it.getString(mimeTypeColumn) ?: ""
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn) * 1000 // Convert to milliseconds
                val mediaId = "audio_$id"

                // Extract and cache thumbnail (album art)
                val thumbnailPath = MediaUtils.extractAndCacheAudioThumbnail(context, path, mediaId)

                audioItems.add(
                    MediaItem(
                        id = mediaId,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        mimeType = mimeType,
                        size = size,
                        dateAdded = dateAdded,
                        albumArt = thumbnailPath,
                        isVideo = false
                    )
                )
            }
        }

        audioItems
    }

    private suspend fun scanVideoFiles(): List<MediaItem> = withContext(Dispatchers.IO) {
        val videoItems = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Video.Media.TITLE + " ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn).toString()
                val title = it.getString(titleColumn) ?: ""
                val duration = it.getLong(durationColumn)
                val path = it.getString(dataColumn) ?: ""
                val mimeType = it.getString(mimeTypeColumn) ?: ""
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn) * 1000 // Convert to milliseconds
                val mediaId = "video_$id"

                // Extract and cache thumbnail (video frame)
                val thumbnailPath = MediaUtils.extractAndCacheVideoThumbnail(context, path, mediaId)

                videoItems.add(
                    MediaItem(
                        id = mediaId,
                        title = title,
                        duration = duration,
                        path = path,
                        mimeType = mimeType,
                        size = size,
                        dateAdded = dateAdded,
                        albumArt = thumbnailPath,
                        isVideo = true
                    )
                )
            }
        }

        videoItems
    }

    // Thumbnail management
    suspend fun clearThumbnailCache() {
        MediaUtils.clearThumbnailCache(context)
    }

    suspend fun getThumbnailCacheSize(): Long {
        return MediaUtils.getThumbnailCacheSize(context)
    }

    suspend fun regenerateThumbnails() = withContext(Dispatchers.IO) {
        // Clear existing cache
        MediaUtils.clearThumbnailCache(context)

        // Re-scan all media files to regenerate thumbnails
        scanForMediaFiles()
    }
}
