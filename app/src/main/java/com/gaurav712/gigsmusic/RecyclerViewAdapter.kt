package com.gaurav712.gigsmusic

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<PlaylistRecyclerViewAdapter.PlaylistRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistRecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_view_list_item, parent, false)
        return PlaylistRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return MainActivity.defaultPlaylist.size
    }

    override fun onBindViewHolder(holder: PlaylistRecyclerViewHolder, position: Int) {

        holder.itemView.setOnClickListener {
            (holder.itemView.context as MainActivity).onItemClicked(position)
        }

        holder.recyclerViewTitleTextView.text = MainActivity.defaultPlaylist[position][titleIndex].toString()
        holder.recyclerViewArtistTextView.text = MainActivity.defaultPlaylist[position][artistIndex].toString()
        CoroutineScope(IO).launch {
            val bitmap = MediaInfo().getPlaylistThumbnail(
                context,
                Uri.parse(MainActivity.defaultPlaylist[position][mediaUriIndex].toString())
            )

            updateThumbnail(holder, bitmap)
        }
    }

    private suspend fun updateThumbnail(holder: PlaylistRecyclerViewHolder, bitmap: Bitmap) {
        withContext(Main) {
            holder.playlistThumbnailImageView.setImageBitmap(bitmap)
        }
    }

    class PlaylistRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerViewArtistTextView: TextView = itemView.findViewById(R.id.recyclerViewArtistTextView)
        val recyclerViewTitleTextView: TextView = itemView.findViewById(R.id.recyclerViewTitleTextView)
        val playlistThumbnailImageView: ImageView = itemView.findViewById(R.id.playlistThumbnailImageView)
    }

    companion object {
        const val titleIndex = 0
        const val artistIndex = 1
        const val mediaUriIndex = 3
    }
}