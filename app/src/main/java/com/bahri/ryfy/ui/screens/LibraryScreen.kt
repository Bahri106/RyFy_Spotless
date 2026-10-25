package com.bahri.ryfy.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bahri.ryfy.data.CustomPlaylist
import com.bahri.ryfy.data.FavoritesManager
import com.bahri.ryfy.data.OfflineTrackManager
import com.bahri.ryfy.data.PlaylistManager
import com.bahri.ryfy.playback.PlayerManager
import com.bahri.ryfy.ui.theme.SpotifyDark
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyHighlight
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    val favorites by FavoritesManager.favorites.collectAsState()
    val customPlaylists by PlaylistManager.playlists.collectAsState()
    val offlineTracks by OfflineTrackManager.offlineTracks.collectAsState()

    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Playlist", "Artis", "Diunduh")

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showImportPlaylistDialog by remember { mutableStateOf(false) }
    var showOfflineSheet by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<CustomPlaylist?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Koleksi Kamu",
                        style = MaterialTheme.typography.headlineLarge,
                        color = SpotifyText
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { showCreatePlaylistDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Buat Playlist",
                                tint = SpotifyText
                            )
                        }
                        IconButton(onClick = { showImportPlaylistDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Impor Playlist Bersama",
                                tint = SpotifyText
                            )
                        }
                    }
                }
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

        // Liked Songs row (Actual Reactive Count)
        item {
            LibraryRow(
                title = "Lagu yang Disukai",
                subtitle = "Sematkan · ${favorites.size} lagu",
                brush = Brush.linearGradient(listOf(Color(0xFF450AF5), Color(0xFFC4EFD9))),
                icon = Icons.Default.Favorite,
                onClick = {
                    if (favorites.isNotEmpty()) {
                        PlayerManager.playTrack(favorites.first(), favorites)
                    } else {
                        Toast.makeText(context, "Belum ada lagu yang disukai", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Offline Downloaded Tracks row
        item {
            LibraryRow(
                title = "Musik Tersimpan Offline",
                subtitle = "${offlineTracks.size} lagu · Tersedia tanpa internet",
                brush = Brush.linearGradient(listOf(Color(0xFF006450), Color(0xFF1DB954))),
                icon = Icons.Default.DownloadDone,
                onClick = {
                    if (offlineTracks.isNotEmpty()) {
                        showOfflineSheet = true
                    } else {
                        Toast.makeText(context, "Belum ada lagu yang diunduh. Unduh lewat menu titik 3 di pemutar.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Section: Custom User Playlists
        item {
            Text(
                text = "Playlist Kamu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SpotifyText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }

        if (customPlaylists.isEmpty()) {
            item {
                Text(
                    text = "Belum ada playlist kustom. Ketuk ikon '+' di atas untuk membuat playlist baru.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(customPlaylists) { playlist ->
                LibraryRow(
                    title = playlist.name,
                    subtitle = "Playlist · ${playlist.tracks.size} lagu",
                    brush = Brush.linearGradient(listOf(Color(0xFF1E3264), Color(0xFF8D67AB))),
                    icon = Icons.Default.QueueMusic,
                    onClick = {
                        selectedPlaylistForDetail = playlist
                    }
                )
            }
        }
    }

    // Playlist Detail BottomSheet
    selectedPlaylistForDetail?.let { playlist ->
        val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedPlaylistForDetail = null },
            sheetState = detailSheetState,
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${playlist.tracks.size} lagu tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextSecondary
                        )
                    }

                    Row {
                        // Play All Button
                        if (playlist.tracks.isNotEmpty()) {
                            IconButton(onClick = {
                                PlayerManager.playTrack(playlist.tracks.first(), playlist.tracks)
                                selectedPlaylistForDetail = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Putar Semua",
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Share Playlist Button (Exports to link/code)
                        IconButton(onClick = {
                            val code = PlaylistManager.exportPlaylistToBase64(playlist)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Playlist ${playlist.name}")
                                putExtra(Intent.EXTRA_TEXT, "Buka playlist '${playlist.name}' di RyFy Spotless:\nryfy://playlist?data=$code")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Playlist"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan Playlist",
                                tint = SpotifyText,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Delete Playlist Button
                        IconButton(onClick = {
                            PlaylistManager.deletePlaylist(context, playlist.id)
                            selectedPlaylistForDetail = null
                            Toast.makeText(context, "Playlist dihapus", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Playlist",
                                tint = SpotifyTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                if (playlist.tracks.isEmpty()) {
                    Text(
                        text = "Playlist ini masih kosong. Tambahkan lagu lewat menu titik 3 di pemutar musik.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpotifyTextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlist.tracks) { track ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        PlayerManager.playTrack(track, playlist.tracks)
                                        selectedPlaylistForDetail = null
                                    }
                                    .padding(vertical = 4.dp)
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

                                IconButton(onClick = {
                                    PlaylistManager.removeTrackFromPlaylist(context, playlist.id, track.id)
                                    val updated = customPlaylists.find { it.id == playlist.id }
                                    selectedPlaylistForDetail = updated
                                }) {
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

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = SpotifyElevated,
            title = {
                Text(
                    text = "Buat Playlist Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText
                )
            },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    placeholder = { Text("Nama playlist", color = SpotifyTextSecondary) },
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
                        if (playlistName.isNotBlank()) {
                            PlaylistManager.createPlaylist(context, playlistName.trim())
                            showCreatePlaylistDialog = false
                            Toast.makeText(context, "Playlist dibuat!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
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

    // Import Shared Playlist Dialog
    if (showImportPlaylistDialog) {
        var shareCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportPlaylistDialog = false },
            containerColor = SpotifyElevated,
            title = {
                Text(
                    text = "Buka Playlist Bersama",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Tempel tautan atau kode playlist yang dibagikan oleh teman Anda:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = shareCode,
                        onValueChange = { shareCode = it },
                        placeholder = { Text("ryfy://playlist?data=...", color = SpotifyTextSecondary) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpotifyText,
                            unfocusedTextColor = SpotifyText,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = SpotifyHighlight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shareCode.isNotBlank()) {
                            val imported = PlaylistManager.importPlaylistFromBase64(context, shareCode.trim())
                            showImportPlaylistDialog = false
                            if (imported != null) {
                                Toast.makeText(context, "Playlist '${imported.name}' berhasil diimpor!", Toast.LENGTH_SHORT).show()
                                if (imported.tracks.isNotEmpty()) {
                                    PlayerManager.playTrack(imported.tracks.first(), imported.tracks)
                                }
                            } else {
                                Toast.makeText(context, "Format kode playlist tidak valid", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Buka & Putar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportPlaylistDialog = false }) {
                    Text("Batal", color = SpotifyTextSecondary)
                }
            }
        )
    }

    // Offline Tracks BottomSheet
    if (showOfflineSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showOfflineSheet = false },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Musik Tersimpan Offline",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyText
                        )
                        Text(
                            text = "${offlineTracks.size} lagu siap diputar tanpa internet",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyGreen
                        )
                    }

                    if (offlineTracks.isNotEmpty()) {
                        IconButton(onClick = {
                            PlayerManager.playTrack(offlineTracks.first(), offlineTracks)
                            showOfflineSheet = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Putar Semua Offline",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = SpotifyHighlight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(offlineTracks) { track ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    PlayerManager.playTrack(track, offlineTracks)
                                    showOfflineSheet = false
                                }
                                .padding(vertical = 4.dp)
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

                            IconButton(onClick = {
                                OfflineTrackManager.deleteOfflineTrack(context, track.id)
                                Toast.makeText(context, "Lagu offline dihapus", Toast.LENGTH_SHORT).show()
                            }) {
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
