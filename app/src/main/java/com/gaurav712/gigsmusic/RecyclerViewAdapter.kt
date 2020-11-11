package com.gaurav712.gigsmusic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistRecyclerViewAdapter(private val context: Context, private val musicList: ArrayList<String>) :
    RecyclerView.Adapter<PlaylistRecyclerViewAdapter.PlaylistRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistRecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_view_list_item, parent, false)
        return PlaylistRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: PlaylistRecyclerViewHolder, position: Int) {
        holder.recyclerViewTitleTextView.text = musicList[position]
    }

    class PlaylistRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerViewTitleTextView: TextView = itemView.findViewById(R.id.recyclerViewTitleTextView)
    }
}
