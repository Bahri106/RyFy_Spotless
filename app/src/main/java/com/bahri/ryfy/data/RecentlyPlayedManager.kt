package com.bahri.ryfy.data

import android.content.Context
import com.bahri.ryfy.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object RecentlyPlayedManager {
    private const val PREFS_NAME = "ryfy_recently_played_prefs"
    private const val KEY_RECENT = "recent_tracks"
    private const val MAX_ENTRIES = 20

    private val _recentTracks = MutableStateFlow<List<Track>>(emptyList())
    val recentTracks: StateFlow<List<Track>> = _recentTracks.asStateFlow()

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECENT, null) ?: return
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
                        isLiked = obj.optBoolean("isLiked", false),
                        videoId = obj.optString("videoId")
                    )
                )
            }
        } catch (_: Exception) {}
        _recentTracks.value = list
    }

    fun addTrack(track: Track) {
        val current = _recentTracks.value.toMutableList()
        current.removeAll {
            it.id == track.id || (it.title.equals(track.title, true) && it.artist.equals(track.artist, true))
        }
        current.add(0, track)
        if (current.size > MAX_ENTRIES) {
            current.removeAt(current.lastIndex)
        }
        _recentTracks.value = current
        saveToPrefs(current)
    }

    private fun saveToPrefs(list: List<Track>) {
        val ctx = appContext ?: return
        runCatching {
            val arr = JSONArray()
            list.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("artist", t.artist)
                    put("album", t.album)
                    put("durationMs", t.durationMs)
                    put("coverUrl", t.coverUrl)
                    put("isLiked", t.isLiked)
                    put("videoId", t.videoId)
                }
                arr.put(obj)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_RECENT, arr.toString()).apply()
        }
    }
}
