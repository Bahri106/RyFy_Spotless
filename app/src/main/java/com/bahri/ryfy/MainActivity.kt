package com.bahri.ryfy

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bahri.ryfy.core.AppLanguageManager
import com.bahri.ryfy.core.RyFyStrings
import com.bahri.ryfy.data.FavoritesManager
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.playback.RyFyNotificationManager
import com.bahri.ryfy.ui.components.LiquidGlassBottomBar
import com.bahri.ryfy.ui.components.MiniPlayer
import com.bahri.ryfy.ui.components.NowPlayingDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import com.bahri.ryfy.ui.dialogs.WhatsNewDialog
import com.bahri.ryfy.ui.dialogs.WhatsNewHelper
import com.bahri.ryfy.ui.screens.HomeScreen
import com.bahri.ryfy.ui.screens.LibraryScreen
import com.bahri.ryfy.ui.screens.SearchScreen
import com.bahri.ryfy.ui.screens.SettingsScreen
import com.bahri.ryfy.ui.theme.RyFyTheme
import com.bahri.ryfy.ui.theme.SpotifyBlack
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import com.bahri.ryfy.update.UpdateDialog
import com.bahri.ryfy.update.UpdateManager
import com.bahri.ryfy.update.UpdateState

enum class RyFyTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.AutoMirrored.Filled.QueueMusic),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var updateManager: UpdateManager

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.bahri.ryfy.core.AppLanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateManager = UpdateManager(this)
        RyFyNotificationManager.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            RyFyTheme {
                MainApp(updateManager = updateManager)
            }
        }
    }
}

@Composable
fun MainApp(updateManager: UpdateManager) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(RyFyTab.HOME) }
    val currentTrack by PlayerManager.currentTrack.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val progressMs by PlayerManager.currentPositionMs.collectAsState()
    val shuffleMode by PlayerManager.shuffleMode.collectAsState()
    val repeatMode by PlayerManager.repeatMode.collectAsState()

    val updateState by updateManager.state.collectAsState()
    var showAutoUpdateDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(WhatsNewHelper.shouldShow(context)) }

    var showNowPlaying by remember { mutableStateOf(false) }

    // Auto-check for updates on app startup (1:1 Rolleys architecture)
    LaunchedEffect(Unit) {
        updateManager.checkForUpdates(manual = false)
    }

    // Automatically trigger update dialog when new version is available
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Available) {
            showAutoUpdateDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        // Full screen content layer (Content scrolls freely behind translucent bottom bar)
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                label = "tab_content_transition"
            ) { tab ->
                when (tab) {
                    RyFyTab.HOME -> HomeScreen()
                    RyFyTab.SEARCH -> SearchScreen()
                    RyFyTab.LIBRARY -> LibraryScreen()
                    RyFyTab.SETTINGS -> SettingsScreen(updateManager = updateManager)
                }
            }
        }

        // Translucent Floating Bottom Navigation & Mini Player
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 6.dp)
        ) {
            // Persistent Floating Mini Player
            currentTrack?.let { track ->
                val isLiked = FavoritesManager.isLiked(track.id)
                val activeTrack = track.copy(isLiked = isLiked)

                MiniPlayer(
                    track = activeTrack,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    onClick = { showNowPlaying = true },
                    onTogglePlay = { PlayerManager.togglePlayPause() },
                    onToggleLike = { t ->
                        FavoritesManager.toggleLike(context, t)
                    }
                )
            }

            // Liquid Glass Bottom Navigation Bar
            LiquidGlassBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }

    // Expandable Now Playing Fullscreen Dialog
    if (showNowPlaying && currentTrack != null) {
        val track = currentTrack!!
        val isLiked = FavoritesManager.isLiked(track.id)
        val activeTrack = track.copy(isLiked = isLiked)

        NowPlayingDialog(
            track = activeTrack,
            isPlaying = isPlaying,
            progressMs = progressMs,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            onDismiss = { showNowPlaying = false },
            onTogglePlay = { PlayerManager.togglePlayPause() },
            onSeek = { PlayerManager.seekTo(it) },
            onNext = { PlayerManager.playNext() },
            onPrevious = { PlayerManager.playPrevious() },
            onToggleShuffle = { PlayerManager.cycleShuffleMode() },
            onToggleRepeat = { PlayerManager.toggleRepeat() },
            onToggleLike = { t ->
                FavoritesManager.toggleLike(context, t)
            }
        )
    }

    // Auto-update OTA Dialog (1:1 Rolleys)
    if (showAutoUpdateDialog) {
        UpdateDialog(
            state = updateState,
            onDismiss = {
                showAutoUpdateDialog = false
                updateManager.dismissUpdate()
            },
            onDownload = { info -> updateManager.downloadUpdate(info) },
            onInstall = { file -> updateManager.installUpdate(file) },
            onRetry = { updateManager.checkForUpdates(manual = true) }
        )
    }

    // What's New Dialog (Shown once after update)
    if (showWhatsNewDialog && !showAutoUpdateDialog) {
        WhatsNewDialog(
            onDismiss = {
                showWhatsNewDialog = false
                WhatsNewHelper.markSeen(context)
            }
        )
    }
}
