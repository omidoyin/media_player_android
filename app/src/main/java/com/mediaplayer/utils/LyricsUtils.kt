package com.mediaplayer.utils

import com.mediaplayer.data.models.LyricsLine
import java.util.regex.Pattern

object LyricsUtils {
    
    private val LRC_PATTERN = Pattern.compile("""\[(\d{2}):(\d{2})\.(\d{2})\](.*)""")
    private val LRC_PATTERN_ALT = Pattern.compile("""\[(\d{2}):(\d{2}):(\d{2})\](.*)""")
    private val LRC_PATTERN_SIMPLE = Pattern.compile("""\[(\d{2}):(\d{2})\](.*)""")
    
    /**
     * Check if lyrics content contains time synchronization
     */
    fun isTimeSynced(lyricsContent: String): Boolean {
        return lyricsContent.lines().any { line ->
            LRC_PATTERN.matcher(line).matches() ||
            LRC_PATTERN_ALT.matcher(line).matches() ||
            LRC_PATTERN_SIMPLE.matcher(line).matches()
        }
    }
    
    /**
     * Parse lyrics content into LyricsLine objects
     */
    fun parseLyrics(lyricsContent: String): List<LyricsLine> {
        val lines = mutableListOf<LyricsLine>()
        var lineNumber = 0
        
        lyricsContent.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty()) {
                val lyricsLine = parseLrcLine(trimmedLine) ?: LyricsLine(
                    timestamp = lineNumber * 3000L, // 3 seconds per line for non-synced
                    text = trimmedLine
                )
                lines.add(lyricsLine)
                lineNumber++
            }
        }
        
        return lines.sortedBy { it.timestamp }
    }
    
    /**
     * Parse a single LRC format line
     */
    private fun parseLrcLine(line: String): LyricsLine? {
        // Try different LRC formats
        
        // Format: [mm:ss.xx]text
        val matcher1 = LRC_PATTERN.matcher(line)
        if (matcher1.matches()) {
            val minutes = matcher1.group(1)?.toLongOrNull() ?: 0
            val seconds = matcher1.group(2)?.toLongOrNull() ?: 0
            val centiseconds = matcher1.group(3)?.toLongOrNull() ?: 0
            val text = matcher1.group(4)?.trim() ?: ""
            
            val timestamp = (minutes * 60 + seconds) * 1000 + centiseconds * 10
            return LyricsLine(timestamp, text)
        }
        
        // Format: [mm:ss:xx]text
        val matcher2 = LRC_PATTERN_ALT.matcher(line)
        if (matcher2.matches()) {
            val minutes = matcher2.group(1)?.toLongOrNull() ?: 0
            val seconds = matcher2.group(2)?.toLongOrNull() ?: 0
            val centiseconds = matcher2.group(3)?.toLongOrNull() ?: 0
            val text = matcher2.group(4)?.trim() ?: ""
            
            val timestamp = (minutes * 60 + seconds) * 1000 + centiseconds * 10
            return LyricsLine(timestamp, text)
        }
        
        // Format: [mm:ss]text
        val matcher3 = LRC_PATTERN_SIMPLE.matcher(line)
        if (matcher3.matches()) {
            val minutes = matcher3.group(1)?.toLongOrNull() ?: 0
            val seconds = matcher3.group(2)?.toLongOrNull() ?: 0
            val text = matcher3.group(3)?.trim() ?: ""
            
            val timestamp = (minutes * 60 + seconds) * 1000
            return LyricsLine(timestamp, text)
        }
        
        return null
    }
    
    /**
     * Find the current lyrics line based on playback position
     */
    fun getCurrentLineIndex(lyrics: List<LyricsLine>, currentPosition: Long): Int {
        if (lyrics.isEmpty()) return -1
        
        for (i in lyrics.indices.reversed()) {
            if (currentPosition >= lyrics[i].timestamp) {
                return i
            }
        }
        
        return -1
    }
    
    /**
     * Get the next lyrics line
     */
    fun getNextLine(lyrics: List<LyricsLine>, currentIndex: Int): LyricsLine? {
        return if (currentIndex >= 0 && currentIndex < lyrics.size - 1) {
            lyrics[currentIndex + 1]
        } else null
    }
    
    /**
     * Get the previous lyrics line
     */
    fun getPreviousLine(lyrics: List<LyricsLine>, currentIndex: Int): LyricsLine? {
        return if (currentIndex > 0) {
            lyrics[currentIndex - 1]
        } else null
    }
    
    /**
     * Format timestamp to readable time
     */
    fun formatTimestamp(timestamp: Long): String {
        val minutes = timestamp / 60000
        val seconds = (timestamp % 60000) / 1000
        val centiseconds = (timestamp % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)
    }
    
    /**
     * Convert LyricsLine list back to LRC format
     */
    fun toLrcFormat(lyrics: List<LyricsLine>): String {
        return lyrics.joinToString("\n") { line ->
            "[${formatTimestamp(line.timestamp)}]${line.text}"
        }
    }
    
    /**
     * Clean lyrics text by removing common prefixes/suffixes
     */
    fun cleanLyricsText(text: String): String {
        return text
            .replace(Regex("""\[.*?\]"""), "") // Remove any remaining brackets
            .replace(Regex("""^\d+\.\s*"""), "") // Remove numbering
            .replace(Regex("""\s+"""), " ") // Normalize whitespace
            .trim()
    }
    
    /**
     * Validate LRC format
     */
    fun isValidLrcFormat(content: String): Boolean {
        val lines = content.lines().filter { it.trim().isNotEmpty() }
        if (lines.isEmpty()) return false
        
        val validLines = lines.count { line ->
            LRC_PATTERN.matcher(line).matches() ||
            LRC_PATTERN_ALT.matcher(line).matches() ||
            LRC_PATTERN_SIMPLE.matcher(line).matches()
        }
        
        // At least 50% of lines should be in LRC format
        return validLines.toDouble() / lines.size >= 0.5
    }
    
    /**
     * Extract metadata from LRC file
     */
    fun extractLrcMetadata(content: String): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        val metadataPattern = Pattern.compile("""\[(\w+):(.*?)\]""")
        
        content.lines().forEach { line ->
            val matcher = metadataPattern.matcher(line.trim())
            if (matcher.matches()) {
                val key = matcher.group(1)?.lowercase()
                val value = matcher.group(2)?.trim()
                if (key != null && value != null && !key.matches(Regex("""\d+"""))) {
                    metadata[key] = value
                }
            }
        }
        
        return metadata
    }
    
    /**
     * Search for lyrics line containing specific text
     */
    fun searchInLyrics(lyrics: List<LyricsLine>, query: String): List<Int> {
        return lyrics.mapIndexedNotNull { index, line ->
            if (line.text.contains(query, ignoreCase = true)) index else null
        }
    }
}
