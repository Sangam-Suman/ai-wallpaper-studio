package com.example.aiwallpaper.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = OnBackground,
    secondary = NeonBlue,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = OnBackground,
    surface = DarkSurface,
    onSurface = OnSurface,
    surfaceVariant = CardBackground,
    onSurfaceVariant = OnSurface,
    error = ErrorRed,
    onError = OnBackground
)

@Composable
fun AIWallpaperTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
