package com.example.musicapp.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.DataHolder
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.adapter.PlaylistAdapter
import com.example.musicapp.databinding.FragmentPlaylistBinding
import com.example.musicapp.model.Playlist
import com.example.musicapp.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class FragmentPlaylist : Fragment() {
    private lateinit var binding: FragmentPlaylistBinding
    private val songViewModel: SongViewModel by activityViewModels()
    private var songs: MutableList<Song> = mutableListOf()
    private var listOfPlaylists = mutableListOf<Playlist>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        fetchPlaylists{playlists ->
            listOfPlaylists = playlists
            displayPlaylist(listOfPlaylists)
            Log.d("FragmentPlaylist", "$listOfPlaylists" + "1")
        }


        binding.icSearch.setOnClickListener {
            songViewModel.songList.observe(viewLifecycleOwner) { songList ->
                songs = songList
            }
            val intent = Intent(requireContext(), ActivitySearch::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(songs))
            }
            startActivity(intent)
        }

        binding.icSetting.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.icSetting)

            popupMenu.menuInflater.inflate(R.menu.option1, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.setting -> {
                        val intent = Intent(requireContext(), ActivitySettings::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
        var listSongPlayed = DataHolder.playedSongs
        var listSongFavorite = DataHolder.favouriteSongs
        var mostPlayedSongs = DataHolder.mostPlayedSongs

        binding.tvNumberOfSongRecent.text = listSongPlayed.size.toString() + " tracks"
        binding.tvNumberOfSongFavorite.text = listSongFavorite.size.toString() + " tracks"
        binding.tvNumberOfSongMostPlayed.text = mostPlayedSongs.size.toString() + " tracks"

        binding.playlistRecentPlayed.setOnClickListener {
            val namePlaylist = "Recent Played"
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(listSongPlayed))
                putString("namePlaylist", namePlaylist)
            }
            findNavController().navigate(
                R.id.action_fragmentPlaylist_to_fragmentPlaylistSpecific,
                bundle
            )
        }

        binding.playlistFavorite.setOnClickListener {
            val namePlaylist = "Favourite"
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(listSongFavorite))
                putString("namePlaylist", namePlaylist)
            }
            findNavController().navigate(
                R.id.action_fragmentPlaylist_to_fragmentPlaylistSpecific,
                bundle
            )
        }

        binding.playlistMostPlayed.setOnClickListener {
            val namePlaylist = "Most Played"
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(mostPlayedSongs))
                putString("namePlaylist", namePlaylist)
            }
            findNavController().navigate(
                R.id.action_fragmentPlaylist_to_fragmentPlaylistSpecific,
                bundle
            )
        }

        binding.icAdd.setOnClickListener {
            showCreatePlaylistDialog()
        }

        return binding.root
    }

    private fun displayPlaylist(listOfPlaylists: MutableList<Playlist>) {
        binding.recvPlaylist.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = PlaylistAdapter(listOfPlaylists,findNavController(), requireContext())
        Log.d("FragmentPlaylist", "$listOfPlaylists" + "2")
        binding.recvPlaylist.adapter = adapter
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_playlist, null)
        val dialog = AlertDialog.Builder(requireContext())
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
                Toast.makeText(requireContext(), "Please enter a playlist name", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Playlist \"$name\" created", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to create playlist", Toast.LENGTH_SHORT).show()
        }

        Log.d("FragmentPlaylist", "$listOfPlaylists" + "3")
        Toast.makeText(requireContext(), "Playlist \"$name\" created", Toast.LENGTH_SHORT).show()
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