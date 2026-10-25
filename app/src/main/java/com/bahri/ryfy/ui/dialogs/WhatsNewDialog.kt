package com.bahri.ryfy.ui.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.BuildConfig
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

object WhatsNewHelper {
    private const val PREFS = "ryfy_version_prefs"
    private const val KEY_LAST_CODE = "last_seen_version_code"

    fun shouldShow(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastCode = prefs.getInt(KEY_LAST_CODE, 0)
        return lastCode < BuildConfig.VERSION_CODE
    }

    fun markSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_LAST_CODE, BuildConfig.VERSION_CODE).apply()
    }
}

val RELEASE_HIGHLIGHTS = listOf(
    "Media Notification & Lockscreen: Kontrol lagu aktif di status bar, dynamic island, dan layar kunci HP.",
    "Fitur Lagu yang Disukai (Love Icon): Simpan lagu favorit secara permanen dan sinkron ke tab Library.",
    "Menu 3-Titik Player: Bagikan lagu, lihat karya artis, dan pantau antrean lagu berikutnya.",
    "Auto-Update OTA Popup (1:1 Rolleys): Notifikasi pembaruan otomatis muncul saat versi baru tersedia.",
    "Fix Kompatibilitas Android 11: Instalasi lancar tanpa error paket.",
    "Logo Baru & FAB Custom Icon Launcher: Pilihan tema ikon Neon, Spotify Classic, dan Mono."
)

@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyElevated,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Yang Baru di RyFy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyText
                    )
                    Text(
                        text = "Versi ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Berikut peningkatan dan fitur terbaru yang siap Anda nikmati:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                RELEASE_HIGHLIGHTS.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpotifyText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Mulai Mendengarkan",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
