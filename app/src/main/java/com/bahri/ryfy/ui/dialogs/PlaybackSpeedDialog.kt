package com.bahri.ryfy.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

@Composable
fun PlaybackSpeedDialog(onDismiss: () -> Unit) {
    val currentSpeed by PlayerManager.playbackSpeed.collectAsState()

    val speeds = listOf(
        Pair("0.8x (Slowed & Reverb Chill)", 0.8f),
        Pair("1.0x (Normal)", 1.0f),
        Pair("1.25x (Speed Up)", 1.25f),
        Pair("1.5x (Nightcore Fast)", 1.5f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyElevated,
        title = {
            Text(
                text = "Kecepatan Pemutaran",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SpotifyText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                speeds.forEach { (label, speed) ->
                    val isSelected = Math.abs(currentSpeed - speed) < 0.01f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                PlayerManager.setPlaybackSpeed(speed)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                PlayerManager.setPlaybackSpeed(speed)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SpotifyGreen,
                                unselectedColor = SpotifyTextSecondary
                            )
                        )
                        Text(
                            text = label,
                            color = if (isSelected) SpotifyGreen else SpotifyText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp)
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
