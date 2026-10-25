package com.bahri.ryfy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.ui.components.MiniPlayer
import com.bahri.ryfy.ui.components.NowPlayingDialog
import com.bahri.ryfy.ui.screens.HomeScreen
import com.bahri.ryfy.ui.screens.LibraryScreen
import com.bahri.ryfy.ui.screens.SearchScreen
import com.bahri.ryfy.ui.screens.SettingsScreen
import com.bahri.ryfy.ui.theme.RyFyTheme
import com.bahri.ryfy.ui.theme.SpotifyBlack
import com.bahri.ryfy.ui.theme.SpotifyDark
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import com.bahri.ryfy.update.UpdateManager

enum class RyFyTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.AutoMirrored.Filled.QueueMusic),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateManager = UpdateManager(this)

        setContent {
            RyFyTheme {
                MainApp(updateManager = updateManager)
            }
        }
    }
}

@Composable
fun MainApp(updateManager: UpdateManager) {
    var selectedTab by remember { mutableStateOf(RyFyTab.HOME) }
    val currentTrack by PlayerManager.currentTrack.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val progressMs by PlayerManager.currentPositionMs.collectAsState()
    val isShuffle by PlayerManager.isShuffle.collectAsState()
    val repeatMode by PlayerManager.repeatMode.collectAsState()

    var showNowPlaying by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.background(SpotifyDark)) {
                // Persistent Floating Mini Player
                currentTrack?.let { track ->
                    MiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        progressMs = progressMs,
                        onClick = { showNowPlaying = true },
                        onTogglePlay = { PlayerManager.togglePlayPause() },
                        onToggleLike = { /* Like */ }
                    )
                }

                // Spotify Bottom Navigation Bar
                NavigationBar(
                    containerColor = SpotifyDark,
                    contentColor = SpotifyTextSecondary,
                    tonalElevation = 0.dp
                ) {
                    RyFyTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyText,
                                selectedTextColor = SpotifyText,
                                unselectedIconColor = SpotifyTextSecondary,
                                unselectedTextColor = SpotifyTextSecondary,
                                indicatorColor = SpotifyDark
                            )
                        )
                    }
                }
            }
        },
        containerColor = SpotifyBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                RyFyTab.HOME -> HomeScreen()
                RyFyTab.SEARCH -> SearchScreen()
                RyFyTab.LIBRARY -> LibraryScreen()
                RyFyTab.SETTINGS -> SettingsScreen(updateManager = updateManager)
            }
        }
    }

    // Expandable Now Playing Fullscreen Dialog
    if (showNowPlaying && currentTrack != null) {
        NowPlayingDialog(
            track = currentTrack!!,
            isPlaying = isPlaying,
            progressMs = progressMs,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            onDismiss = { showNowPlaying = false },
            onTogglePlay = { PlayerManager.togglePlayPause() },
            onSeek = { PlayerManager.seekTo(it) },
            onNext = { PlayerManager.playNext() },
            onPrevious = { PlayerManager.playPrevious() },
            onToggleShuffle = { PlayerManager.toggleShuffle() },
            onToggleRepeat = { PlayerManager.toggleRepeat() },
            onToggleLike = { /* Like */ }
        )
    }
}
