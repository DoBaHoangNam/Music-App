package com.example.musicapp.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.musicapp.R
import com.example.musicapp.model.Playlist
import com.example.musicapp.model.Song
import com.example.musicapp.ui.ActivityAlbumSelected

class SongInAlbumAdapter(
    private val context: Context,
    private val items: MutableList<Song>,
    private val itemClickListener: (Song) -> Unit):

    RecyclerView.Adapter<SongInAlbumAdapter.ViewHolder>() {
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.tvSongName2)
        val number: TextView = itemView.findViewById(R.id.tvNumber)
        val length: TextView = itemView.findViewById(R.id.tvLength)
        val songPlayAnim: LottieAnimationView = itemView.findViewById(R.id.songPlayAnim1)
        val option: TextView = itemView.findViewById(R.id.icOption)

        fun bind(song: Song, clickListener: (Song) -> Unit) {
            songName.text = song.songName
            number.text = (adapterPosition + 1).toString()
            length.text = formatDuration(song.duration)

            if (selectedItemPosition == adapterPosition) {
                itemView.setBackgroundResource(R.color.white)
                songPlayAnim.visibility = View.VISIBLE
                number.visibility = View.INVISIBLE
                songName.isSelected = true
            } else {
                itemView.setBackgroundResource(0)
                songPlayAnim.visibility = View.INVISIBLE
                number.visibility = View.VISIBLE
                songName.isSelected = false
            }

            itemView.setOnClickListener {
                setSelectedItem(adapterPosition)
                saveCurrentSongName(song.songName)
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongInAlbumAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_song_in_album_artist_single, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongInAlbumAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem, itemClickListener)

        holder.option.setOnClickListener { view ->
            showPopupMenu(view, currentItem)
        }


    }

    private fun showPopupMenu(view: View, song: Song) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.option_for_song_in_album, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.add -> {
                    val songNameToAdd = song.songName
                    Log.d("SongInAlbum", "songNameToAdd: $songNameToAdd")

                    val intent = Intent(context, ActivityAlbumSelected::class.java).apply {
                        putExtra("song_name", songNameToAdd)
                    }
                    context.startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun saveCurrentSongName(songName: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("selected_song", songName).apply()
        Log.d("check_source", songName + " song name")
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setSelectedItem(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged() // Cập nhật lại RecyclerView để vẽ lại giao diện
    }

    fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }





}