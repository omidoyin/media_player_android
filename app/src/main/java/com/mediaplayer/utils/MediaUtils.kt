package com.mediaplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object MediaUtils {

    private const val TAG = "MediaUtils"
    private const val THUMBNAIL_DIR = "thumbnails"
    private const val THUMBNAIL_SIZE = 512
    private const val THUMBNAIL_QUALITY = 85

    /**
     * Get duration of media file in milliseconds
     */
    fun getMediaDuration(context: Context, path: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(File(path)))
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting media duration for $path", e)
            0L
        }
    }

    /**
     * Get album art from media file
     */
    fun getAlbumArt(context: Context, path: String): ByteArray? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(File(path)))
            val art = retriever.embeddedPicture
            retriever.release()
            art
        } catch (e: Exception) {
            Log.e(TAG, "Error getting album art for $path", e)
            null
        }
    }

    /**
     * Extract and cache thumbnail for audio file (album art)
     */
    suspend fun extractAndCacheAudioThumbnail(context: Context, mediaPath: String, mediaId: String): String? = withContext(Dispatchers.IO) {
        try {
            val thumbnailFile = getThumbnailFile(context, mediaId)

            // Return existing thumbnail if it exists
            if (thumbnailFile.exists()) {
                return@withContext thumbnailFile.absolutePath
            }

            // Extract album art
            val albumArtBytes = getAlbumArt(context, mediaPath)
            if (albumArtBytes != null) {
                // Save to cache
                thumbnailFile.parentFile?.mkdirs()
                FileOutputStream(thumbnailFile).use { output ->
                    output.write(albumArtBytes)
                }
                return@withContext thumbnailFile.absolutePath
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting audio thumbnail for $mediaPath", e)
            null
        }
    }

    /**
     * Extract and cache thumbnail for video file
     */
    suspend fun extractAndCacheVideoThumbnail(context: Context, mediaPath: String, mediaId: String): String? = withContext(Dispatchers.IO) {
        try {
            val thumbnailFile = getThumbnailFile(context, mediaId)

            // Return existing thumbnail if it exists
            if (thumbnailFile.exists()) {
                return@withContext thumbnailFile.absolutePath
            }

            // Extract video frame
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(File(mediaPath)))

            // Get frame at 1 second or 10% of duration, whichever is smaller
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            val timeUs = minOf(1000000L, duration * 100L) // 1 second or 10% of duration in microseconds

            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()

            if (bitmap != null) {
                // Resize and compress bitmap
                val resizedBitmap = resizeBitmap(bitmap, THUMBNAIL_SIZE)

                // Save to cache
                thumbnailFile.parentFile?.mkdirs()
                FileOutputStream(thumbnailFile).use { output ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, output)
                }

                bitmap.recycle()
                resizedBitmap.recycle()

                return@withContext thumbnailFile.absolutePath
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting video thumbnail for $mediaPath", e)
            null
        }
    }

    /**
     * Get thumbnail file for a media item
     */
    private fun getThumbnailFile(context: Context, mediaId: String): File {
        val thumbnailDir = File(context.cacheDir, THUMBNAIL_DIR)
        val hashedId = hashString(mediaId)
        return File(thumbnailDir, "$hashedId.jpg")
    }

    /**
     * Hash string to create consistent filename
     */
    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Resize bitmap while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Clear thumbnail cache
     */
    suspend fun clearThumbnailCache(context: Context) = withContext(Dispatchers.IO) {
        try {
            val thumbnailDir = File(context.cacheDir, THUMBNAIL_DIR)
            if (thumbnailDir.exists()) {
                thumbnailDir.deleteRecursively()
            }
            Log.i(TAG, "Thumbnail cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing thumbnail cache", e)
        }
    }

    /**
     * Get cache size in bytes
     */
    suspend fun getThumbnailCacheSize(context: Context): Long = withContext(Dispatchers.IO) {
        try {
            val thumbnailDir = File(context.cacheDir, THUMBNAIL_DIR)
            if (thumbnailDir.exists()) {
                return@withContext thumbnailDir.walkTopDown()
                    .filter { it.isFile }
                    .map { it.length() }
                    .sum()
            }
            0L
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
            0L
        }
    }

    /**
     * Delete specific thumbnail
     */
    suspend fun deleteThumbnail(context: Context, mediaId: String) {
        withContext(Dispatchers.IO) {
            try {
                val thumbnailFile = getThumbnailFile(context, mediaId)
                if (thumbnailFile.exists()) {
                    thumbnailFile.delete()
                    Log.d(TAG, "Deleted thumbnail for media ID: $mediaId")
                } else {
                    Log.d(TAG, "Thumbnail file does not exist for media ID: $mediaId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting thumbnail for $mediaId", e)
            }
        }
    }

    /**
     * Format time in milliseconds to MM:SS or HH:MM:SS format
     */
    fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    /**
     * Format file size in bytes to human readable format
     */
    fun formatFileSize(bytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            bytes >= gb -> String.format("%.1f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format("%.1f MB", bytes.toDouble() / mb)
            bytes >= kb -> String.format("%.1f KB", bytes.toDouble() / kb)
            else -> "$bytes B"
        }
    }

    /**
     * Check if file is a supported audio format
     */
    fun isAudioFile(mimeType: String): Boolean {
        return mimeType.startsWith("audio/")
    }

    /**
     * Check if file is a supported video format
     */
    fun isVideoFile(mimeType: String): Boolean {
        return mimeType.startsWith("video/")
    }

    /**
     * Get file extension from path
     */
    fun getFileExtension(path: String): String {
        return path.substringAfterLast(".", "")
    }

    /**
     * Get filename without extension from path
     */
    fun getFileNameWithoutExtension(path: String): String {
        val fileName = path.substringAfterLast("/")
        return fileName.substringBeforeLast(".")
    }

    /**
     * Check if the media file exists and is readable
     */
    fun isMediaFileValid(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.canRead() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get supported audio mime types
     */
    fun getSupportedAudioMimeTypes(): List<String> {
        return listOf(
            "audio/mpeg",
            "audio/mp4",
            "audio/aac",
            "audio/ogg",
            "audio/wav",
            "audio/flac",
            "audio/x-wav",
            "audio/3gpp",
            "audio/amr"
        )
    }

    /**
     * Get supported video mime types
     */
    fun getSupportedVideoMimeTypes(): List<String> {
        return listOf(
            "video/mp4",
            "video/3gpp",
            "video/avi",
            "video/mkv",
            "video/webm",
            "video/x-msvideo",
            "video/quicktime"
        )
    }
}
