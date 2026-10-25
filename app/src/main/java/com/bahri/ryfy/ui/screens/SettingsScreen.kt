package com.bahri.ryfy.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bahri.ryfy.BuildConfig
import com.bahri.ryfy.R
import com.bahri.ryfy.core.AppIconManager
import com.bahri.ryfy.core.AppLanguageManager
import com.bahri.ryfy.core.RyFyStrings
import com.bahri.ryfy.ui.dialogs.AboutRyFyDialog
import com.bahri.ryfy.ui.dialogs.AppIconDialog
import com.bahri.ryfy.ui.dialogs.DeveloperBottomSheet
import com.bahri.ryfy.ui.dialogs.EqualizerDialog
import com.bahri.ryfy.ui.dialogs.LanguageSelectionDialog
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import com.bahri.ryfy.update.UpdateDialog
import com.bahri.ryfy.update.UpdateManager
import com.bahri.ryfy.update.UpdateState

@Composable
fun SettingsScreen(updateManager: UpdateManager) {
    val context = LocalContext.current
    val updateState by updateManager.state.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAppIconDialog by remember { mutableStateOf(false) }
    var showDeveloperSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }

    val currentLangCode by AppLanguageManager.currentLanguage.collectAsState()
    val currentLangLabel = AppLanguageManager.supportedLanguages.firstOrNull { it.code.equals(currentLangCode, true) }?.nativeName ?: "Default Sistem"

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            item {
                Text(
                    text = RyFyStrings.get(RyFyStrings.Key.SETTINGS_TITLE),
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpotifyText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 1. Sleek Interactive Developer & About Section (Opsi 1)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpotifyElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        SettingsItemRow(
                            icon = Icons.Default.People,
                            title = RyFyStrings.get(RyFyStrings.Key.DEV_TEAM),
                            subtitle = RyFyStrings.get(RyFyStrings.Key.DEV_TEAM_SUB),
                            highlight = true,
                            onClick = { showDeveloperSheet = true }
                        )

                        HorizontalDivider(
                            color = SpotifyHighlight,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        SettingsItemRow(
                            icon = Icons.Default.Info,
                            title = RyFyStrings.get(RyFyStrings.Key.ABOUT_APP),
                            subtitle = RyFyStrings.get(RyFyStrings.Key.ABOUT_APP_SUB),
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // 2. Settings Category Section
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpotifyElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        SettingsItemRow(
                            icon = Icons.Default.GraphicEq,
                            title = RyFyStrings.get(RyFyStrings.Key.EQUALIZER_TITLE),
                            subtitle = RyFyStrings.get(RyFyStrings.Key.EQUALIZER_SUB),
                            onClick = { showEqualizerDialog = true }
                        )

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsItemRow(
                            icon = Icons.Default.Security,
                            title = RyFyStrings.get(RyFyStrings.Key.PRIVACY_TITLE),
                            subtitle = RyFyStrings.get(RyFyStrings.Key.PRIVACY_SUB),
                            onClick = {
                                Toast.makeText(context, "Proteksi privasi offline aktif", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsItemRow(
                            icon = Icons.Default.CleaningServices,
                            title = RyFyStrings.get(RyFyStrings.Key.CLEAR_CACHE),
                            subtitle = RyFyStrings.get(RyFyStrings.Key.CLEAR_CACHE_SUB),
                            onClick = {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "Cache aplikasi berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsItemRow(
                            icon = Icons.Default.Language,
                            title = RyFyStrings.get(RyFyStrings.Key.APP_LANGUAGE),
                            subtitle = currentLangLabel,
                            onClick = { showLanguageDialog = true }
                        )

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        // OTA In-App Update Feature
                        val updateSubtitle = when (updateState) {
                            is UpdateState.Checking -> "Sedang memeriksa pembaruan..."
                            is UpdateState.Available -> "Versi baru tersedia! Ketuk untuk pasang"
                            is UpdateState.Downloading -> "Sedang mengunduh pembaruan..."
                            is UpdateState.ReadyToInstall -> "Pembaruan siap dipasang"
                            is UpdateState.UpToDate -> "Versi terbaru v${BuildConfig.VERSION_NAME}"
                            is UpdateState.Error -> "Gagal memeriksa (ketuk untuk coba lagi)"
                            else -> "Versi saat ini v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
                        }

                        SettingsItemRow(
                            icon = Icons.Default.SystemUpdate,
                            title = RyFyStrings.get(RyFyStrings.Key.APP_UPDATE),
                            subtitle = updateSubtitle,
                            isBusy = updateState is UpdateState.Checking,
                            highlight = updateState is UpdateState.Available || updateState is UpdateState.ReadyToInstall,
                            onClick = {
                                showUpdateDialog = true
                                if (updateState is UpdateState.Idle || updateState is UpdateState.UpToDate || updateState is UpdateState.Error) {
                                    updateManager.checkForUpdates(manual = true)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button for Custom App Icon (like WhatsApp FAB in Rolleys)
        FloatingActionButton(
            onClick = { showAppIconDialog = true },
            containerColor = SpotifyGreen,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp)
                .size(56.dp)
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = "Custom Icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        }
    }

    // Tim Pengembang & Komunitas BottomSheet
    if (showDeveloperSheet) {
        DeveloperBottomSheet(onDismiss = { showDeveloperSheet = false })
    }

    // Tentang RyFy Popup Dialog
    if (showAboutDialog) {
        AboutRyFyDialog(onDismiss = { showAboutDialog = false })
    }

    // App Icon Customizer Dialog
    if (showAppIconDialog) {
        val currentIcon by AppIconManager.icon.collectAsState()
        AppIconDialog(
            current = currentIcon,
            onDismiss = { showAppIconDialog = false },
            onSelect = { icon ->
                AppIconManager.setIcon(context, icon)
                showAppIconDialog = false
                Toast.makeText(context, "Ikon berhasil diubah ke ${icon.label}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLangCode,
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Equalizer Dialog
    if (showEqualizerDialog) {
        EqualizerDialog(onDismiss = { showEqualizerDialog = false })
    }

    // Update Dialog
    if (showUpdateDialog) {
        UpdateDialog(
            state = updateState,
            onDismiss = {
                showUpdateDialog = false
                updateManager.dismissUpdate()
            },
            onDownload = { info -> updateManager.downloadUpdate(info) },
            onInstall = { file -> updateManager.installUpdate(file) },
            onRetry = { updateManager.checkForUpdates(manual = true) }
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isBusy: Boolean = false,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) SpotifyGreen else SpotifyTextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (highlight) SpotifyGreen else SpotifyText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SpotifyTextSecondary
            )
        }

        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = SpotifyGreen
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SpotifyTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
