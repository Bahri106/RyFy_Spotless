package com.bahri.ryfy.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverUrl: String,
    val streamUrl: String? = null,
    val videoId: String? = null,
    val lyrics: String? = null,
    val isLiked: Boolean = false
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val tracks: List<Track> = emptyList()
)

data class GenreCategory(
    val id: String,
    val name: String,
    val colorHex: Long,
    val iconUrl: String = ""
)
