package com.mediaplayer.utils

import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages screen wake lock to keep the screen on during video playback
 */
@Singleton
class ScreenManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "ScreenManager"
        private const val WAKE_LOCK_TAG = "MediaPlayer:VideoPlayback"
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var currentActivity: Activity? = null
    private var isVideoPlaying = false

    /**
     * Set the current activity to manage its window flags
     */
    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity
        // Apply current state to new activity
        if (isVideoPlaying && activity != null) {
            keepScreenOnInternal(activity)
        }
    }

    /**
     * Keep screen on during video playback
     */
    fun keepScreenOn(isPlaying: Boolean, isVideo: Boolean) {
        val shouldKeepScreenOn = isPlaying && isVideo
        
        if (shouldKeepScreenOn == isVideoPlaying) {
            return // No change needed
        }
        
        isVideoPlaying = shouldKeepScreenOn
        
        if (shouldKeepScreenOn) {
            acquireWakeLock()
            currentActivity?.let { keepScreenOnInternal(it) }
            Log.d(TAG, "Screen wake lock acquired for video playback")
        } else {
            releaseWakeLock()
            currentActivity?.let { allowScreenOffInternal(it) }
            Log.d(TAG, "Screen wake lock released")
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) {
            return // Already acquired
        }

        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes timeout as safety
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock", e)
        }
    }

    private fun keepScreenOnInternal(activity: Activity) {
        try {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add keep screen on flag", e)
        }
    }

    private fun allowScreenOffInternal(activity: Activity) {
        try {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear keep screen on flag", e)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        // Release wake lock when app goes to background
        if (wakeLock?.isHeld == true) {
            releaseWakeLock()
            Log.d(TAG, "Wake lock released due to app pause")
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        // Re-acquire wake lock if video was playing
        if (isVideoPlaying) {
            acquireWakeLock()
            currentActivity?.let { keepScreenOnInternal(it) }
            Log.d(TAG, "Wake lock re-acquired due to app resume")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        releaseWakeLock()
        currentActivity = null
        isVideoPlaying = false
    }
}
