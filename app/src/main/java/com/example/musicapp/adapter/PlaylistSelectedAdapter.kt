package com.example.musicapp.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.model.Playlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PlaylistSelectedAdapter(
    private val items: MutableList<Playlist>,
    private val songname: String,
    private val activity: Activity,
    private val context: Context
) :
    RecyclerView.Adapter<PlaylistSelectedAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.tvplaylist2)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistSelectedAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_album_selected, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistSelectedAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.playlistName.text = currentItem.playlistName

        holder.itemView.setOnClickListener {
            updatePlaylistInDatabase(currentItem, songname, currentItem.playlistName as String)
        }


    }


    override fun getItemCount(): Int {
        return items.size
    }


    private fun updatePlaylistInDatabase(playlist: Playlist, songName: String, playlistName: String) {
        Log.d("PlaylistAdapter", "$playlist")

        // Initialize the songs list if it's null
        if (playlist.songs == null) {
            playlist.songs = mutableListOf()
        }

        playlist.songs?.add(songName)
        val database: DatabaseReference =
            database.reference.child("user").child(userId).child("playlists").child(playlist.id as String)
                .child("songs")

        database.setValue(playlist.songs).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Song added to the $playlistName playlist", Toast.LENGTH_SHORT).show()
                (context as Activity).finish()  // Ensure to call finish on the activity
                Log.e("PlaylistAdapter", "$playlist")
            } else {
                Log.e("PlaylistAdapter", "Failed to update playlist in database")
            }
        }
    }



}