package com.gaurav712.gigsmusic

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.parseColor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.lang.Integer.parseInt
import java.util.*
import kotlin.random.Random.Default.nextInt

class MainActivity : AppCompatActivity() {

    private lateinit var albumArtImage: ImageView
    private lateinit var musicTitleTextBox: TextView
    private lateinit var musicArtistTextBox: TextView
    private lateinit var durationSeekBar: SeekBar
    private lateinit var currentSeekBarProgress: TextView
    private lateinit var maxSeekBarProgress: TextView
    private lateinit var currentMusicNumberTextView: TextView
    private lateinit var musicPlayerService: MusicPlayerService
    private lateinit var bounceAnimationInterpolator: BounceAnimationInterpolator

    override fun onCreate(savedInstanceState: Bundle?) {

        // To change the activity enter and exit animation
        overridePendingTransition(R.anim.activity_expand_and_fade_in,
            android.R.anim.fade_out)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bounceAnimationInterpolator = BounceAnimationInterpolator(0.2, 20.0)

        albumArtImage = findViewById(R.id.albumArtImage)
        musicTitleTextBox = findViewById(R.id.musicTitleTextBox)
        musicArtistTextBox = findViewById(R.id.musicArtistTextBox)
        durationSeekBar = findViewById(R.id.durationSeekBar)
        currentSeekBarProgress = findViewById(R.id.currentSeekBarProgress)
        maxSeekBarProgress = findViewById(R.id.maxSeekBarProgress)
        currentMusicNumberTextView = findViewById(R.id.currentMusicNumberTextView)

        loadPlayer()
    }

    override fun onStop() {
        // To change the activity enter and exit animation
        overridePendingTransition(R.anim.activity_expand_and_fade_in,
            android.R.anim.fade_out)
        super.onStop()
    }

    override fun onPause() {
        // To change the activity enter and exit animation
        overridePendingTransition(R.anim.activity_expand_and_fade_in,
            android.R.anim.fade_out)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            musicPlayerService.release()
        } catch (ex: Exception) {
            Log.e("stacktrace", ex.message.toString())
        }
    }

    private fun loadPlayer() {

        changeMusic(0)  // start the initial song, hence 0

        // To update the seek-bar with music position
        Timer().scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                try {
                    if (musicPlayerService.isPlaying()) {
                        val currentPosition = musicPlayerService.getCurrentPosition()
                        updateSeekBar(currentPosition)
                    }
                } catch (ex: Exception) {
                    Log.e("stacktrace", ex.message.toString())
                }
            }
        }, 0, 1000)

        durationSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    if (fromUser) {
                        musicPlayerService.seekTo(progress)
                    }
                } catch (ex: Exception) {
                    Log.e("updateSeekBar", ex.message.toString())
                }
                currentSeekBarProgress.text = calculateDuration(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        // To change the music once it's finished
        MusicPlayerService.mediaPlayer.setOnCompletionListener {
            changeMusic(1, true)
        }
    }

    private fun updateMusicData() {
        // get current music's data
        currentMusicTitle = defaultPlaylist[currentMusicIndex][titleIndex].toString()
        currentMusicArtist = defaultPlaylist[currentMusicIndex][artistIndex].toString()
        currentMusicDuration = parseInt(defaultPlaylist[currentMusicIndex][durationIndex].toString())
        currentMusicUri = Uri.parse(defaultPlaylist[currentMusicIndex][mediaUriIndex].toString())
    }

    private fun updateBitmap() {
        // updates music's album art and colors for status bar and window background
        // as it's independent of the playback so better start it on a different thread
        Thread {
            currentMusicAlbumArt = MediaInfo().createCircularBitmap(this, currentMusicUri)
            updateColors()  // updates the status bar and window background
            loadAlbumArt()  // load the current album art into the image view
        }.start()
    }

    private fun loadMusicMetadata() {
        musicArtistTextBox.text = currentMusicArtist
        musicTitleTextBox.text = currentMusicTitle
        currentSeekBarProgress.text = getString(R.string.default_duration)
        durationSeekBar.progress = 0    // set initial position just to be sure
        durationSeekBar.max = currentMusicDuration
        maxSeekBarProgress.text = calculateDuration(currentMusicDuration)
    }

    private fun loadAlbumArt() {
        runOnUiThread {
            albumArtImage.setImageBitmap(currentMusicAlbumArt)
        }
    }

    private fun updateColors() {
        runOnUiThread {
            getAccentColor(currentMusicAlbumArt)
            window.statusBarColor = dominantColor
            findViewById<ConstraintLayout>(R.id.rootLayout).setBackgroundColor(dominantColor)
        }
    }

    private fun updateSeekBar(currentPosition: Int) {
        // This part has to be in same thread as the UI
        // because other threads cannot access views from current thread
        runOnUiThread {
            currentSeekBarProgress.text = calculateDuration(currentPosition)
            durationSeekBar.progress = currentPosition
        }
    }

    private fun calculateDuration(seconds: Int): String {
        var duration = ""
        duration += (seconds / 60).toString().padStart(2, '0') + ":" +
                (seconds % 60).toString().padStart(2, '0')
        return duration
    }

    private fun getAccentColor(bitmap: Bitmap) {

        dominantColor = parseColor(getString(R.string.default_color))

        val palette = Palette.Builder(bitmap).generate()
        var tempColor = palette.getDominantColor(dominantColor)

        if (!colorIsDark(tempColor)) {
            // let's try once more to find a nice dark color
            tempColor = palette.getDarkMutedColor(dominantColor)
            if (!colorIsDark(tempColor))
                return
        }
        dominantColor = tempColor
    }

    private fun colorIsDark(color: Int): Boolean {
        val luminance: Double = 1-(0.299*Color.red(color) +
                0.587*Color.green(color) +
                0.114*Color.blue(color)) / 255
        if (luminance < 0.5)
            return false
        return true
    }

    fun togglePausePlay(pausePlayToggleButton: View) {

        if (pausePlayToggleButton.tag == getString(R.string.paused_text)) {
            musicPlayerService.play()
            pausePlayToggleButton.tag = getString(R.string.playing_text)
            pausePlayToggleButton.setBackgroundResource(R.drawable.pause)
//            val pendingIntent = PendingIntent.getActivity(applicationContext, 0,
//                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
//            val intent = Intent(this, MainActivity::class.java)
//            MusicPlayerService().startNotification(this@MainActivity, intent, currentMusicTitle)
//            musicPlayerService.startService(intent)
        } else {
            musicPlayerService.pause()
            pausePlayToggleButton.tag = getString(R.string.paused_text)
            pausePlayToggleButton.setBackgroundResource(R.drawable.play)
//            musicPlayerService.stopNotification()
//            MusicPlayerService().stopNotification()
        }

        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        animation.interpolator = bounceAnimationInterpolator
        pausePlayToggleButton.startAnimation(animation)
    }

    fun skipNext(skipNextButton: View) {

//        musicPlayerService.release()    // release the already running instance
        changeMusic(1)  // increase the index by 1

        skipNextButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.launch_right))
    }

    fun skipPrevious(skipPreviousButton: View) {

//        musicPlayerService.release()    // release the already running instance
        changeMusic(-1)  // decrease the index by 1

        skipPreviousButton.startAnimation(AnimationUtils.loadAnimation(this,R.anim.launch_left))
    }

    private fun changeMusic(jumpBy: Int, followRepeatValue: Boolean = false) {

        if (followRepeatValue) {    // the song changed automatically
            // this block should start if the song changed automatically
            if (REPEAT) {
                // repeat the same song
                musicPlayerService.seekTo(0)
            } else {
                // move to next
                updateCurrentMusicIndex(jumpBy)
            }
        } else {    // song changed manually
            if (REPEAT && repeatButton.tag == getString(R.string.repeat_once)) {
                // REPEAT mode is set to repeat once and song changed manually
                // now change the toggle as well
                // it's emulating a click here and since the sequence is:
                //      repeat off
                //      repeat on
                //      repeat once
                // just invoking the function once will turn the toggle off
                repeatToggle(repeatButton)
            }

            updateCurrentMusicIndex(jumpBy)
        }


        updateMusicData()   // updates metadata to be used by other functions

        try {
            musicPlayerService.reset()
        } catch (ex: UninitializedPropertyAccessException) {
            // initialize the music player
            // most probably the application has just started
            musicPlayerService = MusicPlayerService()
        }

        try {
            musicPlayerService.loadMedia(this, currentMusicUri)
        } catch (ex: Exception) {
            Log.e("loadingMedia", "could not load media")
        }

        if (playPauseToggleButton.tag == getString(R.string.playing_text)) {
            musicPlayerService.play()
        }

        updateCurrentMusicNumberText()  // to refresh the current song number

        loadMusicMetadata() // updates music title, artist and duration

        // activates the favorite button if the current music is listed in favorites
        updateFavoriteToggle()

        updateBitmap()  // updates the bitmap
    }

    private fun updateCurrentMusicIndex(jumpBy: Int) {
        if (SHUFFLE) {
            val temp = currentMusicIndex
            // seed the random function
            do {
                currentMusicIndex = nextInt(0, defaultPlaylist.size)
            } while (currentMusicIndex == temp) // to make sure it doesn't repeat the current song
        } else {
            when {
                (currentMusicIndex + jumpBy) < 0 -> currentMusicIndex =
                    (defaultPlaylist.size - 1)
                (currentMusicIndex + jumpBy) >= defaultPlaylist.size -> currentMusicIndex =
                    0
                else -> currentMusicIndex += jumpBy
            }
        }
    }

    private fun updateCurrentMusicNumberText() {
        val text = "${(currentMusicIndex + 1)}/${(defaultPlaylist.size)}"
        currentMusicNumberTextView.text = text
    }

    fun toggleFavorite(toggleFavoriteButton: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        animation.interpolator = bounceAnimationInterpolator

        if (toggleFavoriteButton.tag == getString(R.string.favorite_false)) {
            toggleFavoriteButton.setBackgroundResource(R.drawable.favorite_true)
            toggleFavoriteButton.tag = getString(R.string.favorite_true)
            if (!FAVORITE) {
                Playlist().addToPlaylist(
                    this, currentMusicUri.toString(),
                    getString(R.string.favorite_playlist_name))
            }
        } else {
            toggleFavoriteButton.setBackgroundResource(R.drawable.favorite_false)
            toggleFavoriteButton.tag = getString(R.string.favorite_false)
        }

        toggleFavoriteButton.startAnimation(animation)
    }

    private fun updateFavoriteToggle() {
        if (Playlist().isInPlaylist(this, currentMusicUri.toString(),
                getString(R.string.favorite_playlist_name))) {
            FAVORITE = true
            toggleFavorite(addFavButton)
        } else {
            if (addFavButton.tag == getString(R.string.favorite_true)) {
                // current music is not in favorites and the button is enabled
                FAVORITE = false
                toggleFavorite(addFavButton)
            }
        }
    }

    fun repeatToggle(repeatToggleButton: View) {

        when (repeatToggleButton.tag) {
            getString(R.string.repeat_inactive) -> {
                // Set it to active for all songs
                repeatToggleButton.setBackgroundResource(R.drawable.repeat_active)
                repeatToggleButton.tag = getString(R.string.repeat_active)
                REPEAT = true
            }
            getString(R.string.repeat_active) -> {
                // Set it to active for current song
                repeatToggleButton.setBackgroundResource(R.drawable.repeat_once)
                repeatToggleButton.tag = getString(R.string.repeat_once)
                REPEAT = true
            }
            else -> {
                // Disable it
                repeatToggleButton.setBackgroundResource(R.drawable.repeat_inactive)
                repeatToggleButton.tag = getString(R.string.repeat_inactive)
                REPEAT = false
            }
        }

        repeatToggleButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.repeat_animation))
    }

    fun shuffleToggle(shuffleToggleButton: View) {
        if (shuffleToggleButton.tag == getString(R.string.shuffle_inactive)) {
            shuffleToggleButton.setBackgroundResource(R.drawable.shuffle_active)
            shuffleToggleButton.tag = getString(R.string.shuffle_active)
            SHUFFLE = true
        } else {
            shuffleToggleButton.setBackgroundResource(R.drawable.shuffle_inactive)
            shuffleToggleButton.tag = getString(R.string.shuffle_inactive)
            SHUFFLE = false
        }
        shuffleToggleButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shuffle_animation))
    }

    fun preferencesMenu(settingsLaunchButton: View) {
        settingsLaunchButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate))
    }

    companion object {
        private var dominantColor: Int = 0
        lateinit var defaultPlaylist: Array<JSONArray>
        private var currentMusicIndex: Int = 0
        private lateinit var currentMusicUri: Uri
        private lateinit var currentMusicTitle: String
        private lateinit var currentMusicArtist: String
        private var currentMusicDuration: Int = 0
        private lateinit var currentMusicAlbumArt: Bitmap

        // shuffle and repeat
        private var SHUFFLE = false
        private var REPEAT = false
        private var FAVORITE = false

        const val titleIndex = 0
        const val artistIndex = 1
        const val durationIndex = 2
        const val mediaUriIndex = 3
    }
}