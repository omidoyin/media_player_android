package com.mediaplayer.data.repository

import android.content.Context
import com.mediaplayer.data.database.MediaDao
import com.mediaplayer.data.models.*
import com.mediaplayer.utils.LyricsUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    private val mediaDao: MediaDao,
    @ApplicationContext private val context: Context
) {

    suspend fun getLyrics(mediaId: String): Lyrics? {
        return mediaDao.getLyrics(mediaId)
    }

    suspend fun saveLyrics(lyrics: Lyrics) {
        mediaDao.insertLyrics(lyrics)
    }

    suspend fun deleteLyrics(mediaId: String) {
        mediaDao.deleteLyricsByMediaId(mediaId)
    }

    suspend fun loadLyricsForMedia(mediaItem: MediaItem): Lyrics? = withContext(Dispatchers.IO) {
        // First check if we have cached lyrics
        val cachedLyrics = getLyrics(mediaItem.id)
        if (cachedLyrics != null) {
            return@withContext cachedLyrics
        }

        // Try to find local lyrics file
        val localLyrics = findLocalLyricsFile(mediaItem.path)
        if (localLyrics != null) {
            val lyrics = Lyrics(
                mediaId = mediaItem.id,
                content = localLyrics,
                isTimeSynced = LyricsUtils.isTimeSynced(localLyrics),
                source = LyricsSource.LOCAL
            )
            saveLyrics(lyrics)
            return@withContext lyrics
        }

        // Try to fetch from online sources
        val onlineLyrics = fetchOnlineLyrics(mediaItem)
        if (onlineLyrics != null) {
            val lyrics = Lyrics(
                mediaId = mediaItem.id,
                content = onlineLyrics,
                isTimeSynced = LyricsUtils.isTimeSynced(onlineLyrics),
                source = LyricsSource.ONLINE
            )
            saveLyrics(lyrics)
            return@withContext lyrics
        }

        return@withContext null
    }

    private suspend fun findLocalLyricsFile(mediaPath: String): String? = withContext(Dispatchers.IO) {
        try {
            val mediaFile = File(mediaPath)
            val parentDir = mediaFile.parentFile ?: return@withContext null
            val baseName = mediaFile.nameWithoutExtension

            // Look for .lrc file with same name
            val lrcFile = File(parentDir, "$baseName.lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                return@withContext lrcFile.readText()
            }

            // Look for .txt file with same name
            val txtFile = File(parentDir, "$baseName.txt")
            if (txtFile.exists() && txtFile.canRead()) {
                return@withContext txtFile.readText()
            }

            return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private suspend fun fetchOnlineLyrics(mediaItem: MediaItem): String? = withContext(Dispatchers.IO) {
        // This is a placeholder for online lyrics fetching
        // You can integrate with services like:
        // - Genius API
        // - Musixmatch API
        // - LyricFind API
        // - AZLyrics (web scraping)

        try {
            // Example implementation would go here
            // For now, return null as we don't have API keys
            return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun searchOnlineLyrics(
        title: String,
        artist: String,
        album: String? = null
    ): String? = withContext(Dispatchers.IO) {
        // Placeholder for online lyrics search
        // This would implement actual API calls to lyrics services
        return@withContext null
    }

    suspend fun saveLyricsFromText(mediaId: String, lyricsText: String) {
        val lyrics = Lyrics(
            mediaId = mediaId,
            content = lyricsText,
            isTimeSynced = LyricsUtils.isTimeSynced(lyricsText),
            source = LyricsSource.MANUAL
        )
        saveLyrics(lyrics)
    }

    suspend fun updateLyrics(lyrics: Lyrics) {
        mediaDao.updateLyrics(lyrics)
    }

    fun parseLyricsToLines(lyricsContent: String): List<LyricsLine> {
        return LyricsUtils.parseLyrics(lyricsContent)
    }

    suspend fun exportLyricsToFile(mediaItem: MediaItem, lyrics: Lyrics): Boolean = withContext(Dispatchers.IO) {
        try {
            val mediaFile = File(mediaItem.path)
            val parentDir = mediaFile.parentFile ?: return@withContext false
            val baseName = mediaFile.nameWithoutExtension

            val lyricsFile = File(parentDir, "$baseName.lrc")
            lyricsFile.writeText(lyrics.content)
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }

    suspend fun importLyricsFromFile(mediaId: String, filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists() && file.canRead()) {
                val content = file.readText()
                saveLyricsFromText(mediaId, content)
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            return@withContext false
        }
    }
}
