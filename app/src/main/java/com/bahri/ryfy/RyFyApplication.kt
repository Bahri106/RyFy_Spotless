package com.bahri.ryfy

import android.app.Application
import com.bahri.ryfy.core.AppIconManager
import com.bahri.ryfy.core.AppLanguageManager
import com.bahri.ryfy.core.CrashHandler
import com.bahri.ryfy.data.FavoritesManager
import com.bahri.ryfy.data.OfflineTrackManager
import com.bahri.ryfy.data.PlaylistManager
import com.bahri.ryfy.data.RecentlyPlayedManager
import com.bahri.ryfy.playback.EqualizerManager
import com.bahri.ryfy.playback.PlayerManager

class RyFyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 1. Crash handler installed first to catch any unexpected issues
        CrashHandler.install(this)

        // 2. Defensive initialization with runCatching to ensure 100% startup reliability
        runCatching { AppLanguageManager.init(this) }
        runCatching { AppIconManager.init(this) }
        runCatching { FavoritesManager.init(this) }
        runCatching { PlaylistManager.init(this) }
        runCatching { RecentlyPlayedManager.init(this) }
        runCatching { OfflineTrackManager.init(this) }
        runCatching { EqualizerManager.init(this) }
        runCatching { PlayerManager.init(this) }
    }
}
