package com.example.musicapp.fragment

import com.example.musicapp.SongViewModel
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentSongsBinding
import com.example.musicapp.model.Song
import com.example.musicapp.ui.ActivitySearch


class FragmentSongs : Fragment(),SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: FragmentSongsBinding
    private var mediaPlayerControl: MediaPlayerControl? = null
    private var songs: MutableList<Song> = mutableListOf()
    private val songViewModel: SongViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private var currentSongIndex = 0
    private lateinit var adapter: SongAdapter

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
            songs = it.getParcelableArrayList<Song>("song_list")?.toMutableList()
                ?: mutableListOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongsBinding.inflate(inflater, container, false)

        songViewModel.songList.observe(viewLifecycleOwner) { songList ->
            displaySong(songList)
            songs = songList
        }

        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(songs))
            }
            startActivity(intent)
        }


        binding.icSetting.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.icSetting)

            popupMenu.menuInflater.inflate(R.menu.option, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.setting -> {
                        val intent = Intent(requireContext(), ActivitySettings::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.edit -> {
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }



        return binding.root


    }

    private fun displaySong(songList: MutableList<Song>) {
        binding.recvSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        currentSongIndex = sharedPreferences.getInt("currentSongIndex", 0)

        adapter = SongAdapter(requireContext(), songList) { song ->
            mediaPlayerControl?.playSong(song)

        }
        binding.recvSong.adapter = adapter
        adapter.setSelectedItem(currentSongIndex)
        binding.recvSong.scrollToPosition(currentSongIndex)
    }


    companion object {
        fun newInstance(songList: MutableList<Song>): FragmentSongs {
            val fragment = FragmentSongs()
            val args = Bundle()
            args.putParcelableArrayList("song_list", ArrayList(songList))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "currentSongIndex") {
            if (sharedPreferences != null) {
                currentSongIndex = sharedPreferences.getInt("currentSongIndex", 0)
                adapter.setSelectedItem(currentSongIndex)
                binding.recvSong.scrollToPosition(currentSongIndex)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }




}