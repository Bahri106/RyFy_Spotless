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

    fun upgradeThumbnail(url: String): String {
        if (url.isBlank()) return url
        return url
            .replace(Regex("=w\\d+-h\\d+[^\"'\\s]*"), "=w1080-h1080-l90-rj")
            .replace(Regex("=s\\d+[^\"'\\s]*"), "=s1080")
            .replace("/hqdefault.jpg", "/maxresdefault.jpg")
            .replace("/mqdefault.jpg", "/maxresdefault.jpg")
            .replace("/default.jpg", "/maxresdefault.jpg")
    }

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

                    val rawThumb = if (thumbs != null && thumbs.length() > 0) {
                        thumbs.optJSONObject(thumbs.length() - 1)?.optString("url") ?: ""
                    } else ""
                    val thumbUrl = upgradeThumbnail(rawThumb)

                    val album = "RyFy Spotless"

                    if (videoId.isNotBlank()) {
                        list.add(
                            Track(
                                id = videoId,
                                title = title,
                                artist = artist,
                                album = album,
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
     * Uses local=true proxy mode so streams bypass Google 403 Forbidden IP mismatch.
     */
    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        for (instance in INVIDIOUS_INSTANCES) {
            // Strategy 1: Direct proxied latest_version endpoint
            val directProxy = "$instance/latest_version?id=$videoId&itag=140&local=true"
            try {
                val headReq = Request.Builder()
                    .url(directProxy)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) RyFy/1.0.2")
                    .header("Range", "bytes=0-1024")
                    .build()
                client.newCall(headReq).execute().use { resp ->
                    if (resp.isSuccessful || resp.code == 200 || resp.code == 206) {
                        return@withContext directProxy
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct proxy check failed on $instance: ${e.message}")
            }

            // Strategy 2: API query with ?local=true for proxied adaptiveFormats
            try {
                val url = "$instance/api/v1/videos/$videoId?local=true"
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
                                var streamUrl = f.optString("url", "")
                                if (streamUrl.isNotBlank()) {
                                    if (!streamUrl.startsWith("http")) {
                                        streamUrl = "$instance$streamUrl"
                                    }
                                    if (bitrate >= highestBitrate) {
                                        highestBitrate = bitrate
                                        bestUrl = streamUrl
                                    }
                                }
                            }
                        }
                        if (bestUrl != null) return@withContext bestUrl
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Instance $instance API query failed: ${e.message}")
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
