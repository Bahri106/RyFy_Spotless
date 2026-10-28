package com.bahri.ryfy.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlayerStyle(val displayName: String, val shortName: String) {
    IOS_GLASS("Kartu Kaca iOS (Apple Style)", "iOS"),
    SPOTIFY_CLASSIC("Spotify Classic Fullscreen", "Spotify")
}

object PlayerStyleManager {
    private const val PREFS = "ryfy_player_style"
    private const val KEY_STYLE = "player_style"

    private val _currentStyle = MutableStateFlow(PlayerStyle.IOS_GLASS)
    val currentStyle: StateFlow<PlayerStyle> = _currentStyle.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_STYLE, PlayerStyle.IOS_GLASS.name)
        _currentStyle.value = try {
            PlayerStyle.valueOf(saved ?: PlayerStyle.IOS_GLASS.name)
        } catch (_: Exception) {
            PlayerStyle.IOS_GLASS
        }
    }

    fun setStyle(context: Context, style: PlayerStyle) {
        _currentStyle.value = style
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STYLE, style.name)
            .apply()
    }

    fun toggleStyle(context: Context): PlayerStyle {
        val next = if (_currentStyle.value == PlayerStyle.IOS_GLASS) {
            PlayerStyle.SPOTIFY_CLASSIC
        } else {
            PlayerStyle.IOS_GLASS
        }
        setStyle(context, next)
        return next
    }
}
