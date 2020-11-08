package com.gaurav712.gigsmusic

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MusicPlayerService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        playerNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
//        playerNotification.setContentTitle("Nothing").
//        setSmallIcon(R.drawable.ic_launcher_icon).
//        setContentText("Lorem ipsum dolor").
//        priority = NotificationCompat.PRIORITY_DEFAULT

//        startForeground(NOTIFICATION_REQUEST_CODE, playerNotification.build())

        return START_STICKY
    }

    fun startNotification(context: Context, intent: Intent, title: String) {

        val pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        playerNotification = NotificationCompat.Builder(context, CHANNEL_ID)
        playerNotification.setContentTitle(title).
        setContentIntent(pendingIntent).
        setSmallIcon(R.drawable.ic_launcher_icon).
        setContentText("Lorem ipsum dolor").
        priority = NotificationCompat.PRIORITY_DEFAULT

//        startForeground(NOTIFICATION_REQUEST_CODE, playerNotification.build())
        context.startService(intent)
    }

    fun stopNotification() {
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
    }

    fun reset() {
        mediaPlayer.reset()
    }

    fun loadMedia(context: Context, mediaUri: Uri) {
        mediaPlayer.setDataSource(context, mediaUri)
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
        const val NOTIFICATION_REQUEST_CODE = 712
    }
}