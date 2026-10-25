package com.bahri.ryfy.core

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.bahri.ryfy.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLauncherIcon(val alias: String, val label: String, val preview: Int) {
    NEON("LauncherNeon", "RyFy Neon", R.mipmap.ic_launcher),
    SPOTIFY("LauncherSpotify", "Spotify Classic", R.mipmap.ic_launcher_spotify),
    MONO("LauncherMono", "RyFy Mono", R.mipmap.ic_launcher_mono)
}

object AppIconManager {
    private const val PREFS = "ryfy_theme"
    private const val KEY_ICON = "launcher_icon"

    private val _icon = MutableStateFlow(AppLauncherIcon.NEON)
    val icon: StateFlow<AppLauncherIcon> = _icon.asStateFlow()

    fun init(context: Context) {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ICON, null)
        _icon.value = saved?.let { runCatching { AppLauncherIcon.valueOf(it) }.getOrNull() }
            ?: enabledIcon(context) ?: AppLauncherIcon.NEON
    }

    fun setIcon(context: Context, icon: AppLauncherIcon) {
        if (icon == _icon.value && enabledIcon(context) == icon) return
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            component(context, icon),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        AppLauncherIcon.entries.filter { it != icon }.forEach {
            pm.setComponentEnabledSetting(
                component(context, it),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ICON, icon.name).apply()
        _icon.value = icon
    }

    private fun component(context: Context, icon: AppLauncherIcon) =
        ComponentName(context.packageName, "com.bahri.ryfy.${icon.alias}")

    private fun enabledIcon(context: Context): AppLauncherIcon? {
        val pm = context.packageManager
        AppLauncherIcon.entries.firstOrNull {
            pm.getComponentEnabledSetting(component(context, it)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }?.let { return it }
        val defaultState = pm.getComponentEnabledSetting(component(context, AppLauncherIcon.NEON))
        return if (defaultState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) AppLauncherIcon.NEON else null
    }
}
