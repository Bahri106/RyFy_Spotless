package com.bahri.ryfy.playback

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EqualizerManager {
    private const val TAG = "EqualizerManager"
    private const val PREFS_NAME = "ryfy_equalizer_prefs"
    private const val KEY_ENABLED = "eq_enabled"
    private const val KEY_PRESET = "eq_preset"
    private const val KEY_BASS = "eq_bass_strength"

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentAudioSessionId: Int = 0

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _bassStrength = MutableStateFlow(0)
    val bassStrength: StateFlow<Int> = _bassStrength.asStateFlow()

    private val _presets = MutableStateFlow<List<String>>(emptyList())
    val presets: StateFlow<List<String>> = _presets.asStateFlow()

    private val _currentPreset = MutableStateFlow<Short>(-1)
    val currentPreset: StateFlow<Short> = _currentPreset.asStateFlow()

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isEnabled.value = prefs.getBoolean(KEY_ENABLED, false)
        _bassStrength.value = prefs.getInt(KEY_BASS, 0)
        _currentPreset.value = prefs.getInt(KEY_PRESET, -1).toShort()
    }

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentAudioSessionId) return
        currentAudioSessionId = audioSessionId

        release()

        try {
            val eq = Equalizer(0, audioSessionId)
            val bb = BassBoost(0, audioSessionId)

            val presetList = mutableListOf<String>()
            val numPresets = eq.numberOfPresets.toInt()
            for (i in 0 until numPresets) {
                presetList.add(eq.getPresetName(i.toShort()))
            }
            _presets.value = presetList

            eq.enabled = _isEnabled.value
            bb.enabled = _isEnabled.value

            if (_currentPreset.value in 0 until numPresets) {
                eq.usePreset(_currentPreset.value)
            }
            if (bb.strengthSupported) {
                bb.setStrength(_bassStrength.value.toShort())
            }

            equalizer = eq
            bassBoost = bb
            Log.d(TAG, "Equalizer attached to audio session $audioSessionId with ${presetList.size} presets")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach equalizer: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        savePrefs()
    }

    fun setPreset(presetIndex: Short) {
        _currentPreset.value = presetIndex
        try {
            equalizer?.usePreset(presetIndex)
        } catch (_: Exception) {}
        savePrefs()
    }

    fun setBassStrength(strength: Int) {
        _bassStrength.value = strength.coerceIn(0, 1000)
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(_bassStrength.value.toShort())
            }
        } catch (_: Exception) {}
        savePrefs()
    }

    private fun savePrefs() {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, _isEnabled.value)
            .putInt(KEY_PRESET, _currentPreset.value.toInt())
            .putInt(KEY_BASS, _bassStrength.value)
            .apply()
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
        } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
    }
}
