package com.bahri.ryfy.data

import android.content.Context
import android.net.Uri
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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object OfflineTrackManager {
    private const val TAG = "OfflineTrackManager"
    private const val PREFS_NAME = "ryfy_offline_prefs"
    private const val KEY_TRACKS = "offline_tracks_list"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

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
                            streamUrl = Uri.fromFile(localFile).toString(),
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
        val safeId = trackId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(dir, "$safeId.mp4")
    }

    fun downloadTrack(context: Context, track: Track, onFinish: (Boolean) -> Unit = {}) {
        val vid = track.videoId?.ifBlank { null } ?: track.id
        if (vid.isBlank() || isDownloaded(track.id)) {
            onFinish(true)
            return
        }

        _downloadingIds.value = _downloadingIds.value + track.id

        scope.launch {
            val targetFile = getFile(context, track.id)
            val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
            var success = false

            try {
                // 1. Resolve candidate stream URLs: Existing HTTP -> Primary VPS -> Fallback Invidious
                val primaryUrl = track.streamUrl.takeIf { !it.isNullOrBlank() && it.startsWith("http") }
                    ?: MusicApiService.resolveStreamUrl(vid)
                val fallbackUrl = MusicApiService.resolveFallbackStreamUrl(vid)

                val candidates = listOfNotNull(primaryUrl, fallbackUrl).distinct()

                for (url in candidates) {
                    try {
                        val req = Request.Builder()
                            .url(url)
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) RyFy/1.0.18")
                            .header("Accept", "*/*")
                            .build()

                        httpClient.newCall(req).execute().use { resp ->
                            if (resp.isSuccessful && resp.body != null) {
                                val body = resp.body!!
                                body.byteStream().use { input ->
                                    FileOutputStream(tempFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                if (tempFile.exists() && tempFile.length() > 10000) {
                                    if (targetFile.exists()) targetFile.delete()
                                    if (tempFile.renameTo(targetFile)) {
                                        success = true
                                        return@use
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Download attempt failed for $url: ${e.message}")
                    }

                    if (success) break
                }

                if (success) {
                    val offlineUri = Uri.fromFile(targetFile).toString()
                    val offlineTrack = track.copy(
                        streamUrl = offlineUri,
                        videoId = vid
                    )
                    val current = _offlineTracks.value.toMutableList()
                    current.removeAll { it.id == track.id }
                    current.add(0, offlineTrack)
                    _offlineTracks.value = current
                    savePrefs(context, current)
                    Log.d(TAG, "Track downloaded successfully: ${track.title} (${targetFile.length()} bytes)")
                } else {
                    if (tempFile.exists()) tempFile.delete()
                    Log.e(TAG, "All stream candidates failed for ${track.title}")
                }
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                Log.e(TAG, "Download fatal error for ${track.title}: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    _downloadingIds.value = _downloadingIds.value - track.id
                    onFinish(success)
                }
            }
        }
    }

    fun deleteOfflineTrack(context: Context, trackId: String) {
        val f = getFile(context, trackId)
        if (f.exists()) f.delete()
        val temp = File(f.parentFile, "${f.name}.tmp")
        if (temp.exists()) temp.delete()

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
