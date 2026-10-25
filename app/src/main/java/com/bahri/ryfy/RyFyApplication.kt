package com.bahri.ryfy

import android.app.Application
import com.bahri.ryfy.core.AppIconManager
import com.bahri.ryfy.data.FavoritesManager
import com.bahri.ryfy.playback.PlayerManager

class RyFyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppIconManager.init(this)
        FavoritesManager.init(this)
        PlaylistManager.init(this)
        PlayerManager.init(this)
    }
}
