package com.mediaplayer.data.models

enum class SortOption(val displayName: String) {
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    ARTIST_ASC("Artist A-Z"),
    ARTIST_DESC("Artist Z-A"),
    ALBUM_ASC("Album A-Z"),
    ALBUM_DESC("Album Z-A"),
    DURATION_ASC("Duration (Short to Long)"),
    DURATION_DESC("Duration (Long to Short)"),
    DATE_ADDED_ASC("Date Added (Oldest)"),
    DATE_ADDED_DESC("Date Added (Newest)"),
    PLAY_COUNT_ASC("Play Count (Low to High)"),
    PLAY_COUNT_DESC("Play Count (High to Low)")
}

fun List<MediaItem>.sortedBy(sortOption: SortOption): List<MediaItem> {
    return when (sortOption) {
        SortOption.TITLE_ASC -> sortedBy { it.displayTitle.lowercase() }
        SortOption.TITLE_DESC -> sortedByDescending { it.displayTitle.lowercase() }
        SortOption.ARTIST_ASC -> sortedBy { it.displayArtist.lowercase() }
        SortOption.ARTIST_DESC -> sortedByDescending { it.displayArtist.lowercase() }
        SortOption.ALBUM_ASC -> sortedBy { it.displayAlbum.lowercase() }
        SortOption.ALBUM_DESC -> sortedByDescending { it.displayAlbum.lowercase() }
        SortOption.DURATION_ASC -> sortedBy { it.duration }
        SortOption.DURATION_DESC -> sortedByDescending { it.duration }
        SortOption.DATE_ADDED_ASC -> sortedBy { it.dateAdded }
        SortOption.DATE_ADDED_DESC -> sortedByDescending { it.dateAdded }
        SortOption.PLAY_COUNT_ASC -> sortedBy { it.playCount }
        SortOption.PLAY_COUNT_DESC -> sortedByDescending { it.playCount }
    }
}
