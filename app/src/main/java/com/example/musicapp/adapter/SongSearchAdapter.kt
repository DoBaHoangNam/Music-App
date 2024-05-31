package com.example.musicapp.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.model.Song
import com.example.musicapp.ui.MainActivity

class SongSearchAdapter(
    private val requireContext: Context,
    private val items: MutableList<Song>,
    private val itemClickListener: (Song) -> Unit
) :
    RecyclerView.Adapter<SongSearchAdapter.ViewHolder>() {
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singerName: TextView = itemView.findViewById(R.id.tvSingerName1)
        val songName: TextView = itemView.findViewById(R.id.tvSongName1)
        val image: ImageView = itemView.findViewById(R.id.imgSong1)
        val songPlayAnim: LottieAnimationView = itemView.findViewById(R.id.songPlayAnim)

        fun bind(song: Song, clickListener: (Song) -> Unit) {
            singerName.text = song.singerName
            songName.text = song.songName
            Glide.with(itemView.context)
                .load(song.image)
                .placeholder(R.mipmap.ic_song_round)
                .into(image)

            if (selectedItemPosition == adapterPosition) {
                itemView.setBackgroundResource(R.color.white)
                image.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                songPlayAnim.visibility = View.VISIBLE
                songName.isSelected = true
            } else {
                itemView.setBackgroundResource(0)
                songPlayAnim.visibility = View.INVISIBLE
                songName.isSelected = false
            }

            itemView.setOnClickListener {
                setSelectedItem(adapterPosition)
                clickListener(song)

            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongSearchAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongSearchAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem, itemClickListener)


    }

    override fun getItemCount(): Int {
        return items.size
    }


    fun setSelectedItem(position: Int) {
        val previousItemPosition = selectedItemPosition
        selectedItemPosition = position

        notifyItemChanged(previousItemPosition)
        notifyItemChanged(position)
    }

}