package com.bahri.ryfy

import android.app.Application
import com.bahri.ryfy.core.AppIconManager
import com.bahri.ryfy.playback.PlayerManager

class RyFyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppIconManager.init(this)
        PlayerManager.init(this)
    }
}
