package com.example.musicapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.DataHolder
import com.example.musicapp.R
import com.example.musicapp.adapter.PlaylistSelectedAdapter
import com.example.musicapp.databinding.ActivityAlbumSelectedBinding
import com.example.musicapp.model.Playlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityAlbumSelected : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumSelectedBinding
    private var listOfPlaylists = mutableListOf<Playlist>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumSelectedBinding.inflate(layoutInflater)

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.icAdd.setOnClickListener {
            showCreatePlaylistDialog()
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        val songName = intent.getStringExtra("song_name")

        fetchPlaylists{playlists ->
            listOfPlaylists = playlists
            displayPlaylist(listOfPlaylists, songName)
            Log.d("ActivitySelectPlaylist", "$listOfPlaylists" + "1")
        }

        binding.playlistFavorite.setOnClickListener {
            addToFavoritePlaylist(songName)
        }

        setContentView(binding.root)
    }

    private fun addToFavoritePlaylist(songName: String?) {
        if (songName == null) return

        val favoriteSongsRef = databaseReference.child("user").child(auth.currentUser?.uid ?: "")
            .child("favouriteSongs")

        if (DataHolder.favouriteSongs.contains(songName)) {
            Toast.makeText(this, "Song already exists in the favorite playlist", Toast.LENGTH_SHORT).show()
        } else {
            val newFavoriteRef = favoriteSongsRef.push()
            newFavoriteRef.setValue(songName)
            DataHolder.favouriteSongs.add(songName)
            Toast.makeText(this, "Song added to the favorite playlist", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun displayPlaylist(listOfPlaylists: MutableList<Playlist>, songName: String?) {
        binding.recvPlaylist.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = PlaylistSelectedAdapter(listOfPlaylists,  songName as String, this, this)
        Log.d("ActivitySelectPlaylist", "$listOfPlaylists" + "2")
        binding.recvPlaylist.adapter = adapter
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_playlist, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)

        val etPlaylistName = dialogView.findViewById<EditText>(R.id.etPlaylistName)
        val btnCreate = dialogView.findViewById<TextView>(R.id.btnCreate)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)

        btnCreate.setOnClickListener {
            val playlistName = etPlaylistName.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                createPlaylist(playlistName)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a playlist name", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createPlaylist(name: String) {
        val playlist = Playlist("","$name", "0 Songs", mutableListOf())
        listOfPlaylists.add(playlist)
        val playlistKey = database.reference.child("user").child(userId).child("playlists").push().key
        playlist.id = playlistKey
        if (playlistKey != null) {
            database.reference.child("user").child(userId).child("playlists").child(playlistKey).setValue(playlist)
            Toast.makeText(this, "Playlist \"$name\" created", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to create playlist", Toast.LENGTH_SHORT).show()
        }

        Log.d("ActivitySelectPlaylist", "$listOfPlaylists" + "3")
        Toast.makeText(this, "Playlist \"$name\" created", Toast.LENGTH_SHORT).show()
    }

    private fun fetchPlaylists(callback: (MutableList<Playlist>) -> Unit) {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        val playlistsRef: DatabaseReference =
            database.reference.child("user").child(userId).child("playlists")
        playlistsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                val playlists = mutableListOf<Playlist>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val playlist = snapshot.getValue(Playlist::class.java)
                        playlist?.let { playlists.add(it) }
                    }
                }
                callback(playlists)
            } else {
                // Handle error if needed
                callback(mutableListOf())
            }
        }
    }
}