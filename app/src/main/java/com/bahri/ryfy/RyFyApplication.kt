package com.bahri.ryfy

import android.app.Application
import com.bahri.ryfy.playback.PlayerManager

class RyFyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlayerManager.init(this)
    }
}
