package com.example.musicapp.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.model.Song

class SongSelectedAdapter(private val context: Context,
                          private val items: MutableList<Song>,
                          private var selectedSongs: MutableList<String>) :
    RecyclerView.Adapter<SongSelectedAdapter.ViewHolder>() {
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val singerName: TextView = itemView.findViewById(R.id.tvSingerName2)
        val songName: TextView = itemView.findViewById(R.id.tvSongName2)
        val image: ImageView = itemView.findViewById(R.id.imgSong2)
        val checkbox: CheckBox = itemView.findViewById(R.id.cbSelectSong)

        fun bind(song: Song) {
            singerName.text = song.singerName
            songName.text = song.songName
            Glide.with(itemView.context)
                .load(song.image)
                .placeholder(R.mipmap.ic_song_round_high)
                .into(image)

            // Set initial checked state
            checkbox.isChecked = selectedSongs.contains(song.songName)

            // Toggle checkbox state on item click
            itemView.setOnClickListener {
                if (selectedSongs.contains(song.songName)) {
                    selectedSongs.remove(song.songName)
                    Log.d("SongSelected", "$selectedSongs 1")
                } else {
                    selectedSongs.add(song.songName)
                    Log.d("SongSelected", "$selectedSongs 2")
                }
                notifyDataSetChanged()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSelectedAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_song_selected, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongSelectedAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)


    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun selectAll(isChecked: Boolean) {
        selectedSongs.clear()
        if (isChecked) {
            for(song in items){
                selectedSongs.addAll(listOf(song.songName))
            }

        }
        Log.d("SongSelected", "$selectedSongs 3")
        notifyDataSetChanged()
    }


}