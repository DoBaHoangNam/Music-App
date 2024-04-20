package com.example.musicapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.musicapp.OnRecentSearchItemClickListener
import com.example.musicapp.R
import com.example.musicapp.RecentSearchListener
import com.example.musicapp.model.RecentSearch
import com.example.musicapp.model.SongInAlbum

class SongInAlbumAdapter(
    private val items: MutableList<SongInAlbum>,
) :
    RecyclerView.Adapter<SongInAlbumAdapter.ViewHolder>() {
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.tvSongName2)
        val number: TextView = itemView.findViewById(R.id.tvNumber)
        val length: TextView = itemView.findViewById(R.id.tvLength)
        val songPlayAnim: LottieAnimationView = itemView.findViewById(R.id.songPlayAnim1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongInAlbumAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_song_in_album_artist_single, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongInAlbumAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.songName.text = currentItem.songName
        holder.number.text = (position + 1).toString()
        holder.length.text = currentItem.length

        if (selectedItemPosition == position) {
            holder.itemView.setBackgroundResource(R.color.white)
            holder.songPlayAnim.visibility = View.VISIBLE
            holder.number.visibility = View.INVISIBLE
            holder.songName.isSelected = true
        } else {
            holder.itemView.setBackgroundResource(0)
            holder.songPlayAnim.visibility = View.INVISIBLE
            holder.number.visibility = View.VISIBLE
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