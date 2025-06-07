package com.mediaplayer

import com.mediaplayer.utils.MediaUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MediaUtils
 */
class MediaUtilsTest {
    
    @Test
    fun formatTime_correctlyFormatsSeconds() {
        assertEquals("0:30", MediaUtils.formatTime(30000))
        assertEquals("1:00", MediaUtils.formatTime(60000))
        assertEquals("1:30", MediaUtils.formatTime(90000))
    }
    
    @Test
    fun formatTime_correctlyFormatsMinutes() {
        assertEquals("2:30", MediaUtils.formatTime(150000))
        assertEquals("10:00", MediaUtils.formatTime(600000))
        assertEquals("59:59", MediaUtils.formatTime(3599000))
    }
    
    @Test
    fun formatTime_correctlyFormatsHours() {
        assertEquals("1:00:00", MediaUtils.formatTime(3600000))
        assertEquals("1:30:45", MediaUtils.formatTime(5445000))
        assertEquals("2:15:30", MediaUtils.formatTime(8130000))
    }
    
    @Test
    fun formatFileSize_correctlyFormatsBytes() {
        assertEquals("512 B", MediaUtils.formatFileSize(512))
        assertEquals("1.0 KB", MediaUtils.formatFileSize(1024))
        assertEquals("1.5 KB", MediaUtils.formatFileSize(1536))
    }
    
    @Test
    fun formatFileSize_correctlyFormatsMB() {
        assertEquals("1.0 MB", MediaUtils.formatFileSize(1024 * 1024))
        assertEquals("5.5 MB", MediaUtils.formatFileSize((5.5 * 1024 * 1024).toLong()))
    }
    
    @Test
    fun formatFileSize_correctlyFormatsGB() {
        assertEquals("1.0 GB", MediaUtils.formatFileSize(1024L * 1024 * 1024))
        assertEquals("2.5 GB", MediaUtils.formatFileSize((2.5 * 1024 * 1024 * 1024).toLong()))
    }
    
    @Test
    fun isAudioFile_correctlyIdentifiesAudioMimeTypes() {
        assertTrue(MediaUtils.isAudioFile("audio/mpeg"))
        assertTrue(MediaUtils.isAudioFile("audio/mp4"))
        assertTrue(MediaUtils.isAudioFile("audio/wav"))
        assertFalse(MediaUtils.isAudioFile("video/mp4"))
        assertFalse(MediaUtils.isAudioFile("image/jpeg"))
    }
    
    @Test
    fun isVideoFile_correctlyIdentifiesVideoMimeTypes() {
        assertTrue(MediaUtils.isVideoFile("video/mp4"))
        assertTrue(MediaUtils.isVideoFile("video/avi"))
        assertTrue(MediaUtils.isVideoFile("video/mkv"))
        assertFalse(MediaUtils.isVideoFile("audio/mpeg"))
        assertFalse(MediaUtils.isVideoFile("image/jpeg"))
    }
    
    @Test
    fun getFileExtension_correctlyExtractsExtension() {
        assertEquals("mp3", MediaUtils.getFileExtension("/path/to/file.mp3"))
        assertEquals("mp4", MediaUtils.getFileExtension("/path/to/video.mp4"))
        assertEquals("", MediaUtils.getFileExtension("/path/to/file"))
        assertEquals("jpeg", MediaUtils.getFileExtension("image.jpeg"))
    }
    
    @Test
    fun getFileNameWithoutExtension_correctlyExtractsName() {
        assertEquals("song", MediaUtils.getFileNameWithoutExtension("/path/to/song.mp3"))
        assertEquals("video", MediaUtils.getFileNameWithoutExtension("/path/to/video.mp4"))
        assertEquals("file", MediaUtils.getFileNameWithoutExtension("/path/to/file"))
        assertEquals("image", MediaUtils.getFileNameWithoutExtension("image.jpeg"))
    }
}
