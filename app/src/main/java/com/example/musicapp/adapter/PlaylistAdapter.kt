package com.example.musicapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.model.Playlist

class PlaylistAdapter(private val items: MutableList<Playlist>, private val navController: NavController) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        val numberOfSong: TextView = itemView.findViewById(R.id.tvNumberOfSong)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.playlistName.text = currentItem.playlistName
        holder.numberOfSong.text = currentItem.numberOfSong

        holder.itemView.setOnClickListener {
            navController.navigate(R.id.action_fragmentPlaylist_to_fragmentPlaylistSpecific)
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }

}