package com.bahri.ryfy.playback

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaPlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return PlayerManager.getSession()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
