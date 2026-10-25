package com.bahri.ryfy.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object SleepTimerManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val _remainingTimeMs = MutableStateFlow<Long?>(null)
    val remainingTimeMs: StateFlow<Long?> = _remainingTimeMs.asStateFlow()

    private val _stopAtEndOfTrack = MutableStateFlow(false)
    val stopAtEndOfTrack: StateFlow<Boolean> = _stopAtEndOfTrack.asStateFlow()

    fun startTimer(durationMinutes: Int) {
        cancelTimer()
        _stopAtEndOfTrack.value = false
        val totalMs = durationMinutes * 60 * 1000L
        _remainingTimeMs.value = totalMs

        timerJob = scope.launch {
            var current = totalMs
            while (isActive && current > 0) {
                delay(1000)
                current -= 1000
                _remainingTimeMs.value = current
            }
            _remainingTimeMs.value = null
            PlayerManager.pause()
        }
    }

    fun setStopAtEndOfTrack() {
        cancelTimer()
        _stopAtEndOfTrack.value = true
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _remainingTimeMs.value = null
        _stopAtEndOfTrack.value = false
    }

    fun onTrackEnded(): Boolean {
        if (_stopAtEndOfTrack.value) {
            cancelTimer()
            PlayerManager.pause()
            return true
        }
        return false
    }
}
