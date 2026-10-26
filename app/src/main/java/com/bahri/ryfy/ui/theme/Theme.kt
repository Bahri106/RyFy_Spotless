package com.bahri.ryfy.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1:1 Authentic Spotify Color Palette
val SpotifyGreen = Color(0xFF1DB954)
val SpotifyGreenBright = Color(0xFF1ED760)
val SpotifyBlack = Color(0xFF121212)
val SpotifyDark = Color(0xFF000000)
val SpotifySurface = Color(0xEB181818)
val SpotifyElevated = Color(0xE6202020)
val SpotifyHighlight = Color(0x33FFFFFF)
val SpotifyText = Color(0xFFFFFFFF)
val SpotifyTextSecondary = Color(0xFFB3B3B3)
val SpotifyError = Color(0xFFE91429)

private val SpotifyDarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = SpotifyDark,
    primaryContainer = Color(0xD9242424),
    onPrimaryContainer = SpotifyGreenBright,
    surface = Color(0xEB181818),
    onSurface = SpotifyText,
    surfaceVariant = Color(0xCC222222),
    onSurfaceVariant = SpotifyTextSecondary,
    background = SpotifyBlack,
    onBackground = SpotifyText,
    outline = Color(0x33FFFFFF),
    outlineVariant = Color(0x1AFFFFFF),
    error = SpotifyError
)

@Composable
fun RyFyTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = SpotifyDarkColorScheme,
        typography = Typography,
        content = content
    )
}
