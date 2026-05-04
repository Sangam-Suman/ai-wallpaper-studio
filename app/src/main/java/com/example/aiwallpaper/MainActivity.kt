package com.example.aiwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aiwallpaper.storage.AppContainer
import com.example.aiwallpaper.ui.navigation.AppNavigation
import com.example.aiwallpaper.ui.theme.AIWallpaperTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize repositories/database once at app start
        AppContainer.initialize(applicationContext)

        enableEdgeToEdge()

        setContent {
            AIWallpaperTheme {
                AppNavigation()
            }
        }
    }
}
