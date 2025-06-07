package com.mediaplayer.di

import android.content.Context
import androidx.room.Room
import com.mediaplayer.data.database.MediaDao
import com.mediaplayer.data.database.MediaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideMediaDatabase(@ApplicationContext context: Context): MediaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MediaDatabase::class.java,
            "media_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideMediaDao(database: MediaDatabase): MediaDao {
        return database.mediaDao()
    }
}
