package com.bahri.ryfy.data

import android.content.Context
import com.bahri.ryfy.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object FavoritesManager {
    private const val PREFS_NAME = "ryfy_favorites_prefs"
    private const val KEY_FAVORITES = "favorite_tracks"

    private val _favorites = MutableStateFlow<List<Track>>(emptyList())
    val favorites: StateFlow<List<Track>> = _favorites.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return
        val list = mutableListOf<Track>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Track(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        artist = obj.getString("artist"),
                        album = obj.optString("album", "RyFy Spotless"),
                        durationMs = obj.optLong("durationMs", 210000L),
                        coverUrl = obj.optString("coverUrl"),
                        isLiked = true,
                        videoId = obj.optString("videoId")
                    )
                )
            }
        } catch (_: Exception) {}
        _favorites.value = list
    }

    fun isLiked(trackId: String): Boolean {
        return _favorites.value.any { it.id == trackId }
    }

    fun toggleLike(context: Context, track: Track): Boolean {
        val current = _favorites.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.id == track.id || (it.title.equals(track.title, true) && it.artist.equals(track.artist, true))
        }

        val likedNow: Boolean
        if (existingIndex != -1) {
            current.removeAt(existingIndex)
            likedNow = false
        } else {
            current.add(0, track.copy(isLiked = true))
            likedNow = true
        }

        _favorites.value = current
        save(context, current)
        return likedNow
    }

    private fun save(context: Context, list: List<Track>) {
        val arr = JSONArray()
        for (t in list) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("artist", t.artist)
                put("album", t.album)
                put("durationMs", t.durationMs)
                put("coverUrl", t.coverUrl)
                put("videoId", t.videoId)
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FAVORITES, arr.toString())
            .apply()
    }
}
