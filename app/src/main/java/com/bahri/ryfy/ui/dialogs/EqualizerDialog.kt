package com.bahri.ryfy.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.playback.EqualizerManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

@Composable
fun EqualizerDialog(onDismiss: () -> Unit) {
    val isEnabled by EqualizerManager.isEnabled.collectAsState()
    val bassStrength by EqualizerManager.bassStrength.collectAsState()
    val presets by EqualizerManager.presets.collectAsState()
    val currentPreset by EqualizerManager.currentPreset.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyElevated,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Equalizer & Efek",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyText,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { EqualizerManager.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = SpotifyGreen,
                        uncheckedThumbColor = SpotifyTextSecondary,
                        uncheckedTrackColor = Color(0xFF282828)
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Bass Boost Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Penguat Bass (Bass Boost)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText
                        )
                        Text(
                            text = "${(bassStrength / 10)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) SpotifyGreen else SpotifyTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = bassStrength.toFloat(),
                        onValueChange = { EqualizerManager.setBassStrength(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = SpotifyGreen,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = SpotifyHighlight,
                            disabledActiveTrackColor = SpotifyTextSecondary.copy(alpha = 0.3f),
                            disabledInactiveTrackColor = SpotifyHighlight.copy(alpha = 0.2f),
                            disabledThumbColor = SpotifyTextSecondary
                        )
                    )
                }

                // Presets Section
                if (presets.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Preset Musik",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(presets) { idx, name ->
                                val selected = currentPreset.toInt() == idx
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (selected && isEnabled) SpotifyGreen else Color(0xFF242424))
                                        .clickable(enabled = isEnabled) {
                                            EqualizerManager.setPreset(idx.toShort())
                                        }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected && isEnabled) Color.Black else SpotifyText
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Equalizer akan aktif saat lagu mulai diputar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextSecondary
                    )
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
