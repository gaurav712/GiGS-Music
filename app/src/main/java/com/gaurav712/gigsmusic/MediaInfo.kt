package com.gaurav712.gigsmusic

import android.content.Context
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlin.Exception

class MediaInfo {

    private lateinit var mediaMetadata: MediaMetadataRetriever

    fun getMetadata(context: Context, mediaPath: Uri): Triple<String, String, Int> {

        mediaMetadata = MediaMetadataRetriever()
        mediaMetadata.setDataSource(context, mediaPath)

        val title = getTitle()
        val artist = getArtist()

        // get media duration
        // "/ 1000" is to convert it to seconds
        val duration = (mediaMetadata.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000) //duration

        return Triple(title, artist, duration)
    }

    private fun getTitle(): String {
        return try {
            mediaMetadata.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE
            )!!
        } catch (ex: Exception) {
            DEFAULT_TITLE
        }
    }

    private fun getArtist(): String {
        return try {
            mediaMetadata.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ARTIST
            )!!
        } catch (ex: Exception) {
            DEFAULT_ARTIST
        }
    }

    fun createCircularBitmap(context: Context,
                             mediaPath: Uri
    ): Bitmap {

        mediaMetadata = MediaMetadataRetriever()
        mediaMetadata.setDataSource(context, mediaPath)

        val bitmap = getBitmap(context)

        val color = -0xffffff

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val circularBitmap = Bitmap.createBitmap(bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circularBitmap)

        paint.color = color
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(),
            paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        release()

        return circularBitmap
    }

    private fun getBitmap(context: Context): Bitmap {
        return try {
            // fetch the album art
            val byteArray = mediaMetadata.embeddedPicture!!
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (ex: Exception) {
            // load the app icon as the default album art
            BitmapFactory.decodeResource(context.resources, R.drawable.launcher_icon)
        }
    }

    fun release() {
        mediaMetadata.release()
    }

    companion object {
        private const val DEFAULT_TITLE = "Unknown"
        private const val DEFAULT_ARTIST = "Unknown"
    }
}