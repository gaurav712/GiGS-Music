package com.gaurav712.gigsmusic

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MusicPlayerService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "my_notification",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(notificationChannel)
        }
        playerNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        playerNotification.setContentTitle("Nothing").
        setSmallIcon(R.drawable.launcher_icon).
        setContentText("Lorem ipsum dolor").
        priority = NotificationCompat.PRIORITY_DEFAULT

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
    }
}