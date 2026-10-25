package com.bahri.ryfy.data

import android.content.Context
import android.util.Base64
import com.bahri.ryfy.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CustomPlaylist(
    val id: String,
    val name: String,
    val tracks: List<Track>,
    val createdAt: Long = System.currentTimeMillis()
)

object PlaylistManager {
    private const val PREFS_NAME = "ryfy_playlists_prefs"
    private const val KEY_PLAYLISTS = "custom_playlists"

    private val _playlists = MutableStateFlow<List<CustomPlaylist>>(emptyList())
    val playlists: StateFlow<List<CustomPlaylist>> = _playlists.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_PLAYLISTS, null) ?: return
        val list = mutableListOf<CustomPlaylist>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val pObj = arr.getJSONObject(i)
                val pId = pObj.getString("id")
                val pName = pObj.getString("name")
                val pCreated = pObj.optLong("createdAt", System.currentTimeMillis())
                val tArr = pObj.optJSONArray("tracks") ?: JSONArray()
                val tList = mutableListOf<Track>()
                for (j in 0 until tArr.length()) {
                    val tObj = tArr.getJSONObject(j)
                    tList.add(
                        Track(
                            id = tObj.getString("id"),
                            title = tObj.getString("title"),
                            artist = tObj.getString("artist"),
                            album = tObj.optString("album", "RyFy Spotless"),
                            durationMs = tObj.optLong("durationMs", 210000L),
                            coverUrl = tObj.optString("coverUrl"),
                            videoId = tObj.optString("videoId")
                        )
                    )
                }
                list.add(CustomPlaylist(pId, pName, tList, pCreated))
            }
        } catch (_: Exception) {}
        _playlists.value = list
    }

    fun createPlaylist(context: Context, name: String): CustomPlaylist {
        val newPlaylist = CustomPlaylist(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Playlist Baru" },
            tracks = emptyList()
        )
        val current = _playlists.value.toMutableList()
        current.add(0, newPlaylist)
        _playlists.value = current
        save(context, current)
        return newPlaylist
    }

    fun deletePlaylist(context: Context, playlistId: String) {
        val current = _playlists.value.toMutableList()
        current.removeAll { it.id == playlistId }
        _playlists.value = current
        save(context, current)
    }

    fun addTrackToPlaylist(context: Context, playlistId: String, track: Track): Boolean {
        val current = _playlists.value.toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index == -1) return false
        val playlist = current[index]
        if (playlist.tracks.any { it.id == track.id }) return false // already in playlist
        val updatedTracks = listOf(track) + playlist.tracks
        current[index] = playlist.copy(tracks = updatedTracks)
        _playlists.value = current
        save(context, current)
        return true
    }

    fun removeTrackFromPlaylist(context: Context, playlistId: String, trackId: String) {
        val current = _playlists.value.toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index == -1) return
        val playlist = current[index]
        val updatedTracks = playlist.tracks.filter { it.id != trackId }
        current[index] = playlist.copy(tracks = updatedTracks)
        _playlists.value = current
        save(context, current)
    }

    fun exportPlaylistToBase64(playlist: CustomPlaylist): String {
        val obj = JSONObject().apply {
            put("id", playlist.id)
            put("name", playlist.name)
            val tArr = JSONArray()
            for (t in playlist.tracks) {
                tArr.put(
                    JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("artist", t.artist)
                        put("album", t.album)
                        put("durationMs", t.durationMs)
                        put("coverUrl", t.coverUrl)
                        put("videoId", t.videoId)
                    }
                )
            }
            put("tracks", tArr)
        }
        return Base64.encodeToString(obj.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun importPlaylistFromBase64(context: Context, codeOrUrl: String): CustomPlaylist? {
        try {
            var rawBase64 = codeOrUrl.trim()
            if (rawBase64.contains("data=")) {
                rawBase64 = rawBase64.substringAfter("data=").substringBefore("&")
            }
            val jsonStr = String(Base64.decode(rawBase64, Base64.NO_WRAP), Charsets.UTF_8)
            val pObj = JSONObject(jsonStr)
            val name = pObj.optString("name", "Playlist Impor")
            val tArr = pObj.optJSONArray("tracks") ?: JSONArray()
            val tList = mutableListOf<Track>()
            for (i in 0 until tArr.length()) {
                val tObj = tArr.getJSONObject(i)
                tList.add(
                    Track(
                        id = tObj.getString("id"),
                        title = tObj.getString("title"),
                        artist = tObj.getString("artist"),
                        album = tObj.optString("album", "RyFy Spotless"),
                        durationMs = tObj.optLong("durationMs", 210000L),
                        coverUrl = tObj.optString("coverUrl"),
                        videoId = tObj.optString("videoId")
                    )
                )
            }
            val imported = CustomPlaylist(
                id = UUID.randomUUID().toString(),
                name = "$name (Dibagikan)",
                tracks = tList
            )
            val current = _playlists.value.toMutableList()
            current.add(0, imported)
            _playlists.value = current
            save(context, current)
            return imported
        } catch (_: Exception) {
            return null
        }
    }

    private fun save(context: Context, list: List<CustomPlaylist>) {
        val arr = JSONArray()
        for (p in list) {
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("createdAt", p.createdAt)
                val tArr = JSONArray()
                for (t in p.tracks) {
                    tArr.put(
                        JSONObject().apply {
                            put("id", t.id)
                            put("title", t.title)
                            put("artist", t.artist)
                            put("album", t.album)
                            put("durationMs", t.durationMs)
                            put("coverUrl", t.coverUrl)
                            put("videoId", t.videoId)
                        }
                    )
                }
                put("tracks", tArr)
            }
            arr.put(pObj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PLAYLISTS, arr.toString())
            .apply()
    }
}
