package com.bahri.ryfy.playback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.bahri.ryfy.data.api.MusicApiService
import com.bahri.ryfy.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Collections

enum class ShuffleMode {
    OFF,
    SHUFFLE,
    SMART_SHUFFLE
}

object PlayerManager {
    private const val TAG = "PlayerManager"
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var playbackJob: Job? = null
    private var prefetchJob: Job? = null
    private var simpleCache: SimpleCache? = null

    // In-memory instant stream URL cache: videoId -> direct playable URL
    private val streamUrlCache = Collections.synchronizedMap(LinkedHashMap<String, String>(50, 0.75f, true))

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    fun getSession(): MediaSession? = mediaSession

    fun preloadTrack(videoId: String) {
        if (videoId.isBlank() || streamUrlCache.containsKey(videoId)) return
        scope.launch(Dispatchers.IO) {
            runCatching {
                val url = MusicApiService.resolveStreamUrl(videoId)
                if (!url.isNullOrBlank()) {
                    streamUrlCache[videoId] = url
                }
            }
        }
    }

    @Synchronized
    private fun getCache(context: Context): SimpleCache {
        return simpleCache ?: run {
            val cacheFolder = File(context.applicationContext.cacheDir, "media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(250 * 1024 * 1024) // 250 MB
            val dbProvider = StandaloneDatabaseProvider(context.applicationContext)
            SimpleCache(cacheFolder, evictor, dbProvider).also { simpleCache = it }
        }
    }

    fun init(context: Context) {
        if (exoPlayer != null) return

        val app = context.applicationContext
        appContext = app

        // High reliability HTTP Data Source with long timeout & auto-redirects
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) RyFy/1.0.9")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(30000)
            .setAllowCrossProtocolRedirects(true)

        // Cache Data Source saves chunks locally to survive proxy drops
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(getCache(app))
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)

        // Ultra-low latency playback start (250ms buffer) + aggressive background buffer (5 mins)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000,   // minBufferMs (15 seconds)
                300_000,  // maxBufferMs (5 minutes)
                250,      // bufferForPlaybackMs: Instant start (< 250ms)!
                500       // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(app)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startProgressPolling()
                } else {
                    stopProgressPolling()
                }
                updateSystemNotification()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val realDuration = player.duration
                    if (realDuration > 0 && _currentTrack.value != null && _currentTrack.value?.durationMs != realDuration) {
                        _currentTrack.value = _currentTrack.value?.copy(durationMs = realDuration)
                    }
                } else if (state == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback error [${error.errorCodeName}]: ${error.message}", error)
                // Auto reconnect with fallback mirror
                val cur = _currentTrack.value
                val lastPos = _currentPositionMs.value
                if (cur != null && !cur.videoId.isNullOrBlank()) {
                    scope.launch {
                        delay(500)
                        val retryUrl = MusicApiService.resolveFallbackStreamUrl(cur.videoId)
                        if (!retryUrl.isNullOrBlank()) {
                            streamUrlCache[cur.videoId] = retryUrl
                            val mediaItem = buildMediaItem(cur, retryUrl)
                            exoPlayer?.setMediaItem(mediaItem, lastPos)
                            exoPlayer?.prepare()
                            exoPlayer?.play()
                        } else {
                            playNext()
                        }
                    }
                }
            }
        })

        exoPlayer = player
        runCatching {
            mediaSession = MediaSession.Builder(app, player).build()
        }.onFailure {
            Log.w(TAG, "MediaSession initialization skipped: ${it.message}")
        }
        RyFyNotificationManager.init(app)
    }

    fun updateSystemNotification() {
        val track = _currentTrack.value ?: return
        val playing = _isPlaying.value
        appContext?.let { ctx ->
            RyFyNotificationManager.update(ctx, track, playing)
        }
    }

    private fun buildMediaItem(track: Track, url: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album.ifBlank { "RyFy Spotless" })
            .setArtworkUri(if (track.coverUrl.isNotBlank()) Uri.parse(track.coverUrl) else null)
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        val player = exoPlayer ?: return

        // 1. Cancel previous pending stream resolution immediately
        playbackJob?.cancel()

        // 2. Immediately stop previous audio & reset position
        player.stop()
        player.clearMediaItems()
        _currentPositionMs.value = 0L
        _isPlaying.value = false
        _currentTrack.value = track
        com.bahri.ryfy.data.RecentlyPlayedManager.addTrack(track)
        fetchLyricsForTrack(track)

        // 3. Manage Queue (Strict Genre / Artist Isolation)
        if (newQueue != null && newQueue.size > 1) {
            _queue.value = newQueue
        } else {
            // Unrelated queue wiped out, replaced by dedicated Artist Radio queue
            _queue.value = listOf(track)
            scope.launch(Dispatchers.IO) {
                try {
                    val query = if (track.artist.isNotBlank() && track.artist != "Artis") {
                        track.artist
                    } else {
                        track.title
                    }
                    val related = MusicApiService.searchSongs(query)
                        .filter { !it.title.equals(track.title, true) }
                    if (related.isNotEmpty()) {
                        _queue.value = listOf(track) + related
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Artist radio queue error: ${e.message}")
                }
            }
        }

        // 4. Check in-memory stream cache for instant start (0 ms latency)
        val cachedUrl = track.streamUrl ?: track.videoId?.let { streamUrlCache[it] }

        if (!cachedUrl.isNullOrBlank()) {
            val mediaItem = buildMediaItem(track, cachedUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            _isPlaying.value = true
            updateSystemNotification()
            prefetchNextTrack()
        } else {
            playbackJob = scope.launch {
                var targetVideoId = track.videoId
                var currentTrackInfo = track

                if (targetVideoId.isNullOrBlank()) {
                    val searchResults = MusicApiService.searchSongs("${track.title} ${track.artist}")
                    val match = searchResults.firstOrNull()
                    if (match != null && !match.videoId.isNullOrBlank()) {
                        targetVideoId = match.videoId
                        currentTrackInfo = track.copy(
                            id = match.videoId,
                            videoId = match.videoId,
                            coverUrl = if (track.coverUrl.isBlank()) match.coverUrl else track.coverUrl,
                            durationMs = if (track.durationMs == 0L) match.durationMs else track.durationMs
                        )
                    }
                }

                if (!targetVideoId.isNullOrBlank()) {
                    val resolvedUrl = streamUrlCache[targetVideoId]
                        ?: MusicApiService.resolveStreamUrl(targetVideoId)

                    if (!resolvedUrl.isNullOrBlank()) {
                        streamUrlCache[targetVideoId] = resolvedUrl
                    }

                    val updated = currentTrackInfo.copy(streamUrl = resolvedUrl)
                    _currentTrack.value = updated

                    if (!resolvedUrl.isNullOrBlank()) {
                        val mediaItem = buildMediaItem(updated, resolvedUrl)
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                        _isPlaying.value = true
                        updateSystemNotification()
                        prefetchNextTrack()
                    } else {
                        Log.w(TAG, "Failed to resolve stream for videoId $targetVideoId, auto-skipping to next")
                        playNext()
                    }
                } else {
                    Log.w(TAG, "No videoId found for track ${track.title}, auto-skipping to next")
                    playNext()
                }
            }
        }
    }

    private fun fetchLyricsForTrack(track: Track) {
        scope.launch(Dispatchers.IO) {
            try {
                val lyrics = MusicApiService.fetchLyrics(track.title, track.artist)
                if (!lyrics.isNullOrBlank() && _currentTrack.value?.title.equals(track.title, true)) {
                    _currentTrack.value = _currentTrack.value?.copy(lyrics = lyrics)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Lyrics fetch error: ${e.message}")
            }
        }
    }

    /**
     * Prefetch next track's audio stream in background so when user presses Next, playback is instant.
     */
    private fun prefetchNextTrack() {
        prefetchJob?.cancel()
        prefetchJob = scope.launch(Dispatchers.IO) {
            delay(4000) // Wait until current track is stably buffering
            val cur = _currentTrack.value ?: return@launch
            val list = _queue.value
            val curIdx = list.indexOfFirst { it.id == cur.id || (it.title.equals(cur.title, true) && it.artist.equals(cur.artist, true)) }
            val next = if (curIdx != -1 && curIdx + 1 < list.size) list[curIdx + 1] else null
            if (next != null) {
                var vid = next.videoId
                if (vid.isNullOrBlank()) {
                    val s = MusicApiService.searchSongs("${next.title} ${next.artist}")
                    vid = s.firstOrNull()?.videoId
                }
                if (!vid.isNullOrBlank() && !streamUrlCache.containsKey(vid)) {
                    val stream = MusicApiService.resolveStreamUrl(vid)
                    if (!stream.isNullOrBlank()) {
                        streamUrlCache[vid] = stream
                        Log.d(TAG, "Prefetched next track audio stream: ${next.title}")
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (_currentTrack.value != null) {
                player.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun playNext() {
        val currentList = _queue.value
        val current = _currentTrack.value ?: return
        if (currentList.isEmpty()) return

        if (_shuffleMode.value != ShuffleMode.OFF && currentList.size > 1) {
            val remaining = currentList.filter {
                it.id != current.id && !(it.title.equals(current.title, true) && it.artist.equals(current.artist, true))
            }
            val nextTrack = remaining.randomOrNull() ?: currentList.first()
            playTrack(nextTrack, currentList)
        } else {
            val currentIndex = currentList.indexOfFirst {
                it.id == current.id || (it.title.equals(current.title, true) && it.artist.equals(current.artist, true))
            }
            if (currentIndex != -1 && currentIndex + 1 < currentList.size) {
                playTrack(currentList[currentIndex + 1], currentList)
            } else if (currentList.isNotEmpty()) {
                playTrack(currentList.first(), currentList)
            }
        }
    }

    fun playPrevious() {
        val currentList = _queue.value
        val current = _currentTrack.value ?: return
        if (currentList.isEmpty()) {
            seekTo(0)
            return
        }

        val currentIndex = currentList.indexOfFirst {
            it.id == current.id || (it.title.equals(current.title, true) && it.artist.equals(current.artist, true))
        }
        if (currentIndex > 0) {
            playTrack(currentList[currentIndex - 1], currentList)
        } else {
            seekTo(0)
        }
    }

    fun removeFromQueue(trackId: String) {
        val cur = _currentTrack.value
        if (cur?.id == trackId) return // Don't remove currently playing track
        val updated = _queue.value.filter { it.id != trackId }
        _queue.value = updated
    }

    fun clearQueueExceptCurrent() {
        val cur = _currentTrack.value ?: return
        _queue.value = listOf(cur)
    }

    fun cycleShuffleMode() {
        val next = when (_shuffleMode.value) {
            ShuffleMode.OFF -> ShuffleMode.SHUFFLE
            ShuffleMode.SHUFFLE -> ShuffleMode.SMART_SHUFFLE
            ShuffleMode.SMART_SHUFFLE -> ShuffleMode.OFF
        }
        _shuffleMode.value = next

        if (next == ShuffleMode.SMART_SHUFFLE) {
            val cur = _currentTrack.value ?: return
            scope.launch(Dispatchers.IO) {
                try {
                    val smartTracks = MusicApiService.searchSongs("${cur.artist} rekomendasi")
                    if (smartTracks.isNotEmpty()) {
                        val currentList = _queue.value.toMutableList()
                        smartTracks.forEach { t ->
                            if (currentList.none { it.id == t.id || (it.title == t.title && it.artist == t.artist) }) {
                                currentList.add(t)
                            }
                        }
                        _queue.value = currentList
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleRepeat() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        exoPlayer?.repeatMode = nextMode
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _currentPositionMs.value = player.currentPosition
                    val realDuration = player.duration
                    if (realDuration > 0 && _currentTrack.value != null && _currentTrack.value?.durationMs != realDuration) {
                        _currentTrack.value = _currentTrack.value?.copy(durationMs = realDuration)
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }
}
