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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bahri.ryfy.R
import com.bahri.ryfy.data.api.MusicApiService
import com.bahri.ryfy.model.Track
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import java.util.Calendar

data class ArtistMix(
    val title: String,
    val artist: String,
    val subtitle: String,
    val coverUrl: String,
    val headerColor: Color
)

val ARTIST_MIXES = listOf(
    ArtistMix(
        title = "Mix Raim Laode",
        artist = "Raim Laode",
        subtitle = "Bernadya, Nadin Amizah, dan lainnya",
        coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1080&auto=format&fit=crop&q=90",
        headerColor = Color(0xFFB02897)
    ),
    ArtistMix(
        title = "Mix Hindia",
        artist = "Hindia",
        subtitle = "Lomba Sihir, Feast, dan lainnya",
        coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1080&auto=format&fit=crop&q=90",
        headerColor = Color(0xFF1E3264)
    ),
    ArtistMix(
        title = "Mix Bernadya",
        artist = "Bernadya",
        subtitle = "Nadin Amizah, Tiara Andini, dan lainnya",
        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1080&auto=format&fit=crop&q=90",
        headerColor = Color(0xFFE8115B)
    ),
    ArtistMix(
        title = "Mix Nadin Amizah",
        artist = "Nadin Amizah",
        subtitle = "Sal Priadi, Kunto Aji, dan lainnya",
        coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1080&auto=format&fit=crop&q=90",
        headerColor = Color(0xFF503750)
    )
)

val INITIAL_RECOMMENDED_TRACKS = listOf(
    Track(
        id = "raim_1",
        title = "Dialog Rindu",
        artist = "Raim Laode",
        album = "RyFy Spotless",
        durationMs = 210000,
        coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1080&auto=format&fit=crop&q=90",
        isLiked = true,
        videoId = "M2QKS9tte9g"
    ),
    Track(
        id = "nadin_1",
        title = "Semua Aku Dirayakan",
        artist = "Nadin Amizah",
        album = "RyFy Spotless",
        durationMs = 312000,
        coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1080&auto=format&fit=crop&q=90",
        isLiked = true,
        videoId = "2A9Atl2hUkg"
    ),
    Track(
        id = "nidji_1",
        title = "Hapus Aku",
        artist = "Nidji",
        album = "RyFy Spotless",
        durationMs = 250000,
        coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1080&auto=format&fit=crop&q=90",
        isLiked = false,
        videoId = "2MyjWg7P00A"
    ),
    Track(
        id = "raim_2",
        title = "Keluarga Bahagia (Suara 1)",
        artist = "Raim Laode",
        album = "RyFy Spotless",
        durationMs = 222000,
        coverUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=1080&auto=format&fit=crop&q=90",
        isLiked = false,
        videoId = "gR_qbfGkwpc"
    ),
    Track(
        id = "nadin_2",
        title = "Suar",
        artist = "Nadin Amizah",
        album = "RyFy Spotless",
        durationMs = 260000,
        coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1080&auto=format&fit=crop&q=90",
        isLiked = false,
        videoId = "V697wAtn6cI"
    )
)

val SAMPLE_TRACKS = INITIAL_RECOMMENDED_TRACKS

@Composable
fun HomeScreen() {
    val recentTracks by com.bahri.ryfy.data.RecentlyPlayedManager.recentTracks.collectAsState()
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
    val filters = listOf("Semua", "Pop", "Indie", "Rock", "Akustik", "K-Pop", "Chill")

    var artistMixTracks by remember { mutableStateOf(INITIAL_RECOMMENDED_TRACKS) }
    var topCharts by remember { mutableStateOf(INITIAL_RECOMMENDED_TRACKS) }
    var radioStations by remember { mutableStateOf(INITIAL_RECOMMENDED_TRACKS) }
    var isAutofetching by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFilterIndex) {
        val genre = filters[selectedFilterIndex]
        if (genre == "Semua") {
            try {
                val hits = MusicApiService.searchSongs("Raim Laode Nadin Amizah Bernadya")
                if (hits.isNotEmpty()) artistMixTracks = hits
                val charts = MusicApiService.searchSongs("Top Hits Indonesia 2026")
                if (charts.isNotEmpty()) topCharts = charts
                val radio = MusicApiService.searchSongs("Radio Artis Populer Indonesia")
                if (radio.isNotEmpty()) radioStations = radio
            } catch (_: Exception) {}
        } else {
            isAutofetching = true
            try {
                val query = when (genre) {
                    "Pop" -> "Lagu Pop Indonesia Terpopuler 2026"
                    "Indie" -> "Lagu Indie Indonesia Paling Populer"
                    "Rock" -> "Lagu Rock Indonesia Terbaik"
                    "Akustik" -> "Lagu Akustik Santai Indonesia"
                    "K-Pop" -> "K-Pop Top Hits 2026"
                    "Chill" -> "Chill Relaxing Lo-Fi Music"
                    else -> "$genre Hits"
                }
                val results = MusicApiService.searchSongs(query)
                if (results.isNotEmpty()) {
                    artistMixTracks = results.take(6)
                    topCharts = results.drop(6).ifEmpty { results }
                }
            } catch (_: Exception) {}
            isAutofetching = false
        }
    }

    // Preload stream URLs for top tracks in background so clicking is 0ms instant
    LaunchedEffect(artistMixTracks) {
        artistMixTracks.take(4).forEach { t ->
            val vid = t.videoId ?: t.id
            if (vid.isNotBlank()) {
                PlayerManager.preloadTrack(vid)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Greeting Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyText
                    )
                    if (isAutofetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = SpotifyGreen,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Scrollable Genre Filter Chips Row (Autofetch 1:1 Spotify)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters.size) { index ->
                        val filterName = filters[index]
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

        // Section: Baru Saja Diputar (Recently Played 1:1 Spotify)
        if (recentTracks.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Baru saja diputar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyText,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentTracks) { track ->
                            Column(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { PlayerManager.playTrack(track, listOf(track)) }
                            ) {
                                AsyncImage(
                                    model = track.coverUrl,
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
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
                    }
                }
            }
        }

        // Section 1: Artist Mix Cards (Spotify Mix 1:1)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ARTIST_MIXES) { mix ->
                        MixCard(
                            mix = mix,
                            onClick = {
                                val match = artistMixTracks.find { it.artist.contains(mix.artist, ignoreCase = true) }
                                    ?: artistMixTracks.first()
                                PlayerManager.playTrack(match)
                            }
                        )
                    }
                }
            }
        }

        // Section 2: Recommended Tracks List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val currentCategoryTitle = if (filters[selectedFilterIndex] == "Semua") {
                    "Raim Laode, Bernadya, Nadin Amizah, dan lainnya"
                } else {
                    "Pilihan Terbaik ${filters[selectedFilterIndex]}"
                }

                Text(
                    text = currentCategoryTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText
                )

                artistMixTracks.take(5).forEach { track ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { PlayerManager.playTrack(track, artistMixTracks) }
                            .padding(vertical = 6.dp)
                    ) {
                        if (track.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
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

                        IconButton(onClick = { PlayerManager.playTrack(track, artistMixTracks) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_spotify_play),
                                contentDescription = "Play",
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Stasiun rekomendasi (Radio Artis)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Stasiun rekomendasi",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpotifyText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(radioStations) { track ->
                        RadioCard(
                            track = track,
                            onClick = { PlayerManager.playTrack(track) }
                        )
                    }
                }
            }
        }

        // Section 4: Tangga Lagu Teratas (Indonesia Top 50)
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
    }
}

@Composable
private fun MixCard(
    mix: ArtistMix,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SpotifyElevated,
        modifier = Modifier
            .width(152.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(mix.headerColor)
            )

            AsyncImage(
                model = mix.coverUrl,
                contentDescription = mix.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = mix.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mix.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun RadioCard(
    track: Track,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(SpotifyElevated),
            contentAlignment = Alignment.Center
        ) {
            if (track.coverUrl.isNotBlank()) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            } else {
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
            text = "Radio ${track.artist}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = SpotifyText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Dengan ${track.artist}, dan lainnya",
            style = MaterialTheme.typography.bodySmall,
            color = SpotifyTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
