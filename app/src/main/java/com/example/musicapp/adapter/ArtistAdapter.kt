package com.example.musicapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist

class ArtistAdapter(private val items: MutableList<Artist>) :
    RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val singerName: TextView = itemView.findViewById(R.id.singerTv1)
        val image: ImageView = itemView.findViewById(R.id.imgAlbum1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArtistAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.singerName.text =  currentItem.name
        holder.image.setImageResource(currentItem.image)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}