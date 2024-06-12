package com.example.musicapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.SongSelectedAdapter
import com.example.musicapp.databinding.FragmentSelectTracksBinding
import com.example.musicapp.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FragmentSelectTracks : Fragment() {
    private lateinit var binding: FragmentSelectTracksBinding
    private val songViewModel: SongViewModel by activityViewModels()
    private lateinit var fragmentManager: FragmentManager
    private val selectedSongs = mutableListOf<String>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var adapter: SongSelectedAdapter
    private var playlistName: String = ""
    private var playlistId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistName = it.getString("playlist") ?: ""
            playlistId = it.getString("playlistId") ?: ""
        }
        Log.d("FragmentSelected", "name: $playlistName")
        Log.d("FragmentSelected", "id : $playlistId")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectTracksBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        fragmentManager = requireActivity().supportFragmentManager

        displaySong()

        binding.cbSelectSongAll.isChecked = false

        binding.cbSelectSongAll.isChecked = false

        binding.cbSelectSongAll.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAll(isChecked)
        }

        binding.tvDone.setOnClickListener {
            updatePlaylist(selectedSongs)
            findNavController().navigate(R.id.action_fragmentSelectTracks_to_fragmentPlaylist)
        }



        return binding.root
    }

    private fun updatePlaylist(selectedSongs: MutableList<String>) {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""
        val playlistsRef: DatabaseReference =
            database.reference.child("user").child(userId).child("playlists").child(playlistId)
                .child("songs")
        playlistsRef.setValue(selectedSongs)

    }

    private fun displaySong() {
        val songList = songViewModel.songList.value
        binding.recvSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter =
            SongSelectedAdapter(requireContext(), songList as MutableList<Song>, selectedSongs)
        binding.recvSong.adapter = adapter

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


}