package com.bahri.ryfy.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bahri.ryfy.playback.SleepTimerManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import com.bahri.ryfy.ui.theme.GlassBorderBrush
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@Composable
fun SleepTimerDialog(onDismiss: () -> Unit) {
    val remainingMs by SleepTimerManager.remainingTimeMs.collectAsState()
    val isEndOfTrack by SleepTimerManager.stopAtEndOfTrack.collectAsState()

    val options = listOf(
        Pair("5 Menit", 5),
        Pair("15 Menit", 15),
        Pair("30 Menit", 30),
        Pair("45 Menit", 45),
        Pair("60 Menit (1 Jam)", 60)
    )

    val remainingLabel = if (remainingMs != null) {
        val totalSec = remainingMs!! / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        String.format("%02d:%02d tersisa", m, s)
    } else if (isEndOfTrack) {
        "Berhenti di akhir lagu ini"
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF21C1C1C),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.dp, GlassBorderBrush, RoundedCornerShape(24.dp)),
        title = {
            Column {
                Text(
                    text = "Pengatur Waktu Tidur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText
                )
                if (remainingLabel != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aktif: $remainingLabel",
                        style = MaterialTheme.typography.labelMedium,
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // End of track option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SleepTimerManager.setStopAtEndOfTrack()
                            onDismiss()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = isEndOfTrack,
                        onClick = {
                            SleepTimerManager.setStopAtEndOfTrack()
                            onDismiss()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = SpotifyGreen,
                            unselectedColor = SpotifyTextSecondary
                        )
                    )
                    Text(
                        text = "Akhir Lagu Ini",
                        color = if (isEndOfTrack) SpotifyGreen else SpotifyText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isEndOfTrack) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Minute options
                options.forEach { (label, minutes) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SleepTimerManager.startTimer(minutes)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = false,
                            onClick = {
                                SleepTimerManager.startTimer(minutes)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SpotifyGreen,
                                unselectedColor = SpotifyTextSecondary
                            )
                        )
                        Text(
                            text = label,
                            color = SpotifyText,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Turn off option (if active)
                if (remainingMs != null || isEndOfTrack) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            SleepTimerManager.cancelTimer()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Matikan Pengatur Waktu",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = SpotifyGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}
