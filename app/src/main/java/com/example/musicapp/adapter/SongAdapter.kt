package com.example.musicapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.musicapp.R
import com.example.musicapp.model.Album
import com.example.musicapp.model.Song

class SongAdapter(private val items: MutableList<Song>) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION



    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val singerName: TextView = itemView.findViewById(R.id.tvSingerName1)
        val songName: TextView = itemView.findViewById(R.id.tvSongName1)
        val image: ImageView = itemView.findViewById(R.id.imgSong1)
        val songPlayAnim: LottieAnimationView = itemView.findViewById(R.id.songPlayAnim)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.singerName.text =  currentItem.singerName
        holder.songName.text =  currentItem.songName
        holder.image.setImageResource(currentItem.image)

        if (selectedItemPosition == position) {
            holder.itemView.setBackgroundResource(R.color.white)
            holder.songPlayAnim.visibility = View.VISIBLE
            holder.songName.isSelected = true
        } else {
            holder.itemView.setBackgroundResource(0)
            holder.songPlayAnim.visibility = View.INVISIBLE
            holder.songName.isSelected = false
        }

        holder.itemView.setOnClickListener {
            setSelectedItem(holder.adapterPosition)
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setSelectedItem(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged() // Cập nhật lại RecyclerView để vẽ lại giao diện
    }

}