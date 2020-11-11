package com.gaurav712.gigsmusic

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.DragEvent.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistFragment : Fragment() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootFragment = inflater.inflate(R.layout.playlist_fragment, container, false)
        val recyclerViewAdapter = activity?.let { PlaylistRecyclerViewAdapter(it) }
//        recyclerView = activity?.findViewById(R.id.playlistRecyclerView)!!
        val recyclerView: RecyclerView = rootFragment.findViewById(R.id.playlistRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = recyclerViewAdapter
//        rootFragment.setOnDragListener(View.OnDragListener(view: View, event: DragEvent))
//        if (container != null) {
//            viewGroup = container
//        }

//        CoroutineScope(IO).launch {
//            MainActivity.defaultPlaylist.forEach {
//                recyclerAdapter.plusAssign(it[0].toString())
//            }
//        }

        // Inflate the layout for this fragment
        return rootFragment
    }

//    private suspend fun updateRecyclerView() {
//        withContext(Main) {
//            recyclerView.adapter = RecyclerView.Adapter<>
//        }
//    }
}