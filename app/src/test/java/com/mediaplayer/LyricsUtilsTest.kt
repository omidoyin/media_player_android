package com.mediaplayer

import com.mediaplayer.data.models.LyricsLine
import com.mediaplayer.utils.LyricsUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for LyricsUtils
 */
class LyricsUtilsTest {
    
    @Test
    fun isTimeSynced_detectsLrcFormat() {
        val lrcContent = """
            [00:12.34]First line
            [00:15.67]Second line
            [00:18.90]Third line
        """.trimIndent()
        
        assertTrue(LyricsUtils.isTimeSynced(lrcContent))
    }
    
    @Test
    fun isTimeSynced_detectsPlainText() {
        val plainContent = """
            First line
            Second line
            Third line
        """.trimIndent()
        
        assertFalse(LyricsUtils.isTimeSynced(plainContent))
    }
    
    @Test
    fun parseLyrics_parsesLrcFormat() {
        val lrcContent = """
            [00:12.34]First line
            [00:15.67]Second line
            [00:18.90]Third line
        """.trimIndent()
        
        val lines = LyricsUtils.parseLyrics(lrcContent)
        
        assertEquals(3, lines.size)
        assertEquals(12340L, lines[0].timestamp)
        assertEquals("First line", lines[0].text)
        assertEquals(15670L, lines[1].timestamp)
        assertEquals("Second line", lines[1].text)
    }
    
    @Test
    fun parseLyrics_parsesPlainText() {
        val plainContent = """
            First line
            Second line
            Third line
        """.trimIndent()
        
        val lines = LyricsUtils.parseLyrics(plainContent)
        
        assertEquals(3, lines.size)
        assertEquals("First line", lines[0].text)
        assertEquals("Second line", lines[1].text)
        assertEquals("Third line", lines[2].text)
        // Plain text should have auto-generated timestamps
        assertTrue(lines[0].timestamp >= 0)
        assertTrue(lines[1].timestamp > lines[0].timestamp)
    }
    
    @Test
    fun getCurrentLineIndex_findsCorrectLine() {
        val lines = listOf(
            LyricsLine(10000L, "Line 1"),
            LyricsLine(15000L, "Line 2"),
            LyricsLine(20000L, "Line 3"),
            LyricsLine(25000L, "Line 4")
        )
        
        assertEquals(-1, LyricsUtils.getCurrentLineIndex(lines, 5000L))
        assertEquals(0, LyricsUtils.getCurrentLineIndex(lines, 12000L))
        assertEquals(1, LyricsUtils.getCurrentLineIndex(lines, 17000L))
        assertEquals(2, LyricsUtils.getCurrentLineIndex(lines, 22000L))
        assertEquals(3, LyricsUtils.getCurrentLineIndex(lines, 30000L))
    }
    
    @Test
    fun formatTimestamp_formatsCorrectly() {
        assertEquals("00:12.34", LyricsUtils.formatTimestamp(12340L))
        assertEquals("01:23.45", LyricsUtils.formatTimestamp(83450L))
        assertEquals("10:00.00", LyricsUtils.formatTimestamp(600000L))
    }
    
    @Test
    fun toLrcFormat_convertsCorrectly() {
        val lines = listOf(
            LyricsLine(12340L, "First line"),
            LyricsLine(15670L, "Second line"),
            LyricsLine(18900L, "Third line")
        )
        
        val lrcFormat = LyricsUtils.toLrcFormat(lines)
        val expectedFormat = """
            [00:12.34]First line
            [00:15.67]Second line
            [00:18.90]Third line
        """.trimIndent()
        
        assertEquals(expectedFormat, lrcFormat)
    }
    
    @Test
    fun cleanLyricsText_removesFormatting() {
        val dirtyText = "[00:12.34]1. This is a line with [brackets] and numbering"
        val cleanText = LyricsUtils.cleanLyricsText(dirtyText)
        
        assertEquals("This is a line with and numbering", cleanText)
    }
    
    @Test
    fun isValidLrcFormat_validatesCorrectly() {
        val validLrc = """
            [00:12.34]First line
            [00:15.67]Second line
            [00:18.90]Third line
        """.trimIndent()
        
        val invalidLrc = """
            First line
            Second line
            Third line
        """.trimIndent()
        
        assertTrue(LyricsUtils.isValidLrcFormat(validLrc))
        assertFalse(LyricsUtils.isValidLrcFormat(invalidLrc))
    }
    
    @Test
    fun searchInLyrics_findsMatches() {
        val lines = listOf(
            LyricsLine(10000L, "Hello world"),
            LyricsLine(15000L, "This is a test"),
            LyricsLine(20000L, "Hello again"),
            LyricsLine(25000L, "Final line")
        )
        
        val results = LyricsUtils.searchInLyrics(lines, "hello")
        assertEquals(listOf(0, 2), results)
        
        val testResults = LyricsUtils.searchInLyrics(lines, "test")
        assertEquals(listOf(1), testResults)
        
        val noResults = LyricsUtils.searchInLyrics(lines, "xyz")
        assertEquals(emptyList<Int>(), noResults)
    }
    
    @Test
    fun extractLrcMetadata_extractsCorrectly() {
        val lrcWithMetadata = """
            [ti:Song Title]
            [ar:Artist Name]
            [al:Album Name]
            [00:12.34]First line
            [00:15.67]Second line
        """.trimIndent()
        
        val metadata = LyricsUtils.extractLrcMetadata(lrcWithMetadata)
        
        assertEquals("Song Title", metadata["ti"])
        assertEquals("Artist Name", metadata["ar"])
        assertEquals("Album Name", metadata["al"])
    }
}
