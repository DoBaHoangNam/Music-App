package com.example.musicapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.model.Artist

class ArtistAdapter(
    private val items: MutableList<Artist>,
    private val navController: NavController
) :
    RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singerName: TextView = itemView.findViewById(R.id.singerTv1)
        val image: ImageView = itemView.findViewById(R.id.imgAlbum1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArtistAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.singerName.text = currentItem.name
        Glide.with(holder.itemView.context)
            .load(currentItem.image) // Load the image from the URL or URI
            .placeholder(R.mipmap.ic_person_round) // Placeholder image
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("artist", currentItem)}
            navController.navigate(R.id.action_fragmentArtist_to_fragmentArtistSingle, bundle)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}