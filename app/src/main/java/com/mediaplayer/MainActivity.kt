package com.mediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediaplayer.ui.screens.MainScreen
import com.mediaplayer.ui.theme.MediaPlayerTheme
import com.mediaplayer.ui.viewmodels.MediaPlayerViewModel
import com.mediaplayer.utils.ScreenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var screenManager: ScreenManager

    private var pendingFileUri by mutableStateOf<Uri?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted, proceed with app functionality
            Log.d(TAG, "Media permissions granted")
        } else {
            // Handle permission denial
            Log.w(TAG, "Media permissions not granted")
            pendingFileUri = null // Clear pending URI if permissions denied
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up screen manager
        screenManager.setCurrentActivity(this)
        lifecycle.addObserver(screenManager)

        // Request necessary permissions
        requestPermissions()

        // Handle incoming intent (file opened from external app)
        handleIntent(intent)

        setContent {
            MediaPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val fileUriToPlay = pendingFileUri?.takeIf { hasRequiredPermissions() }

                    MainScreen(
                        externalFileUri = fileUriToPlay,
                        onExternalFileHandled = {
                            // Clear the pending URI after it's been handled
                            pendingFileUri = null
                        }
                    )
                }
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            // Pre-Android 13 permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        Log.d(TAG, "Handling intent: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    Log.d(TAG, "Received file URI: $uri")

                    // Store URI to be handled by MainScreen
                    pendingFileUri = uri

                    if (!hasRequiredPermissions()) {
                        Log.d(TAG, "Requesting permissions for external file")
                        requestPermissions()
                    }
                }
            }
        }
    }



    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        screenManager.setCurrentActivity(null)
        super.onDestroy()
    }
}
