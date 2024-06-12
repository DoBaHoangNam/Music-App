package com.example.musicapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.musicapp.R
import com.example.musicapp.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class SongInPlaylistAdapter(
    private val context: Context,
    private val items: MutableList<Song>,
    private val playlistId: String,
    private val itemClickListener: (Song) -> Unit
):

    RecyclerView.Adapter<SongInPlaylistAdapter.ViewHolder>() {
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""


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
    ): SongInPlaylistAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_song_in_album_artist_single, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongInPlaylistAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem, itemClickListener)

        holder.option.setOnClickListener { view ->
            showPopupMenu(view, currentItem, playlistId)
        }


    }

    private fun showPopupMenu(view: View, song: Song, playlistId: String) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.option_for_song_in_playlist, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.remove -> {
                    removeSongFromPlaylist(playlistId, song.songName as String)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun removeSongFromPlaylist(playlistId: String, songName: String) {
        val playlistRef = database.reference.child("user").child(userId).child("playlists").child(playlistId).child("songs")

        playlistRef.get().addOnSuccessListener { dataSnapshot ->
            val songsList = dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

            if (songsList.contains(songName)) {
                songsList.remove(songName)
                playlistRef.setValue(songsList).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Song removed from the playlist", Toast.LENGTH_SHORT).show()
                        items.removeAll { it.songName == songName }
                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Failed to update playlist", Toast.LENGTH_SHORT).show()
                        Log.e("PlaylistAdapter", "Failed to update playlist in database")
                    }
                }
            } else {
                Toast.makeText(context, "Song not found in the playlist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.e("PlaylistAdapter", "Failed to fetch playlist", it)
            Toast.makeText(context, "Failed to fetch playlist", Toast.LENGTH_SHORT).show()
        }
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