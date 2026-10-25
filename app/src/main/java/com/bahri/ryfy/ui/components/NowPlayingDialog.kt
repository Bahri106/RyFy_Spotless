package com.bahri.ryfy.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.bahri.ryfy.R
import com.bahri.ryfy.data.api.MusicApiService
import com.bahri.ryfy.model.Track
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.playback.ShuffleMode
import com.bahri.ryfy.ui.theme.SpotifyBlack
import com.bahri.ryfy.ui.theme.SpotifyDark
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingDialog(
    track: Track,
    isPlaying: Boolean,
    progressMs: Long,
    shuffleMode: ShuffleMode,
    repeatMode: Int,
    onDismiss: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: (Track) -> Unit
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }
    var showArtistSheet by remember { mutableStateOf(false) }

    val currentSliderPosition = if (isDraggingSlider) {
        dragPositionMs
    } else {
        progressMs.toFloat()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpotifyBlack)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = SpotifyText,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MEMUTAR DARI ALBUM",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = SpotifyTextSecondary
                        )
                        Text(
                            text = if (track.album.isBlank() || track.album == "YouTube Music") "RyFy Spotless" else track.album,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = SpotifyText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Big Square Album Art (High Resolution HD)
                if (track.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpotifyHighlight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SpotifyTextSecondary,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Title, Artist and Heart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SpotifyGreen,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showArtistSheet = true }
                                .padding(vertical = 2.dp)
                        )
                    }

                    // Like Button (Official Spotify Heart)
                    IconButton(onClick = { onToggleLike(track) }) {
                        Icon(
                            painter = painterResource(if (track.isLiked) R.drawable.ic_spotify_heart_active else R.drawable.ic_spotify_heart),
                            contentDescription = "Like",
                            tint = if (track.isLiked) SpotifyGreen else SpotifyText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seekbar Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentSliderPosition.coerceIn(0f, track.durationMs.toFloat().coerceAtLeast(1f)),
                        onValueChange = {
                            isDraggingSlider = true
                            dragPositionMs = it
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            onSeek(dragPositionMs.toLong())
                        },
                        valueRange = 0f..track.durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SpotifyText,
                            activeTrackColor = SpotifyText,
                            inactiveTrackColor = SpotifyHighlight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentSliderPosition.toLong()),
                            style = MaterialTheme.typography.labelSmall,
                            color = SpotifyTextSecondary
                        )
                        Text(
                            text = formatTime(track.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = SpotifyTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Controls (Shuffle/Smart Shuffle, Prev, Play/Pause, Next, Repeat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 3-State Spotify Shuffle (OFF / Normal Shuffle / Smart Shuffle ✨)
                    IconButton(onClick = onToggleShuffle) {
                        val isSmart = shuffleMode == ShuffleMode.SMART_SHUFFLE
                        val isNormal = shuffleMode == ShuffleMode.SHUFFLE
                        val iconRes = if (isSmart) R.drawable.ic_spotify_smart_shuffle else R.drawable.ic_spotify_shuffle
                        val tintColor = if (isSmart || isNormal) SpotifyGreen else SpotifyTextSecondary
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = "Shuffle",
                            tint = tintColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Skip Previous
                    IconButton(onClick = onPrevious) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spotify_skip_prev),
                            contentDescription = "Previous",
                            tint = SpotifyText,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Main Big White Play/Pause Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SpotifyText)
                            .clickable(onClick = onTogglePlay)
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.ic_spotify_pause else R.drawable.ic_spotify_play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SpotifyDark,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Skip Next
                    IconButton(onClick = onNext) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spotify_skip_next),
                            contentDescription = "Next",
                            tint = SpotifyText,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat / Repeat One
                    IconButton(onClick = onToggleRepeat) {
                        val isRepeatOne = repeatMode == Player.REPEAT_MODE_ONE
                        val active = repeatMode != Player.REPEAT_MODE_OFF
                        val iconRes = if (isRepeatOne) R.drawable.ic_spotify_repeat_once else R.drawable.ic_spotify_repeat
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = "Repeat",
                            tint = if (active) SpotifyGreen else SpotifyTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Lyrics Preview Box (Spotify Style)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SpotifyElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIRIK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyGreen
                            )
                            Text(
                                text = "BUKA PENUH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (!track.lyrics.isNullOrBlank()) track.lyrics!! else "Nikmati audio berkualitas tinggi 100% bebas iklan di RyFy Spotless.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            color = SpotifyText
                        )
                    }
                }
            }

            // Artist Discography BottomSheet
            if (showArtistSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var artistTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
                var isLoadingArtist by remember { mutableStateOf(true) }

                LaunchedEffect(track.artist) {
                    isLoadingArtist = true
                    try {
                        val results = MusicApiService.searchSongs(track.artist)
                        artistTracks = results
                    } catch (_: Exception) {}
                    isLoadingArtist = false
                }

                ModalBottomSheet(
                    onDismissRequest = { showArtistSheet = false },
                    sheetState = sheetState,
                    containerColor = SpotifyElevated,
                    contentColor = SpotifyText
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "Karya dari ${track.artist}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (isLoadingArtist) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SpotifyGreen)
                            }
                        } else if (artistTracks.isEmpty()) {
                            Text(
                                text = "Tidak ditemukan lagu lain dari artis ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SpotifyTextSecondary,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(artistTracks) { aTrack ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                showArtistSheet = false
                                                PlayerManager.playTrack(aTrack)
                                            }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        if (aTrack.coverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = aTrack.coverUrl,
                                                contentDescription = aTrack.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                        } else {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SpotifyHighlight)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = SpotifyTextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = aTrack.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (aTrack.id == track.id) SpotifyGreen else SpotifyText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = aTrack.artist,
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
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
