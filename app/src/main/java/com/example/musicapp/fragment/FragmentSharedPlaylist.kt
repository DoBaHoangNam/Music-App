package com.example.musicapp.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.adapter.SharePlaylistAdapter
import com.example.musicapp.databinding.FragmentSharedPlaylistBinding
import com.example.musicapp.model.SharePlaylist
import com.example.musicapp.model.Song
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class FragmentSharedPlaylist : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: FragmentSharedPlaylistBinding
    private var listOfPlaylists = mutableListOf<SharePlaylist>()
    private lateinit var fragmentManager: FragmentManager
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSharedPlaylistBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        databaseReference = FirebaseDatabase.getInstance().reference

        fetchSharePlaylist { playlists ->
            handleNewSongs(playlists)
            listOfPlaylists = playlists
            displaySharedPlaylist(listOfPlaylists)
            Log.d("FragmentPlaylist", "$listOfPlaylists" + "1")
        }

        fragmentManager = requireActivity().supportFragmentManager
        binding.btnBack.setOnClickListener {
            fragmentManager.popBackStack()
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                fragmentManager.popBackStack()
                true
            } else false
        }
    }

    private fun displaySharedPlaylist(listOfSharePlaylists: MutableList<SharePlaylist>) {
        binding.recvSharePlayList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SharePlaylistAdapter(listOfSharePlaylists,findNavController(), requireContext())
        Log.d("FragmentSharedPlaylist", "$listOfSharePlaylists" + "2")
        binding.recvSharePlayList.adapter = adapter
    }

    private fun fetchSharePlaylist(callback: (MutableList<SharePlaylist>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""
        val playlistRef = database.child("user").child(userId).child("sharedPlaylist")

        playlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val playlists = mutableListOf<SharePlaylist>()
                for (playlistSnapshot in dataSnapshot.children) {
                    val playlistId = playlistSnapshot.key ?: continue
                    val playlistName = playlistSnapshot.child("playlistName").getValue(String::class.java)
                    val numberOfSongs = playlistSnapshot.child("numberOfSong").getValue(String::class.java)

                    val songs = mutableListOf<Song>()
                    for (songSnapshot in playlistSnapshot.child("songs").children) {
                        val song = songSnapshot.getValue(Song::class.java)
                        song?.let { songs.add(it) }
                    }

                    val playlist = SharePlaylist(
                        id = playlistId,
                        playlistName = playlistName,
                        numberOfSong = numberOfSongs,
                        songs = songs
                    )
                    playlists.add(playlist)
                }
                callback(playlists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchSharePlaylist", "Failed to fetch playlists: ${error.message}")
                callback(mutableListOf()) // Return an empty list on failure
            }
        })
    }

    private fun savePlaylistsToPreferences(playlists: List<SharePlaylist>) {
        val sharedPreferences = requireContext().getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(playlists)
        editor.putString("playlists", json)
        editor.apply()
    }

    private fun getPlaylistsFromPreferences(): List<SharePlaylist> {
        val sharedPreferences = requireContext().getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("playlists", null)
        val type = object : TypeToken<List<SharePlaylist>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun handleNewSongs(newPlaylists: List<SharePlaylist>) {
        val storedPlaylists = getPlaylistsFromPreferences()
        Log.d("SharedPlaylists", "storedPlaylists : $storedPlaylists")
        Log.d("SharedPlaylists", "newPlaylists: $newPlaylists")

        val newPlaylistsToShow = mutableListOf<SharePlaylist>()
        val newSongsToShow = mutableListOf<Pair<Song, SharePlaylist>>()

        for (newPlaylist in newPlaylists) {
            val oldPlaylist = storedPlaylists.find { it.id == newPlaylist.id }

            if (oldPlaylist == null) {
                // Playlist mới
                newPlaylistsToShow.add(newPlaylist)
            } else {
                // Playlist đã tồn tại, kiểm tra các bài hát mới
                val newSongs = newPlaylist.songs?.filter { song ->
                    val songExists = oldPlaylist.songs?.any { it.id == song.id } == true
                    !songExists
                } ?: emptyList()

                newSongs.forEach { newSong ->
                    newSongsToShow.add(Pair(newSong, newPlaylist))
                }
            }
        }

        // Hiển thị dialog cho các playlist mới
        newPlaylistsToShow.forEach { newPlaylist ->
            newPlaylist.songs?.forEach { newSong ->
                showNewSongDialog(newSong, newPlaylist)
            }
        }

        // Hiển thị dialog cho các bài hát mới trong playlist đã tồn tại
        newSongsToShow.forEach { (newSong, playlist) ->
            showNewSongDialog(newSong, playlist)
        }

        savePlaylistsToPreferences(newPlaylists)
    }



    private fun showNewSongDialog(newSong: Song, playlist: SharePlaylist) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_accept_share, null)
        val textView = dialogView.findViewById<TextView>(R.id.tvMessage)
        textView.text = "A new song '${newSong.songName}' has been added to the playlist '${playlist.playlistName}'. Do you want to accept it?"

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        }

        dialogView.findViewById<TextView>(R.id.btnReject).setOnClickListener {
            val playlistId = playlist.id ?: return@setOnClickListener
            val playlistRef = databaseReference.child("user").child(userId).child("sharedPlaylist").child(playlistId)

            playlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Retrieve the songs node
                    val songsNode = snapshot.child("songs")

                    // Find the last song in the list
                    var lastSongId: String? = null
                    songsNode.children.forEach { songSnapshot ->
                        lastSongId = songSnapshot.key // This will give you the last song ID
                    }

                    // Ensure a song was found
                    if (lastSongId != null) {
                        val songRef = playlistRef.child("songs").child(lastSongId!!)
                        songRef.removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Song rejected and removed from playlist.", Toast.LENGTH_SHORT).show()
                                Log.d("showNewSongDialog", "Song removed successfully")
                            } else {
                                Toast.makeText(context, "Failed to remove song.", Toast.LENGTH_SHORT).show()
                                Log.e("showNewSongDialog", "Failed to remove song: ${task.exception?.message}")
                            }
                        }
                    } else {
                        Toast.makeText(context, "No song found to remove.", Toast.LENGTH_SHORT).show()
                        Log.e("showNewSongDialog", "No song found to remove")
                    }

                    dialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch playlist data.", Toast.LENGTH_SHORT).show()
                    Log.e("showNewSongDialog", "Failed to fetch playlist data: ${error.message}")
                    dialog.dismiss()
                }
            })

            fetchSharePlaylist { playlists ->
                listOfPlaylists = playlists
                savePlaylistsToPreferences(playlists)
                displaySharedPlaylist(listOfPlaylists)
                Log.d("SharedPlaylists", "$playlists" + "1")
            }
        }


        dialogView.findViewById<TextView>(R.id.btnAccept).setOnClickListener {
            fetchSharePlaylist { playlists ->
                listOfPlaylists = playlists
                savePlaylistsToPreferences(playlists)
                displaySharedPlaylist(listOfPlaylists)
                Log.d("SharedPlaylists", "$playlists" + "1")
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "SharedPlaylists") {
//            playSelectedSongIfAvailable()

        }
    }


}