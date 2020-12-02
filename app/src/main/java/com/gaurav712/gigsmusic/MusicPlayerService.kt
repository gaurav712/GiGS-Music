package com.gaurav712.gigsmusic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicPlayerService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Create notification channel for android versions 8 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "my_notification",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(notificationChannel)
        }

        val mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG)

        playerNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
//        playerNotification.setContentTitle("Nothing").
//        setSmallIcon(R.drawable.launcher_icon).
//        setContentText("Lorem ipsum dolor").
//        priority = NotificationCompat.PRIORITY_DEFAULT
        playerNotification
            .setSmallIcon(R.drawable.launcher_icon)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken))
            .setContentTitle(MainActivity.currentMusicTitle)  // title
            .setContentText(MainActivity.currentMusicArtist)    // artist
            .setLargeIcon(MainActivity.currentMusicAlbumArt)    // album art
            .build()

        startForeground(2, playerNotification.build())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopSelf()
    }

    fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun release() {
        if (mediaPlayer.isPlaying)
            mediaPlayer.stop()
        mediaPlayer.release()

        // Stop the service
        stopForeground(true)
        stopSelf()
    }

    fun reset() {
        mediaPlayer.reset()
    }

    fun loadMedia(context: Context, mediaUri: Uri) {
        mediaPlayer.setDataSource(context, mediaUri)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().
            setContentType(CONTENT_TYPE_MUSIC).
            build()
        )   // to set the stream to music
        mediaPlayer.prepare()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun getCurrentPosition(): Int {
        return (mediaPlayer.currentPosition / 1000) // to convert it to seconds
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position * 1000)  // to convert it back to ms
    }

    companion object {
        val mediaPlayer: MediaPlayer = MediaPlayer()
        lateinit var playerNotification: NotificationCompat.Builder

        const val CHANNEL_ID = "default_channel_id"
        const val MEDIA_SESSION_TAG = "media_session_tag"
    }
}