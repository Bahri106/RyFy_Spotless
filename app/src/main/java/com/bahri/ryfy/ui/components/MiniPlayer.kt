package com.bahri.ryfy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bahri.ryfy.model.Track
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progressMs: Long,
    onClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleLike: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (track.durationMs > 0) {
        (progressMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SpotifyElevated,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Album Art
                if (track.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
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

                // Track Title & Artist
                Column(modifier = Modifier.weight(1f)) {
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

                // Like Button
                IconButton(onClick = { onToggleLike(track) }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (track.isLiked) SpotifyGreen else SpotifyTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play / Pause Button
                IconButton(onClick = onTogglePlay, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = SpotifyText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Bottom Progress Bar Line
            LinearProgressIndicator(
                progress = { progress },
                color = SpotifyGreen,
                trackColor = SpotifyHighlight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            )
        }
    }
}
