package com.mediaplayer.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mediaplayer.MainActivity
import com.mediaplayer.R
import com.mediaplayer.data.models.MediaItem

class MediaNotificationManager(
    private val context: Context,
    private val mediaSessionService: MediaSessionService
) {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "media_playback_channel"
        private const val CHANNEL_NAME = "Media Playback"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        mediaSession: MediaSession,
        mediaItem: MediaItem?,
        isPlaying: Boolean,
        albumArt: Bitmap? = null
    ): NotificationCompat.Builder {

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(mediaItem?.displayTitle ?: "Unknown Title")
            .setContentText(mediaItem?.displayArtist ?: "Unknown Artist")
            .setSubText(mediaItem?.displayAlbum)
            .setLargeIcon(albumArt ?: getDefaultAlbumArt())
            .setSmallIcon(R.drawable.ic_music_note) // You'll need to create this icon
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
            )

        // Add media actions
        addMediaActions(builder, isPlaying)

        return builder
    }

    private fun addMediaActions(
        builder: NotificationCompat.Builder,
        isPlaying: Boolean
    ) {
        // Previous action
        val previousIntent = createMediaActionIntent("PREVIOUS")
        builder.addAction(
            R.drawable.ic_skip_previous, // You'll need to create this icon
            "Previous",
            previousIntent
        )

        // Play/Pause action
        val playPauseIntent = createMediaActionIntent(if (isPlaying) "PAUSE" else "PLAY")
        builder.addAction(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play, // You'll need to create these icons
            if (isPlaying) "Pause" else "Play",
            playPauseIntent
        )

        // Next action
        val nextIntent = createMediaActionIntent("NEXT")
        builder.addAction(
            R.drawable.ic_skip_next, // You'll need to create this icon
            "Next",
            nextIntent
        )

        // Stop action
        val stopIntent = createMediaActionIntent("STOP")
        builder.addAction(
            R.drawable.ic_stop, // You'll need to create this icon
            "Stop",
            stopIntent
        )
    }

    private fun createMediaActionIntent(action: String): PendingIntent {
        val intent = Intent(context, MediaPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getDefaultAlbumArt(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_music_note)
    }

    fun showNotification(
        mediaSession: MediaSession,
        mediaItem: MediaItem?,
        isPlaying: Boolean,
        albumArt: Bitmap? = null
    ) {
        val notification = buildNotification(mediaSession, mediaItem, isPlaying, albumArt).build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }

    fun hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun updateNotification(
        mediaSession: MediaSession,
        mediaItem: MediaItem?,
        isPlaying: Boolean,
        albumArt: Bitmap? = null
    ) {
        showNotification(mediaSession, mediaItem, isPlaying, albumArt)
    }
}
