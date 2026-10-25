package com.bahri.ryfy.ui.components

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.bahri.ryfy.data.PlaylistManager
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
    val context = LocalContext.current
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    var showArtistSheet by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val queueList by PlayerManager.queue.collectAsState()
    val playlists by PlaylistManager.playlists.collectAsState()

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

                    // 3-dots More Menu
                    IconButton(onClick = { showMoreOptions = true }) {
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
                    // 3-State Spotify Shuffle
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

            // 1. More Options BottomSheet (3-Dots Menu)
            if (showMoreOptions) {
                val moreOptionsSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showMoreOptions = false },
                    sheetState = moreOptionsSheet,
                    containerColor = SpotifyElevated,
                    contentColor = SpotifyText
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Header Preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            if (track.coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = track.coverUrl,
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.titleMedium,
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

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 8.dp))

                        // Option 1: Suka / Tidak Suka
                        MoreOptionItem(
                            iconPainter = painterResource(if (track.isLiked) R.drawable.ic_spotify_heart_active else R.drawable.ic_spotify_heart),
                            title = if (track.isLiked) "Hapus dari Lagu yang Disukai" else "Sukai Lagu Ini",
                            iconTint = if (track.isLiked) SpotifyGreen else SpotifyText,
                            onClick = {
                                onToggleLike(track)
                                showMoreOptions = false
                            }
                        )

                        // Option 2: Tambahkan ke Playlist
                        MoreOptionItem(
                            iconVector = Icons.Default.PlaylistAdd,
                            title = "Tambahkan ke Playlist",
                            onClick = {
                                showMoreOptions = false
                                showAddToPlaylistDialog = true
                            }
                        )

                        // Option 3: Antrean Pemutaran
                        MoreOptionItem(
                            iconVector = Icons.Default.QueueMusic,
                            title = "Buka Antrean Pemutaran",
                            onClick = {
                                showMoreOptions = false
                                showQueueSheet = true
                            }
                        )

                        // Option 4: Karya Artis
                        MoreOptionItem(
                            iconVector = Icons.Default.Person,
                            title = "Karya Lain dari ${track.artist}",
                            onClick = {
                                showMoreOptions = false
                                showArtistSheet = true
                            }
                        )

                        // Option 5: Bagikan
                        MoreOptionItem(
                            iconVector = Icons.Default.Share,
                            title = "Bagikan Lagu",
                            onClick = {
                                showMoreOptions = false
                                val shareText = "Mendengarkan '${track.title}' oleh ${track.artist} di RyFy Spotless (Bebas Iklan): https://t.me/ryfyProject"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, track.title)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan lewat"))
                            }
                        )
                    }
                }
            }

            // 2. Queue List BottomSheet (Antrean Lagu)
            if (showQueueSheet) {
                val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showQueueSheet = false },
                    sheetState = queueSheetState,
                    containerColor = SpotifyElevated,
                    contentColor = SpotifyText
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Antrean Pemutaran",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyText
                            )
                            Text(
                                text = "Kosongkan",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotifyTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        PlayerManager.clearQueueExceptCurrent()
                                    }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Sedang Memutar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen,
                            letterSpacing = 1.sp
                        )

                        // Currently Playing Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            if (track.coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = track.coverUrl,
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyGreen,
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

                        HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "Berikutnya dalam Antrean",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val nextTracks = queueList.filter { it.id != track.id }
                        if (nextTracks.isEmpty()) {
                            Text(
                                text = "Tidak ada lagu lain dalam antrean. Lagu rekomendasi artis akan diputar otomatis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotifyTextSecondary,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(nextTracks) { qTrack ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                PlayerManager.playTrack(qTrack)
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        if (qTrack.coverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = qTrack.coverUrl,
                                                contentDescription = qTrack.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = qTrack.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SpotifyText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = qTrack.artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SpotifyTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(onClick = { PlayerManager.removeFromQueue(qTrack.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus",
                                                tint = SpotifyTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Add to Playlist Dialog
            if (showAddToPlaylistDialog) {
                AlertDialog(
                    onDismissRequest = { showAddToPlaylistDialog = false },
                    containerColor = SpotifyElevated,
                    title = {
                        Text(
                            text = "Tambahkan ke Playlist",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Option: Create New Playlist
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showAddToPlaylistDialog = false
                                        showCreatePlaylistDialog = true
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SpotifyHighlight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Buat Playlist Baru",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyGreen
                                )
                            }

                            HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp)

                            if (playlists.isEmpty()) {
                                Text(
                                    text = "Belum ada playlist kustom. Buat playlist baru di atas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(playlists) { p ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    val added = PlaylistManager.addTrackToPlaylist(context, p.id, track)
                                                    showAddToPlaylistDialog = false
                                                    if (added) {
                                                        Toast.makeText(context, "Ditambahkan ke '${p.name}'", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Lagu sudah ada di '${p.name}'", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SpotifyHighlight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = SpotifyTextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = p.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SpotifyText
                                                )
                                                Text(
                                                    text = "${p.tracks.size} lagu",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SpotifyTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAddToPlaylistDialog = false }) {
                            Text("Tutup", color = SpotifyTextSecondary)
                        }
                    }
                )
            }

            // 4. Create Playlist Dialog
            if (showCreatePlaylistDialog) {
                var newName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showCreatePlaylistDialog = false },
                    containerColor = SpotifyElevated,
                    title = {
                        Text(
                            text = "Beri Nama Playlist",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            placeholder = { Text("Nama playlist Anda", color = SpotifyTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SpotifyText,
                                unfocusedTextColor = SpotifyText,
                                focusedBorderColor = SpotifyGreen,
                                unfocusedBorderColor = SpotifyHighlight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    val newP = PlaylistManager.createPlaylist(context, newName.trim())
                                    PlaylistManager.addTrackToPlaylist(context, newP.id, track)
                                    showCreatePlaylistDialog = false
                                    Toast.makeText(context, "Playlist '${newP.name}' dibuat & lagu ditambahkan!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                        ) {
                            Text("Buat", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreatePlaylistDialog = false }) {
                            Text("Batal", color = SpotifyTextSecondary)
                        }
                    }
                )
            }

            // 5. Artist Discography BottomSheet
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

@Composable
private fun MoreOptionItem(
    iconVector: ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    iconTint: Color = SpotifyText,
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        } else if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = SpotifyText
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
