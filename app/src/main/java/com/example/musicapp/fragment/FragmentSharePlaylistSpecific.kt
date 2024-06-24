package com.example.musicapp.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.SongInSharePlaylistAdapter
import com.example.musicapp.databinding.FragmentPlaylistSpecificBinding
import com.example.musicapp.databinding.FragmentSharedPlaylistSpecificBinding
import com.example.musicapp.model.Song
import com.google.gson.Gson

class FragmentSharePlaylistSpecific : Fragment() {
    private lateinit var binding: FragmentSharedPlaylistSpecificBinding
    private lateinit var fragmentManager: FragmentManager
    private var mediaPlayerControl: MediaPlayerControl? = null
    private val songViewModel: SongViewModel by activityViewModels()
    private var songs: MutableList<Song> = mutableListOf()
    private var listSong: MutableList<String> = mutableListOf()
    private var listShareSong: MutableList<Song> = mutableListOf()
    private var playlistName: String = "Your Playlist"
    private var playlistId: String = ""
    private var currentSongIndex = 0
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private val KEY_SHARED_SONG = "share_song"


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MediaPlayerControl) {
            mediaPlayerControl = context
        } else {
            throw RuntimeException("$context must implement MediaPlayerControl")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mediaPlayerControl = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listSong = it.getStringArrayList("listSongInPlaylist") ?: arrayListOf()
            listShareSong = it.getParcelableArrayList<Song>("listShareSongInPlaylist") ?: arrayListOf()
            playlistName = it.getString("namePlaylist") ?: ""
            playlistId = it.getString("playlistId") ?: ""
        }
        Log.d("FragmentPlaylistSpecific", "Initial songs list: $listSong")
        Log.d("FragmentPlaylistSpecific", "Initial list share: $listShareSong")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSharedPlaylistSpecificBinding.inflate(inflater, container, false)
        displaySong(listShareSong)
        fragmentManager = requireActivity().supportFragmentManager
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        binding.btnBack.setOnClickListener {
            fragmentManager.popBackStack()

        }
        binding.tvPlaylistName.text = playlistName



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

    private fun displaySong(listSong: MutableList<Song>) {
        binding.recvSongInPlayList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        val adapter = SongInSharePlaylistAdapter(requireContext(),
            listSong, playlistId) { song ->
            playSong(song)
        }
        binding.recvSongInPlayList.adapter = adapter
    }

    private fun playSong(song: Song) {
        saveSelectedSongToSharedPreferences(song)
    }

    private fun saveSelectedSongToSharedPreferences(song: Song) {
        val gson = Gson()
        val songJson = gson.toJson(song) // Chuyển đổi đối tượng Song thành chuỗi JSON
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SHARED_SONG, songJson) // Lưu chuỗi JSON vào SharedPreferences
        editor.apply()
        Log.d("FragmentPlaylistSpecific", "saveSelectedSongToSharedPreferences: $song")
    }


}