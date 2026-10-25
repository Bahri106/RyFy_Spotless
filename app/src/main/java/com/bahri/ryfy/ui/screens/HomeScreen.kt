package com.bahri.ryfy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bahri.ryfy.model.Track
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import java.util.Calendar

val SAMPLE_TRACKS = listOf(
    Track(
        id = "1",
        title = "Komang",
        artist = "Raim Laode",
        album = "Komang - Single",
        durationMs = 222000,
        coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
        isLiked = true,
        videoId = "M2QKS9tte9g"
    ),
    Track(
        id = "2",
        title = "Sial",
        artist = "Mahalini",
        album = "Fábula",
        durationMs = 243000,
        coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
        isLiked = true,
        videoId = "2MyjWg7P00A"
    ),
    Track(
        id = "3",
        title = "Rayuan Perempuan Gila",
        artist = "Nadin Amizah",
        album = "Untuk Dunia, Cinta, dan Kotornya",
        durationMs = 312000,
        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
        isLiked = false,
        videoId = "2A9Atl2hUkg"
    ),
    Track(
        id = "4",
        title = "Nanti Kita Seperti Ini",
        artist = "Batas Senja",
        album = "Nanti Kita Seperti Ini",
        durationMs = 239000,
        coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
        isLiked = false,
        videoId = "V697wAtn6cI"
    ),
    Track(
        id = "5",
        title = "Tak Segampang Itu",
        artist = "Anggi Marito",
        album = "Tak Segampang Itu - Single",
        durationMs = 231000,
        coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=500&auto=format&fit=crop&q=80",
        isLiked = true,
        videoId = "gR_qbfGkwpc"
    )
)

@Composable
fun HomeScreen() {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Selamat pagi"
            in 12..15 -> "Selamat siang"
            in 16..18 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("Semua", "Musik", "Podcast & Acara")

    var madeForYou by remember { mutableStateOf(SAMPLE_TRACKS) }
    var topCharts by remember { mutableStateOf(SAMPLE_TRACKS.reversed()) }
    var recommendations by remember { mutableStateOf(SAMPLE_TRACKS) }
    var popularTracks by remember { mutableStateOf(SAMPLE_TRACKS) }

    LaunchedEffect(Unit) {
        try {
            val hits = com.bahri.ryfy.data.api.MusicApiService.searchSongs("Top Hits Indonesia")
            if (hits.isNotEmpty()) {
                topCharts = hits
            }
            val trending = com.bahri.ryfy.data.api.MusicApiService.searchSongs("Lagu Pop Indonesia Terbaru")
            if (trending.isNotEmpty()) {
                madeForYou = trending
            }
            val recs = com.bahri.ryfy.data.api.MusicApiService.searchSongs("Indie Indonesia Populer")
            if (recs.isNotEmpty()) {
                recommendations = recs
            }
            val popular = com.bahri.ryfy.data.api.MusicApiService.searchSongs("Viral Hits TikTok Indonesia")
            if (popular.isNotEmpty()) {
                popularTracks = popular
            }
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Greeting Header (Time-based only, no user account name)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText
                )

                // Spotify Filter Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filters.forEachIndexed { index, filterName ->
                        val isSelected = selectedFilterIndex == index
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) SpotifyGreen else SpotifyElevated)
                                .clickable { selectedFilterIndex = index }
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filterName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else SpotifyText
                            )
                        }
                    }
                }
            }
        }

        // Quick Grid Playlists (6 items, 2 columns)
        item {
            val quickItems = listOf(
                "Lagu yang Disukai" to Icons.Default.Favorite,
                "Top Hits Indonesia" to Icons.Default.MusicNote,
                "Daily Mix 1" to Icons.Default.MusicNote,
                "Rock Classics" to Icons.Default.MusicNote,
                "Deep Focus" to Icons.Default.MusicNote,
                "Release Radar" to Icons.Default.MusicNote
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 2) {
                            val item = quickItems[row * 2 + col]
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SpotifyElevated,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clickable {
                                        PlayerManager.playTrack(topCharts.firstOrNull() ?: SAMPLE_TRACKS.first(), topCharts)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(if (item.first == "Lagu yang Disukai") Color(0xFF431885) else SpotifyHighlight)
                                    ) {
                                        Icon(
                                            imageVector = item.second,
                                            contentDescription = null,
                                            tint = SpotifyText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Made For You
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Dibuat untuk Anda",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpotifyText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(madeForYou) { track ->
                        TrackCard(
                            track = track,
                            onClick = { PlayerManager.playTrack(track, madeForYou) }
                        )
                    }
                }
            }
        }

        // Section 2: Charts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tangga Lagu Teratas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpotifyText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(topCharts) { track ->
                        TrackCard(
                            track = track,
                            onClick = { PlayerManager.playTrack(track, topCharts) }
                        )
                    }
                }
            }
        }

        // Section 3: Recommendations Today (Fill empty space)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Rekomendasi Hari Ini",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpotifyText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recommendations) { track ->
                        TrackCard(
                            track = track,
                            onClick = { PlayerManager.playTrack(track, recommendations) }
                        )
                    }
                }
            }
        }

        // Section 4: Vertical List of Popular Songs (Scrollable long down)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lagu Populer Teratas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpotifyText
                )

                popularTracks.forEachIndexed { index, track ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { PlayerManager.playTrack(track, popularTracks) }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextSecondary,
                            modifier = Modifier.width(28.dp)
                        )

                        if (track.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SpotifyHighlight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = SpotifyTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotifyTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { PlayerManager.playTrack(track, popularTracks) }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: Track,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clickable(onClick = onClick)
    ) {
        if (track.coverUrl.isNotBlank()) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpotifyHighlight)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = SpotifyTextSecondary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = SpotifyText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = SpotifyTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
