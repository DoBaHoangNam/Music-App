package com.example.musicapp.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.SongInPlaylistAdapter
import com.example.musicapp.databinding.FragmentPlaylistSpecificBinding
import com.example.musicapp.model.Song

class FragmentPlaylistSpecific : Fragment() {
    private lateinit var binding: FragmentPlaylistSpecificBinding
    private lateinit var fragmentManager: FragmentManager
    private var mediaPlayerControl: MediaPlayerControl? = null
    private val songViewModel: SongViewModel by activityViewModels()
    private var songs: MutableList<Song> = mutableListOf()
    private var listSong: MutableList<String> = mutableListOf()
    private var playlistName: String = "Your Playlist"
    private var playlistId: String = ""
    private var currentSongIndex = 0
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private val KEY_SELECTED_SONG = "selected_song"

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
            playlistName = it.getString("namePlaylist") ?: ""
            playlistId = it.getString("playlistId") ?: ""
        }
        Log.d("FragmentPlaylistSpecific", "Initial songs list: $listSong")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlaylistSpecificBinding.inflate(inflater, container, false)
        displaySong()
        fragmentManager = requireActivity().supportFragmentManager
        binding.btnBack.setOnClickListener {
            fragmentManager.popBackStack()

        }
        binding.tvPlaylistName.text = playlistName

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
        Log.d("FragmentPlaylistSpecific", "Received song list: $listSong")
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

    private fun displaySong() {
        binding.recvSongInPlayList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SongInPlaylistAdapter(requireContext(), getListSong(), playlistId) { song ->
            mediaPlayerControl?.playSong(song)
        }
        binding.recvSongInPlayList.adapter = adapter


    }

    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        val songList = songViewModel.songList.value
        Log.d("FragmentPlaylistSpecific", " songs list: $songList")
        Log.d("FragmentPlaylistSpecific", " songs list name : $listSong")

        if (songList != null) {
            for (songName in listSong) {
                val matchingSongs = songList.filter { song ->
                    song.songName == songName
                }
                list.addAll(matchingSongs)
            }
        }

        Log.d("FragmentPlaylistSpecific", "Filtered songs list: $list")
        return list.reversed().toMutableList()
    }




}