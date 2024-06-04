package com.example.musicapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.model.Album

class AlbumAdapter(
    private val items: MutableList<Album>,
    private val navController: NavController
) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singerName: TextView = itemView.findViewById(R.id.singerTv)
        val image: ImageView = itemView.findViewById(R.id.imgAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlbumAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.singerName.text = currentItem.name
        Glide.with(holder.itemView.context)
            .load(currentItem.albumArt) // Load the image from the URL or URI
            .placeholder(R.mipmap.ic_song_round_high) // Placeholder image
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val currentDestinationId = navController.currentDestination?.id
            val bundle = Bundle().apply {
                putParcelable("album", currentItem)
            }
            if (currentDestinationId == R.id.fragmentAlbum) {
                navController.navigate(R.id.action_fragmentAlbum_to_fragmentAlbumSingle, bundle)
            } else if (currentDestinationId == R.id.fragmentForYou) {
                navController.navigate(R.id.action_fragmentForYou_to_fragmentAlbumSingle, bundle)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}