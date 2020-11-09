package com.gaurav712.gigsmusic

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class Playlist {

    fun generateDefaultPlaylist(context: Context,
                                defaultPlaylistName: String,
                                defaultPlaylist: Array<ArrayList<String>>) {

        val jsonObject = JSONObject()

        var count = 0

        defaultPlaylist.forEach {
            jsonObject.put(count.toString(), JSONArray()
                .put(it[titleIndex])
                .put(it[artistIndex])
                .put(it[durationIndex])
                .put(it[mediaUriIndex]))
            count++
        }

        val defaultPlaylistFile = File(context.filesDir, defaultPlaylistName)
        val playlistFileWriter = FileWriter(defaultPlaylistFile)
        val bufferedWriter = BufferedWriter(playlistFileWriter)

        bufferedWriter.write(jsonObject.toString())

        bufferedWriter.close()
        playlistFileWriter.close()
    }

    fun getDefaultPlaylist(context: Context, defaultPlaylistName: String): Array<JSONArray> {

        var defaultPlaylist = arrayOf<JSONArray>()
        val defaultPlaylistFile = File(context.filesDir, defaultPlaylistName)
        val fileReader = FileReader(defaultPlaylistFile)
        val bufferedReader = BufferedReader(fileReader)

        val jsonObject = JSONObject(bufferedReader.readLine())
        val len = jsonObject.length()

        for (count in 0 until len) {
            defaultPlaylist += jsonObject.get(count.toString()) as JSONArray
        }

        return defaultPlaylist
    }

    fun isInPlaylist(context: Context, mediaUri: String, playlistName: String): Boolean {

        if (!File(context.filesDir, playlistName).exists()) {
            return false
        }

        val stream = File(context.filesDir, playlistName)
        var exists = false

        // Check if above has an entry
        stream.forEachLine {
            if (mediaUri == it)
                exists = true
        }

        return exists
    }

    fun addToPlaylist(context: Context, mediaUri: String, playlistName: String) {

        // Append the entry to stream
        File(context.filesDir, playlistName).appendText("$mediaUri\n")
    }

    fun removeFromPlaylist(context: Context, mediaUri: String, playlistName: String) {

        val inputStream = File(context.filesDir, playlistName)
        val outputStream = File(context.filesDir, TEMP_PLAYLIST_FILE_NAME)

        inputStream.forEachLine {
            if (it != mediaUri) {
                outputStream.appendText("$it\n")
            }
        }

        inputStream.delete()
        outputStream.renameTo(File(context.filesDir, playlistName))
    }

    companion object {
        // the indices
        const val titleIndex = 0
        const val artistIndex = 1
        const val durationIndex = 2
        const val mediaUriIndex = 3

        // temporary playlist file name
        const val TEMP_PLAYLIST_FILE_NAME = ".temp.playlist"
    }
}
