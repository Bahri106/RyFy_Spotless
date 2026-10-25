package com.bahri.ryfy.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

@Composable
fun LibraryScreen() {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Playlist", "Artis", "Diunduh")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Koleksi Kamu",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpotifyText
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters.size) { idx ->
                        val f = filters[idx]
                        val isSelected = selectedFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = f },
                            label = { Text(f) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpotifyGreen,
                                selectedLabelColor = Color.Black,
                                containerColor = SpotifyElevated,
                                labelColor = SpotifyText
                            ),
                            border = null,
                            shape = CircleShape
                        )
                    }
                }
            }
        }

        // Liked Songs row
        item {
            LibraryRow(
                title = "Lagu yang Disukai",
                subtitle = "Sematkan · ${SAMPLE_TRACKS.size} lagu",
                brush = Brush.linearGradient(listOf(Color(0xFF450AF5), Color(0xFFC4EFD9))),
                icon = Icons.Default.Favorite,
                onClick = {
                    PlayerManager.playTrack(SAMPLE_TRACKS.first(), SAMPLE_TRACKS)
                }
            )
        }

        // Offline Downloaded Tracks row
        item {
            LibraryRow(
                title = "Musik Tersimpan Offline",
                subtitle = "Tersedia tanpa koneksi internet",
                brush = Brush.linearGradient(listOf(Color(0xFF006450), Color(0xFF1DB954))),
                icon = Icons.Default.DownloadDone,
                onClick = {
                    PlayerManager.playTrack(SAMPLE_TRACKS.first(), SAMPLE_TRACKS)
                }
            )
        }

        // Custom Local Playlist row
        item {
            LibraryRow(
                title = "Koleksi Favorit",
                subtitle = "Daftar putar tersimpan di perangkat",
                brush = Brush.linearGradient(listOf(Color(0xFF8C1932), Color(0xFFE91429))),
                icon = Icons.Default.MusicNote,
                onClick = {
                    PlayerManager.playTrack(SAMPLE_TRACKS.first(), SAMPLE_TRACKS)
                }
            )
        }
    }
}

@Composable
private fun LibraryRow(
    title: String,
    subtitle: String,
    brush: Brush,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SpotifyText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SpotifyTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
