package com.gaurav712.gigsmusic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver

class MusicPlayerService: Service() {

    class MusicControlsReceiver: MediaButtonReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null)
                handleIntent(mediaSession, intent)
        }
    }

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

        val mediaButtonReceiver = ComponentName(applicationContext, MusicPlayerService::class.java)

        // receiver
        registerReceiver(mediaControls, IntentFilter(Intent.ACTION_MEDIA_BUTTON))

        // media metadata
        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, MainActivity.currentMusicTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MainActivity.currentMusicArtist)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, MainActivity.currentMusicAlbumArt)
            .build()

        // callback
        val mediaSessionCompatCallback = object: MediaSessionCompat.Callback() {

            override fun onPlay() {
                Log.i("musicCallback", "onPlay")
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }

            override fun onPause() {
                Log.i("musicCallback", "onPause")
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
        }

        // media session
        mediaSession = MediaSessionCompat(
            applicationContext,
            MEDIA_SESSION_TAG,
            mediaButtonReceiver,
            null
        )

        mediaSession.setCallback(mediaSessionCompatCallback)
        mediaSession.setMetadata(mediaMetadata)
        mediaSession.isActive = true

        // update playback state
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)

        playerNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        playerNotification
            .addAction(R.drawable.pause_vector, "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
            )
//                .setShowActionsInCompactView(0, 1, 2))
            .setSmallIcon(R.drawable.launcher_icon)
            .setContentTitle(MainActivity.currentMusicTitle)  // title
            .setContentText(MainActivity.currentMusicArtist)    // artist
            .setLargeIcon(MainActivity.currentMusicAlbumArt)    // album art
            .build()

        startForeground(2, playerNotification.build())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mediaControls)
        mediaSession.release()
        stopForeground(true)
        stopSelf()
    }

    private fun updatePlaybackState(playbackState: Int) {
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(playbackState, 0, 1.0f)
            .build()
        )
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
        lateinit var mediaSession: MediaSessionCompat
        val mediaControls = MediaButtonReceiver()

        const val CHANNEL_ID = "com.gaurav712.gigsmusic.CHANNEL_ID"
        const val MEDIA_SESSION_TAG = "com.gaurav712.gigsmusic.MEDIA_SESSION"
    }
}