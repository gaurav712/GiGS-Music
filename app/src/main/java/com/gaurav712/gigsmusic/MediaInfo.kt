package com.gaurav712.gigsmusic

import android.content.Context
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri

class MediaInfo {

    private lateinit var mediaMetadata: MediaMetadataRetriever

    fun getMetadata(context: Context, mediaPath: Uri): Triple<String, String, Int> {

        mediaMetadata = MediaMetadataRetriever()
        mediaMetadata.setDataSource(context, mediaPath)

        val title = mediaMetadata.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_TITLE)!!  // title
        val artist = mediaMetadata.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_ARTIST)!!    // artist
        // get media duration
        // "/ 1000" is to convert it to seconds
        val duration = (mediaMetadata.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000) //duration

//        mediaMetadata.close()

        return Triple(title, artist, duration)
    }

    fun createCircularBitmap(context: Context, mediaPath: Uri): Bitmap {

        mediaMetadata = MediaMetadataRetriever()
        mediaMetadata.setDataSource(context, mediaPath)

        // fetch the album art
        val byteArray: ByteArray = mediaMetadata.embeddedPicture!!
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

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

//        mediaMetadata.release()
        release()

        return circularBitmap
    }

    fun release() {
        mediaMetadata.release()
    }
}