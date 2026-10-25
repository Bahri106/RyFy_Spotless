package com.bahri.ryfy.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.ui.theme.SpotifyGreen
import java.io.File

@Composable
fun UpdateDialog(
    state: UpdateState,
    onDismiss: () -> Unit,
    onDownload: (UpdateInfo) -> Unit,
    onInstall: (File) -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = SpotifyGreen
                )
                Text(
                    text = when (state) {
                        is UpdateState.Checking -> "Memeriksa Pembaruan..."
                        is UpdateState.Available -> "Pembaruan Tersedia!"
                        is UpdateState.Downloading -> "Mengunduh Pembaruan..."
                        is UpdateState.ReadyToInstall -> "Siap Pasang"
                        is UpdateState.UpToDate -> "Aplikasi Sudah Terbaru"
                        is UpdateState.Error -> "Gagal Memeriksa"
                        else -> "Pembaruan Aplikasi"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (state) {
                    is UpdateState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = SpotifyGreen
                            )
                            Text(
                                text = "Menghubungi server rilis RyFy...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is UpdateState.Available -> {
                        Text(
                            text = "Versi baru: v${state.info.versionName} (Build ${state.info.versionCode})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (state.info.changelog.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Catatan Rilis:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    for (item in state.info.changelog) {
                                        Text(
                                            text = "• $item",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is UpdateState.Downloading -> {
                        Text(
                            text = "Mengunduh paket aplikasi... ${state.progressPercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SpotifyGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    is UpdateState.ReadyToInstall -> {
                        Text(
                            text = "Paket versi ${state.info.versionName} berhasil diunduh. Tekan 'Pasang Sekarang' untuk memperbarui.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    is UpdateState.UpToDate -> {
                        Text(
                            text = "Kamu sudah menggunakan versi RyFy Spotless terbaru.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is UpdateState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            when (state) {
                is UpdateState.Available -> {
                    Button(
                        onClick = { onDownload(state.info) },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                    ) {
                        Text("Unduh & Pasang", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Button(
                        onClick = { onInstall(state.apkFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                    ) {
                        Text("Pasang Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateState.Error -> {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                    ) {
                        Text("Coba Lagi")
                    }
                }
                else -> {
                    TextButton(onClick = onDismiss) {
                        Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        dismissButton = {
            if (state is UpdateState.Available || state is UpdateState.ReadyToInstall) {
                TextButton(onClick = onDismiss) {
                    Text("Nanti Saja", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
