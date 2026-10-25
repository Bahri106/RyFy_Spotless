package com.bahri.ryfy.data

import android.content.Context
import android.util.Log
import com.bahri.ryfy.data.api.MusicApiService
import com.bahri.ryfy.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object OfflineTrackManager {
    private const val TAG = "OfflineTrackManager"
    private const val PREFS_NAME = "ryfy_offline_prefs"
    private const val KEY_TRACKS = "offline_tracks_list"

    private val _offlineTracks = MutableStateFlow<List<Track>>(emptyList())
    val offlineTracks: StateFlow<List<Track>> = _offlineTracks.asStateFlow()

    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingIds: StateFlow<Set<String>> = _downloadingIds.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_TRACKS, null) ?: return
        val list = mutableListOf<Track>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val localFile = getFile(context, id)
                if (localFile.exists() && localFile.length() > 10000) {
                    list.add(
                        Track(
                            id = id,
                            title = obj.getString("title"),
                            artist = obj.getString("artist"),
                            album = obj.optString("album", "Offline"),
                            durationMs = obj.optLong("durationMs", 210000L),
                            coverUrl = obj.optString("coverUrl"),
                            streamUrl = localFile.toURI().toString(),
                            videoId = obj.optString("videoId")
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        _offlineTracks.value = list
    }

    fun isDownloaded(trackId: String): Boolean {
        val ctx = appContext ?: return false
        val f = getFile(ctx, trackId)
        return f.exists() && f.length() > 10000 && _offlineTracks.value.any { it.id == trackId }
    }

    fun isDownloading(trackId: String): Boolean {
        return _downloadingIds.value.contains(trackId)
    }

    fun getFile(context: Context, trackId: String): File {
        val dir = File(context.filesDir, "offline_tracks")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$trackId.mp4")
    }

    fun downloadTrack(context: Context, track: Track, onFinish: (Boolean) -> Unit = {}) {
        val vid = track.videoId ?: track.id
        if (vid.isBlank() || isDownloaded(track.id)) {
            onFinish(true)
            return
        }

        _downloadingIds.value = _downloadingIds.value + track.id

        scope.launch {
            try {
                val streamUrl = track.streamUrl ?: MusicApiService.resolveStreamUrl(vid)
                if (streamUrl.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _downloadingIds.value = _downloadingIds.value - track.id
                        onFinish(false)
                    }
                    return@launch
                }

                val targetFile = getFile(context, track.id)
                val conn = URL(streamUrl).openConnection()
                conn.connectTimeout = 15000
                conn.readTimeout = 30000

                conn.getInputStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val offlineTrack = track.copy(streamUrl = targetFile.toURI().toString())
                val current = _offlineTracks.value.toMutableList()
                current.removeAll { it.id == track.id }
                current.add(0, offlineTrack)
                _offlineTracks.value = current
                savePrefs(context, current)

                withContext(Dispatchers.Main) {
                    _downloadingIds.value = _downloadingIds.value - track.id
                    onFinish(true)
                }
                Log.d(TAG, "Track downloaded successfully: ${track.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for ${track.title}: ${e.message}")
                withContext(Dispatchers.Main) {
                    _downloadingIds.value = _downloadingIds.value - track.id
                    onFinish(false)
                }
            }
        }
    }

    fun deleteOfflineTrack(context: Context, trackId: String) {
        val f = getFile(context, trackId)
        if (f.exists()) f.delete()
        val current = _offlineTracks.value.filter { it.id != trackId }
        _offlineTracks.value = current
        savePrefs(context, current)
    }

    private fun savePrefs(context: Context, list: List<Track>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { t ->
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
        prefs.edit().putString(KEY_TRACKS, arr.toString()).apply()
    }
}
