package com.bahri.ryfy.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PlaybackActionReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "PlaybackActionReceiver"
        const val ACTION_PLAY_PAUSE = "com.bahri.ryfy.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.bahri.ryfy.ACTION_NEXT"
        const val ACTION_PREV = "com.bahri.ryfy.ACTION_PREV"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received playback action: $action")
        when (action) {
            ACTION_PLAY_PAUSE -> PlayerManager.togglePlayPause()
            ACTION_NEXT -> PlayerManager.playNext()
            ACTION_PREV -> PlayerManager.playPrevious()
        }
    }
}
