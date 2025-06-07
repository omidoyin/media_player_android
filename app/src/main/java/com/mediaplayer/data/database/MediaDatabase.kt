package com.mediaplayer.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.mediaplayer.data.models.MediaItem
import com.mediaplayer.data.models.Playlist
import com.mediaplayer.data.models.PlaylistMediaCrossRef
import com.mediaplayer.data.models.Lyrics

@Database(
    entities = [
        MediaItem::class,
        Playlist::class,
        PlaylistMediaCrossRef::class,
        Lyrics::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: MediaDatabase? = null

        fun getDatabase(context: Context): MediaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaDatabase::class.java,
                    "media_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


