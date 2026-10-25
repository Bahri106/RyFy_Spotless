package com.bahri.ryfy.core

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.bahri.ryfy.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLauncherIcon(val alias: String, val label: String, val preview: Int) {
    NEON("MainActivity", "RyFy Neon", R.mipmap.ic_launcher),
    SPOTIFY("LauncherSpotify", "Spotify Classic", R.mipmap.ic_launcher_spotify),
    MONO("LauncherMono", "RyFy Mono", R.mipmap.ic_launcher_mono)
}

object AppIconManager {
    private const val TAG = "AppIconManager"
    private const val PREFS = "ryfy_theme"
    private const val KEY_ICON = "launcher_icon"

    private val _icon = MutableStateFlow(AppLauncherIcon.NEON)
    val icon: StateFlow<AppLauncherIcon> = _icon.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_ICON, null)
        val targetIcon = saved?.let { runCatching { AppLauncherIcon.valueOf(it) }.getOrNull() }
            ?: AppLauncherIcon.NEON

        _icon.value = targetIcon
        enforceSingleIcon(context, targetIcon)
    }

    fun setIcon(context: Context, icon: AppLauncherIcon) {
        if (icon == _icon.value && enabledIcon(context) == icon) return
        enforceSingleIcon(context, icon)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ICON, icon.name).apply()
        _icon.value = icon
    }

    private fun enforceSingleIcon(context: Context, activeIcon: AppLauncherIcon) {
        val pm = context.packageManager
        try {
            // 1. Enable the targeted icon
            pm.setComponentEnabledSetting(
                component(context, activeIcon.alias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Strictly disable all other launcher icons to prevent duplicate icons on update
            AppLauncherIcon.entries.filter { it != activeIcon }.forEach { other ->
                pm.setComponentEnabledSetting(
                    component(context, other.alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

            // 3. Clean up legacy alias if left over from older versions (e.g. LauncherNeon)
            runCatching {
                pm.setComponentEnabledSetting(
                    component(context, "LauncherNeon"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            Log.d(TAG, "Enforced single launcher icon: ${activeIcon.name}")
        } catch (e: Exception) {
            Log.w(TAG, "Error enforcing single launcher icon: ${e.message}")
        }
    }

    private fun component(context: Context, alias: String) =
        ComponentName(context.packageName, "com.bahri.ryfy.$alias")

    private fun enabledIcon(context: Context): AppLauncherIcon? {
        val pm = context.packageManager
        AppLauncherIcon.entries.firstOrNull {
            pm.getComponentEnabledSetting(component(context, it.alias)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }?.let { return it }
        val defaultState = pm.getComponentEnabledSetting(component(context, AppLauncherIcon.NEON.alias))
        return if (defaultState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) AppLauncherIcon.NEON else null
    }
}
