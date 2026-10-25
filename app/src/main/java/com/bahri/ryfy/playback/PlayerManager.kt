package com.bahri.ryfy.playback

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
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

enum class ShuffleMode {
    OFF,
    SHUFFLE,
    SMART_SHUFFLE
}

object PlayerManager {
    private const val TAG = "PlayerManager"
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var playbackJob: Job? = null
    private var simpleCache: SimpleCache? = null

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

        val appContext = context.applicationContext

        // High reliability HTTP Data Source with long timeout & auto-redirects
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) RyFy/1.0.6")
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(35000)
            .setAllowCrossProtocolRedirects(true)

        // Cache Data Source saves chunks locally to survive proxy drops
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(getCache(appContext))
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)

        // Aggressive LoadControl: Buffers entire 3-4 MB track in first few seconds
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                60_000,   // minBufferMs (1 minute)
                300_000,  // maxBufferMs (5 minutes - buffers full song)
                1_500,    // bufferForPlaybackMs
                3_000     // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val player = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) startProgressPolling() else stopProgressPolling()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback error [${error.errorCodeName}]: ${error.message}", error)
                // Auto reconnect on socket drop
                val cur = _currentTrack.value
                val lastPos = _currentPositionMs.value
                if (cur != null && !cur.videoId.isNullOrBlank()) {
                    scope.launch {
                        delay(1200)
                        val retryUrl = MusicApiService.resolveStreamUrl(cur.videoId)
                        if (!retryUrl.isNullOrBlank()) {
                            exoPlayer?.setMediaItem(MediaItem.fromUri(retryUrl), lastPos)
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

        // 3. Manage Queue
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
        } else {
            val existsInQueue = _queue.value.any {
                it.id == track.id || (it.title.equals(track.title, true) && it.artist.equals(track.artist, true))
            }
            if (!existsInQueue) {
                _queue.value = listOf(track)
                // Auto artist radio: fetch more works from the same artist so next tracks are always available
                scope.launch(Dispatchers.IO) {
                    try {
                        val artistTracks = MusicApiService.searchSongs(track.artist)
                            .filter { !it.title.equals(track.title, true) }
                        if (artistTracks.isNotEmpty()) {
                            _queue.value = listOf(track) + artistTracks
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Artist radio queue error: ${e.message}")
                    }
                }
            }
        }

        // 4. Start playback
        val streamUrl = track.streamUrl
        if (!streamUrl.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            _isPlaying.value = true
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
                    val resolvedUrl = MusicApiService.resolveStreamUrl(targetVideoId)
                    val lyrics = MusicApiService.fetchLyrics(track.title, track.artist)
                    val updated = currentTrackInfo.copy(streamUrl = resolvedUrl, lyrics = lyrics)
                    _currentTrack.value = updated

                    if (!resolvedUrl.isNullOrBlank()) {
                        val mediaItem = MediaItem.fromUri(resolvedUrl)
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                        _isPlaying.value = true
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
                // Loop back to start or fetch fresh recommendations
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
