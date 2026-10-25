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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.model.GenreCategory
import com.bahri.ryfy.ui.theme.SpotifyDark
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

val GENRE_CATEGORIES = listOf(
    GenreCategory("1", "Pop", 0xFF8D67AB),
    GenreCategory("2", "Hip-Hop", 0xFFBA5D07),
    GenreCategory("3", "Rock", 0xFFE91429),
    GenreCategory("4", "Indie", 0xFFE1118C),
    GenreCategory("5", "K-Pop", 0xFF148A08),
    GenreCategory("6", "Anime", 0xFF006450),
    GenreCategory("7", "Jazz", 0xFF1E3264),
    GenreCategory("8", "Dance/Elektronik", 0xFF27856A),
    GenreCategory("9", "Gaming", 0xFF8C1932),
    GenreCategory("10", "Chill", 0xFF477D95),
    GenreCategory("11", "Folk & Akustik", 0xFF503750),
    GenreCategory("12", "R&B", 0xFFDC148C)
)

@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    text = "Cari",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpotifyText
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Spotify Search Bar Input (White Card)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF121212),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Apa yang ingin kamu dengarkan?",
                                    color = Color(0xFF747474),
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF121212),
                                    fontSize = 14.5.sp
                                ),
                                cursorBrush = SolidColor(SpotifyDark),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF121212),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Jelajahi Semua",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyText
                )
            }
        }

        items(GENRE_CATEGORIES) { genre ->
            GenreCard(genre = genre)
        }
    }
}

@Composable
private fun GenreCard(genre: GenreCategory) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(genre.colorHex),
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable { /* Select genre */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}
