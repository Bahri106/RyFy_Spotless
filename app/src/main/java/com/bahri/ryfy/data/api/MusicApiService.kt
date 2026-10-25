package com.bahri.ryfy.data.api

import android.util.Log
import com.bahri.ryfy.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object MusicApiService {
    private const val TAG = "MusicApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val INVIDIOUS_INSTANCES = listOf(
        "https://yt.chocolatemoo53.com",
        "https://inv.nadeko.net",
        "https://invidious.nerdvpn.de",
        "https://invidious.tiekoetter.com"
    )

    /**
     * Search songs via YouTube Music Innertube API (Real music database, no API key needed).
     */
    suspend fun searchSongs(query: String): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "id")
                        put("gl", "ID")
                    })
                })
                put("query", query)
                put("params", "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D") // Music Songs Filter
            }

            val request = Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/search")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Origin", "https://music.youtube.com")
                .header("Referer", "https://music.youtube.com/")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)

                val tabs = json.optJSONObject("contents")
                    ?.optJSONObject("tabbedSearchResultsRenderer")
                    ?.optJSONArray("tabs") ?: return@withContext emptyList()

                val sections = tabs.optJSONObject(0)
                    ?.optJSONObject("tabRenderer")
                    ?.optJSONObject("content")
                    ?.optJSONObject("sectionListRenderer")
                    ?.optJSONArray("contents") ?: return@withContext emptyList()

                val shelf = sections.optJSONObject(0)?.optJSONObject("musicShelfRenderer")
                val contents = shelf?.optJSONArray("contents") ?: return@withContext emptyList()

                val list = mutableListOf<Track>()
                for (i in 0 until contents.length()) {
                    val item = contents.optJSONObject(i)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue
                    val flex = item.optJSONArray("flexColumns") ?: continue

                    val title = flex.optJSONObject(0)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                        ?.optJSONArray("runs")
                        ?.optJSONObject(0)
                        ?.optString("text", "Unknown Title") ?: "Unknown Title"

                    val artistRuns = flex.optJSONObject(1)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                        ?.optJSONArray("runs")

                    val artistBuilder = StringBuilder()
                    if (artistRuns != null) {
                        for (j in 0 until artistRuns.length()) {
                            val t = artistRuns.optJSONObject(j)?.optString("text")
                            if (!t.isNullOrBlank() && t != " • ") {
                                if (artistBuilder.isNotEmpty()) artistBuilder.append(", ")
                                artistBuilder.append(t)
                                break
                            }
                        }
                    }
                    val artist = if (artistBuilder.isNotEmpty()) artistBuilder.toString() else "Artis"

                    val videoId = item.optJSONObject("playlistItemData")?.optString("videoId")
                        ?: item.optJSONObject("overlay")
                            ?.optJSONObject("musicItemThumbnailOverlayRenderer")
                            ?.optJSONObject("content")
                            ?.optJSONObject("musicPlayButtonRenderer")
                            ?.optJSONObject("playNavigationEndpoint")
                            ?.optJSONObject("watchEndpoint")
                            ?.optString("videoId") ?: ""

                    val thumbs = item.optJSONObject("thumbnail")
                        ?.optJSONObject("musicThumbnailRenderer")
                        ?.optJSONObject("thumbnail")
                        ?.optJSONArray("thumbnails")

                    val thumbUrl = if (thumbs != null && thumbs.length() > 0) {
                        thumbs.optJSONObject(thumbs.length() - 1)?.optString("url") ?: ""
                    } else ""

                    if (videoId.isNotBlank()) {
                        list.add(
                            Track(
                                id = videoId,
                                title = title,
                                artist = artist,
                                album = "YouTube Music",
                                durationMs = 210000L,
                                coverUrl = thumbUrl,
                                videoId = videoId
                            )
                        )
                    }
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Resolves playable audio stream URL from videoId using Invidious audio formats.
     */
    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        for (instance in INVIDIOUS_INSTANCES) {
            try {
                val url = "$instance/api/v1/videos/$videoId"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)

                    val adaptive = json.optJSONArray("adaptiveFormats")
                    if (adaptive != null) {
                        var bestUrl: String? = null
                        var highestBitrate = 0
                        for (i in 0 until adaptive.length()) {
                            val f = adaptive.optJSONObject(i) ?: continue
                            val type = f.optString("type", "")
                            if (type.startsWith("audio/")) {
                                val bitrate = f.optInt("bitrate", 0)
                                val streamUrl = f.optString("url", "")
                                if (streamUrl.isNotBlank() && bitrate >= highestBitrate) {
                                    highestBitrate = bitrate
                                    bestUrl = streamUrl
                                }
                            }
                        }
                        if (bestUrl != null) return@withContext bestUrl
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Instance $instance failed: ${e.message}")
            }
        }
        null
    }

    /**
     * Fetch real-time synchronized lyrics from LRCLIB open-source API.
     */
    suspend fun fetchLyrics(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        try {
            val encTitle = URLEncoder.encode(title, "UTF-8")
            val encArtist = URLEncoder.encode(artist, "UTF-8")
            val url = "https://lrclib.net/api/get?track_name=$encTitle&artist_name=$encArtist"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "RyFy-Spotless-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val synced = json.optString("syncedLyrics")
                val plain = json.optString("plainLyrics")
                if (synced.isNotBlank()) synced else plain.takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Lyrics fetch error: ${e.message}")
            null
        }
    }
}
