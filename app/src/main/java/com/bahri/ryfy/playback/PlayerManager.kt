package com.bahri.ryfy.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    fun init(context: Context) {
        if (exoPlayer != null) return
        val player = ExoPlayer.Builder(context.applicationContext).build()
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

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("PlayerManager", "Playback error: ${error.errorCodeName} - ${error.message}", error)
            }
        })
        exoPlayer = player
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        val player = exoPlayer ?: return
        if (newQueue != null) {
            _queue.value = newQueue
        }
        _currentTrack.value = track

        val streamUrl = track.streamUrl
        if (!streamUrl.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } else if (!track.videoId.isNullOrBlank()) {
            // Asynchronously resolve real audio stream & lyrics
            scope.launch {
                val resolvedUrl = com.bahri.ryfy.data.api.MusicApiService.resolveStreamUrl(track.videoId)
                val lyrics = com.bahri.ryfy.data.api.MusicApiService.fetchLyrics(track.title, track.artist)
                val updated = track.copy(streamUrl = resolvedUrl, lyrics = lyrics)
                _currentTrack.value = updated

                if (!resolvedUrl.isNullOrBlank()) {
                    val mediaItem = MediaItem.fromUri(resolvedUrl)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                } else {
                    player.stop()
                    _isPlaying.value = true
                    startProgressPolling()
                }
            }
        } else {
            // Virtual mock simulation when no direct URL
            player.stop()
            _isPlaying.value = true
            startProgressPolling()
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
        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex + 1 < currentList.size) {
            playTrack(currentList[currentIndex + 1])
        }
    }

    fun playPrevious() {
        val currentList = _queue.value
        val current = _currentTrack.value ?: return
        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playTrack(currentList[currentIndex - 1])
        } else {
            seekTo(0)
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
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
