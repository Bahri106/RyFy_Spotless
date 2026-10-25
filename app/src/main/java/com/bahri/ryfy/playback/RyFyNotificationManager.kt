package com.bahri.ryfy.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.bahri.ryfy.MainActivity
import com.bahri.ryfy.R
import com.bahri.ryfy.model.Track

object RyFyNotificationManager {

    private const val TAG = "RyFyNotifManager"
    const val CHANNEL_ID = "ryfy_media_playback"
    const val NOTIFICATION_ID = 1010

    private var notificationManager: NotificationManager? = null
    private var isChannelCreated = false

    fun init(context: Context) {
        if (isChannelCreated) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager = nm

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemutar Musik RyFy",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kontrol pemutar musik RyFy di layar kunci, status bar, dan notifikasi"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
            isChannelCreated = true
            Log.d(TAG, "NotificationChannel created")
        }
    }

    fun update(context: Context, track: Track, isPlaying: Boolean) {
        init(context)
        val nm = notificationManager ?: (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager) ?: return

        try {
            // Intent to open MainActivity when user taps the notification card
            val contentIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent for Prev Button
            val prevIntent = PendingIntent.getBroadcast(
                context, 1,
                Intent(PlaybackActionReceiver.ACTION_PREV).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent for Play/Pause Button
            val playPauseIntent = PendingIntent.getBroadcast(
                context, 2,
                Intent(PlaybackActionReceiver.ACTION_PLAY_PAUSE).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent for Next Button
            val nextIntent = PendingIntent.getBroadcast(
                context, 3,
                Intent(PlaybackActionReceiver.ACTION_NEXT).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val playPauseIcon = if (isPlaying) R.drawable.ic_spotify_pause else R.drawable.ic_spotify_play
            val playPauseTitle = if (isPlaying) "Jeda" else "Putar"

            // Official Android MediaStyle configuration (interoperable with Media3)
            val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)

            val compatToken = PlayerManager.getSession()?.sessionCompatToken
            if (compatToken != null) {
                mediaStyle.setMediaSession(compatToken)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(track.title)
                .setContentText(track.artist)
                .setSubText(track.album.ifBlank { "RyFy Spotless" })
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(isPlaying)
                .setSilent(true)
                .setStyle(mediaStyle)
                .addAction(R.drawable.ic_spotify_skip_prev, "Sebelumnya", prevIntent)
                .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
                .addAction(R.drawable.ic_spotify_skip_next, "Berikutnya", nextIntent)

            // 1. Post initial notification immediately (< 10ms response)
            nm.notify(NOTIFICATION_ID, builder.build())

            // 2. Fetch high-res album artwork asynchronously and update notification large icon
            if (track.coverUrl.isNotBlank()) {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(track.coverUrl)
                    .allowHardware(false) // Software bitmap required for Notification RemoteViews
                    .target(
                        onSuccess = { drawable ->
                            val bitmap = (drawable as? BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                builder.setLargeIcon(bitmap)
                                nm.notify(NOTIFICATION_ID, builder.build())
                            }
                        }
                    )
                    .build()
                imageLoader.enqueue(request)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post media notification: ${e.message}", e)
        }
    }

    fun dismiss(context: Context) {
        val nm = notificationManager ?: (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
        nm?.cancel(NOTIFICATION_ID)
    }
}
