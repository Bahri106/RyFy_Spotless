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

    private val DEDICATED_STREAM_HOST = "https://bahrixyz.my.id/api/stream"
    private val FALLBACK_STREAM_HOST = "https://yt.chocolatemoo53.com/latest_version"

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
     * Search songs via YouTube Music Innertube API.
     * Universal parser supporting musicCardShelfRenderer, musicShelfRenderer, and itemSectionRenderer.
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

                val list = mutableListOf<Track>()

                for (sIdx in 0 until sections.length()) {
                    val section = sections.optJSONObject(sIdx) ?: continue

                    // 1. Check musicCardShelfRenderer (Top Result / Exact artist & track card)
                    val card = section.optJSONObject("musicCardShelfRenderer")
                    if (card != null) {
                        val title = card.optJSONObject("title")
                            ?.optJSONArray("runs")
                            ?.optJSONObject(0)
                            ?.optString("text") ?: ""

                        val subRuns = card.optJSONObject("subtitle")?.optJSONArray("runs")
                        var artist = "Artis"
                        if (subRuns != null && subRuns.length() > 0) {
                            for (r in 0 until subRuns.length()) {
                                val t = subRuns.optJSONObject(r)?.optString("text") ?: ""
                                if (t.isNotBlank() && t != " • " && !t.contains("Lagu") && !t.contains("Artis")) {
                                    artist = t
                                    break
                                }
                            }
                        }

                        var cardVideoId = ""
                        val buttons = card.optJSONArray("buttons")
                        if (buttons != null && buttons.length() > 0) {
                            for (b in 0 until buttons.length()) {
                                val cmd = buttons.optJSONObject(b)
                                    ?.optJSONObject("buttonRenderer")
                                    ?.optJSONObject("command")
                                val vid = cmd?.optJSONObject("watchEndpoint")?.optString("videoId") ?: ""
                                if (vid.isNotBlank()) {
                                    cardVideoId = vid
                                    break
                                }
                            }
                        }
                        if (cardVideoId.isBlank()) {
                            cardVideoId = card.optJSONObject("title")
                                ?.optJSONArray("runs")
                                ?.optJSONObject(0)
                                ?.optJSONObject("navigationEndpoint")
                                ?.optJSONObject("watchEndpoint")
                                ?.optString("videoId") ?: ""
                        }
                        if (cardVideoId.isBlank()) {
                            cardVideoId = card.optJSONObject("onTap")
                                ?.optJSONObject("watchEndpoint")
                                ?.optString("videoId") ?: ""
                        }

                        val cardThumbs = card.optJSONObject("thumbnail")
                            ?.optJSONObject("musicThumbnailRenderer")
                            ?.optJSONObject("thumbnail")
                            ?.optJSONArray("thumbnails")
                        val cardRawThumb = if (cardThumbs != null && cardThumbs.length() > 0) {
                            cardThumbs.optJSONObject(cardThumbs.length() - 1)?.optString("url") ?: ""
                        } else ""

                        if (title.isNotBlank() && cardVideoId.isNotBlank()) {
                            list.add(
                                Track(
                                    id = cardVideoId,
                                    title = title,
                                    artist = artist,
                                    album = "RyFy Spotless",
                                    durationMs = 210000L,
                                    coverUrl = upgradeThumbnail(cardRawThumb),
                                    videoId = cardVideoId
                                )
                            )
                        }
                    }

                    // 2. Check musicShelfRenderer or itemSectionRenderer
                    val items = section.optJSONObject("musicShelfRenderer")?.optJSONArray("contents")
                        ?: section.optJSONObject("itemSectionRenderer")?.optJSONArray("contents")

                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val item = items.optJSONObject(i)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue
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

                            var videoId = item.optJSONObject("playlistItemData")?.optString("videoId") ?: ""
                            if (videoId.isBlank()) {
                                videoId = item.optJSONObject("overlay")
                                    ?.optJSONObject("musicItemThumbnailOverlayRenderer")
                                    ?.optJSONObject("content")
                                    ?.optJSONObject("musicPlayButtonRenderer")
                                    ?.optJSONObject("playNavigationEndpoint")
                                    ?.optJSONObject("watchEndpoint")
                                    ?.optString("videoId") ?: ""
                            }
                            if (videoId.isBlank()) {
                                videoId = flex.optJSONObject(0)
                                    ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                                    ?.optJSONObject("text")
                                    ?.optJSONArray("runs")
                                    ?.optJSONObject(0)
                                    ?.optJSONObject("navigationEndpoint")
                                    ?.optJSONObject("watchEndpoint")
                                    ?.optString("videoId") ?: ""
                            }

                            val thumbs = item.optJSONObject("thumbnail")
                                ?.optJSONObject("musicThumbnailRenderer")
                                ?.optJSONObject("thumbnail")
                                ?.optJSONArray("thumbnails")

                            val rawThumb = if (thumbs != null && thumbs.length() > 0) {
                                thumbs.optJSONObject(thumbs.length() - 1)?.optString("url") ?: ""
                            } else ""
                            val thumbUrl = upgradeThumbnail(rawThumb)

                            if (videoId.isNotBlank() && list.none { it.id == videoId }) {
                                list.add(
                                    Track(
                                        id = videoId,
                                        title = title,
                                        artist = artist,
                                        album = "RyFy Spotless",
                                        durationMs = 210000L,
                                        coverUrl = thumbUrl,
                                        videoId = videoId
                                    )
                                )
                            }
                        }
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
     * Resolves playable audio stream URL instantly via dedicated high-speed server.
     * Direct 1 Gbps pipe, zero captcha, byte-range 206 supported.
     */
    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext null
        "$DEDICATED_STREAM_HOST?id=$videoId"
    }

    /**
     * Fallback stream resolver if the primary dedicated host is unreachable.
     */
    suspend fun resolveFallbackStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext null
        "$FALLBACK_STREAM_HOST?id=$videoId&itag=140&local=true"
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
