package com.gaurav712.gigsmusic

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.RuntimeException

private var mediaInfo = MediaInfo()
lateinit var rippleAnimationImageView: ImageView

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        rippleAnimationImageView = findViewById(R.id.rippleAnimationImage)
        rippleAnimationImageView.startAnimation(AnimationUtils
            .loadAnimation(applicationContext, R.anim.loading_animation))

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if it's already permitted to storage
            if (contentResolver.persistedUriPermissions.isEmpty()) {
                getAccess()
            } else {
                processFiles()
            }
        }, 500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == OPEN_DOCUMENT_TREE_REQ_CODE && resultCode == Activity.RESULT_OK) {

            val treeUri = data?.data    // get data

            if (treeUri != null) {
                contentResolver.takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            processFiles()
        }
    }

    private fun getAccess() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DOCUMENT_TREE_REQ_CODE)
    }

    private fun processFiles() {
        CoroutineScope(IO).launch {
            if (!File(filesDir, getString(R.string.default_playlist_name)).exists()) {
                val documentFile: DocumentFile = DocumentFile.fromTreeUri(applicationContext,
                    contentResolver.persistedUriPermissions[0].uri)!!
                generateDefaultPlaylist(documentFile)
            }

            if (File(filesDir, getString(R.string.default_playlist_name)).exists()) {
                MainActivity.defaultPlaylist = Playlist().getDefaultPlaylist(
                    applicationContext,
                    getString(R.string.default_playlist_name)
                )
            } else {
                // Notify MainActivity about unavailability of defaultPlaylist
                MainActivity.DEFAULT_PLAYLIST_AVAILABLE = false
            }

            withContext(Main) {
                startMainActivity()
            }
        }
    }

    private fun startMainActivity() {
        rippleAnimationImageView.clearAnimation()   // to stop the animation
        val intentToLaunchMainActivity = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intentToLaunchMainActivity)
    }

    private suspend fun generateDefaultPlaylist(documentFile: DocumentFile) {

        var defaultPlaylist = arrayOf<ArrayList<String>>()
//        var defaultPlaylistIsInitialized = false
        val files = documentFile.listFiles()
//        var count = 0
//        var countSetFlag = true

        for (file in files) {

            if (file.isFile && file.type == getString(R.string.mp3_mime_type)) {
                Log.i("file", file.name.toString())
//                Thread {
                try {
                    // Just in case if the music has no title
                    MediaInfo.DEFAULT_TITLE = file.name.toString()
                    val (title, artist, duration) = mediaInfo.getMetadata(this, file.uri)
                    defaultPlaylist += (arrayListOf(
                        title, artist, duration.toString(),
                        file.uri.toString()
                    ))
                } catch (ex: RuntimeException) {
                    Log.e("musicFileError", "Can't process " + file.name.toString())
                }
//                    Log.i(count.toString(), title + artist)
//                    if (countSetFlag) { // to make it thread-safe
//                        countSetFlag = false
//                        count++
//                        countSetFlag = true
//                    }
//                    if (count == files.size)
//                        defaultPlaylistIsInitialized = true
//                }.start()
            } else {
                continue
            }
        }

        // Generate the default playlist
        if (defaultPlaylist.isEmpty()) {
            withContext(Main) {
                Toast.makeText(applicationContext,
                    "No *.mp3 found in the selected directory!",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {

            mediaInfo.release()

            Playlist().generateDefaultPlaylist(
                this,
                getString(R.string.default_playlist_name), defaultPlaylist
            )
        }

//        while (true) {
//
//            sleep(50)   // wait for defaultPlaylist to be initialised
//
//            if (defaultPlaylistIsInitialized) {

//            }
//        }
    }

    private fun destroyActivity() {
        finish()
    }

    companion object {
        const val OPEN_DOCUMENT_TREE_REQ_CODE: Int = 42 // just a random number
    }
}