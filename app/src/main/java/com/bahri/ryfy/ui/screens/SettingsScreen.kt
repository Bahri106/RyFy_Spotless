package com.bahri.ryfy.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.BuildConfig
import com.bahri.ryfy.R
import com.bahri.ryfy.core.AppIconManager
import com.bahri.ryfy.ui.dialogs.AppIconDialog
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

    fun openUrl(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Tidak dapat membuka tautan", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            item {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpotifyText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 1. Developer & Founder Card (About Developer)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpotifyElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Saiful Bahri",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyText
                                )
                                Text(
                                    text = "Pemilik & Pendiri Tunggal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Telegram Channel Link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openUrl("https://t.me/ryfyProject") }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Channel Update Aplikasi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpotifyText
                                )
                                Text(
                                    text = "https://t.me/ryfyProject",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Telegram Owner Link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openUrl("https://t.me/Gus_tazdo") }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kontak Telegram Pemilik",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpotifyText
                                )
                                Text(
                                    text = "@Gus_tazdo (Saiful Bahri)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // GitHub Founder Link
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openUrl("https://github.com/Bahri106") }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GitHub Founder (Saiful Bahri)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpotifyText
                                )
                                Text(
                                    text = "github.com/Bahri106",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // GitHub Co-Founder & Contributor (Diki Dev)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openUrl("https://github.com/Towartz") }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Co-Founder & Contributor (Diki Dev)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpotifyText
                                )
                                Text(
                                    text = "github.com/Towartz",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. Info Aplikasi Card (Tentang RyFy Spotless & Fitur Bebas Iklan)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpotifyElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tentang RyFy Spotless",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "RyFy Spotless adalah pemutar musik mandiri hasil adaptasi 1:1 antarmuka Spotify resmi (Dark Mode, tipografi Spotify Mix UI, bar tembus pandang, dan Smart Shuffle).\n\n" +
                                    "• 100% Bebas Iklan: Tanpa jeda audio sponsor dan tanpa iklan visual.\n" +
                                    "• Unlimited On-Demand: Bebas pilih lagu apa pun dan lompat lagu tanpa batas.\n" +
                                    "• Zero-Login: Berjalan privat dalam mode tamu tanpa login akun Google/pihak ketiga, aman dari blokir.\n" +
                                    "• Audio Kualitas Tinggi: Streaming jernih 256 kbps dengan lirik tersinkronisasi real-time via LRCLIB.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 3. Settings Category Section
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
                            title = "Kualitas Audio Streaming",
                            subtitle = "Sangat Tinggi (256 kbps Opus / Bebas Batas)",
                            onClick = {
                                Toast.makeText(context, "Kualitas audio diatur ke Maksimal (Lossless/High)", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsItemRow(
                            icon = Icons.Default.Security,
                            title = "Proteksi & Privasi",
                            subtitle = "Tanpa pelacak, tanpa akun pihak ketiga",
                            onClick = {
                                Toast.makeText(context, "Privasi offline aktif", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        SettingsItemRow(
                            icon = Icons.Default.CleaningServices,
                            title = "Bersihkan Cache Audio",
                            subtitle = "Kosongkan ruang penyimpanan sementara",
                            onClick = {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "Cache aplikasi berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                            }
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
                            title = "Pembaruan Aplikasi",
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
