package com.bahri.ryfy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.bahri.ryfy.R
import com.bahri.ryfy.model.Track
import com.bahri.ryfy.playback.AudioDeviceHelper
import com.bahri.ryfy.playback.ShuffleMode
import com.bahri.ryfy.playback.SleepTimerManager
import com.bahri.ryfy.playback.rememberAudioVolume
import com.bahri.ryfy.ui.theme.GlassBorderBrush
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import com.bahri.ryfy.ui.theme.liquidGlass
import java.util.Locale

@Composable
fun AnimatedEqualizerWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barWidth: Dp = 3.5.dp,
    barSpacing: Dp = 2.5.dp,
    maxHeight: Dp = 20.dp,
    minHeight: Dp = 4.dp,
    color: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_bars")

    val scales = listOf(
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_0"
            )
        } else remember { mutableFloatStateOf(0.25f) },
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_1"
            )
        } else remember { mutableFloatStateOf(0.25f) },
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_2"
            )
        } else remember { mutableFloatStateOf(0.25f) },
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_3"
            )
        } else remember { mutableFloatStateOf(0.25f) }
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.Bottom
    ) {
        scales.take(barCount).forEach { scaleState ->
            val scale by scaleState
            val currentHeight = if (isPlaying) {
                minHeight + (maxHeight - minHeight) * scale
            } else {
                minHeight
            }
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun IosVolumeSlider(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeDown,
            contentDescription = "Volume Min",
            tint = Color(0x99FFFFFF),
            modifier = Modifier.size(18.dp)
        )

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Volume Max",
            tint = Color(0x99FFFFFF),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun IosPlayerCard(
    track: Track,
    isPlaying: Boolean,
    progressMs: Long,
    shuffleMode: ShuffleMode,
    repeatMode: Int,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: (Track) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenArtist: () -> Unit,
    onDownloadTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (volumeFraction, setVolume) = rememberAudioVolume()
    val activeDeviceName = remember { AudioDeviceHelper.getActiveAudioDeviceName(context) }
    val remainingTimerMs by SleepTimerManager.remainingTimeMs.collectAsState()
    val stopAtEnd by SleepTimerManager.stopAtEndOfTrack.collectAsState()
    val sleepTimerActive = remainingTimerMs != null || stopAtEnd

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    val currentSliderPosition = if (isDraggingSlider) {
        dragPositionMs
    } else {
        progressMs.toFloat()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(30.dp),
                backgroundColor = Color(0xE61E1E24),
                borderWidth = 1.2.dp,
                borderBrush = GlassBorderBrush
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Cover Art (Square 1:1, Rounded 18dp)
            if (track.coverUrl.isNotBlank()) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
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

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Metadata & Soundwave Equalizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B0B0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onOpenArtist)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Realtime Animated Soundwave Equalizer
                AnimatedEqualizerWave(
                    isPlaying = isPlaying,
                    color = Color.White,
                    maxHeight = 20.dp,
                    minHeight = 4.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Progress Timeline Bar with Elapsed & Remaining Time
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
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeIos(currentSliderPosition.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x99FFFFFF)
                    )
                    val remainingMs = (track.durationMs - currentSliderPosition.toLong()).coerceAtLeast(0L)
                    Text(
                        text = "-${formatTimeIos(remainingMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x99FFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Playback Controls Row (Star / Prev / Play-Pause / Next / Lyrics)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star Favorite Button
                IconButton(
                    onClick = { onToggleLike(track) },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = if (track.isLiked) Icons.Filled.Star else Icons.Outlined.StarRate,
                        contentDescription = "Favorite",
                        tint = if (track.isLiked) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_spotify_skip_prev),
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Center Play / Pause Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onTogglePlay)
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_spotify_pause else R.drawable.ic_spotify_play),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF141416),
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_spotify_skip_next),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Lyrics Quick Button
                IconButton(
                    onClick = onOpenLyrics,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Lyrics",
                        tint = if (!track.lyrics.isNullOrBlank()) SpotifyGreen else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Extra Quick Actions Strip (Shuffle, Repeat, Speed, Timer, Download)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Mode
                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(36.dp)) {
                    val isSmart = shuffleMode == ShuffleMode.SMART_SHUFFLE
                    val isNormal = shuffleMode == ShuffleMode.SHUFFLE
                    val iconRes = if (isSmart) R.drawable.ic_spotify_smart_shuffle else R.drawable.ic_spotify_shuffle
                    val tintColor = if (isSmart || isNormal) SpotifyGreen else Color(0x99FFFFFF)
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = "Shuffle",
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Repeat Mode
                IconButton(onClick = onToggleRepeat, modifier = Modifier.size(36.dp)) {
                    val isRepeatOne = repeatMode == Player.REPEAT_MODE_ONE
                    val active = repeatMode != Player.REPEAT_MODE_OFF
                    val iconRes = if (isRepeatOne) R.drawable.ic_spotify_repeat_once else R.drawable.ic_spotify_repeat
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = "Repeat",
                        tint = if (active) SpotifyGreen else Color(0x99FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Playback Speed
                IconButton(onClick = onOpenSpeed, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = Color(0x99FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sleep Timer
                IconButton(onClick = onOpenSleepTimer, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerActive) SpotifyGreen else Color(0x99FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Download Offline
                IconButton(onClick = onDownloadTrack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                        contentDescription = "Download Offline",
                        tint = if (isDownloaded) SpotifyGreen else Color(0x99FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. iOS Volume Slider
            IosVolumeSlider(
                volume = volumeFraction,
                onVolumeChange = setVolume
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 7. AirPlay / Audio Output Pill Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x2BFFFFFF),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { AudioDeviceHelper.openAudioOutputPicker(context) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_airplay),
                        contentDescription = "Audio Output",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeDeviceName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

fun formatTimeIos(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
