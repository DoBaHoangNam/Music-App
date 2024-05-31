package com.example.musicapp.fragment

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentPlaylistSpecificBinding
import com.example.musicapp.model.Song

class FragmentPlaylistSpecific : Fragment() {
    private lateinit var binding: FragmentPlaylistSpecificBinding
    private lateinit var fragmentManager: FragmentManager
    private var mediaPlayer: MediaPlayer? = null
    private val songViewModel: SongViewModel by activityViewModels()
    private var songs: MutableList<Song> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onResume() {
        super.onResume()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                fragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun displaySong() {
        binding.recvSongInPlayList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SongAdapter(requireContext(),getListSong()) { song ->
            playSong(song)
        }
        binding.recvSongInPlayList.adapter = adapter
    }

    private fun playSong(song: Song) {
        // Release any previously playing media player
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.data)
            prepare()
            start()
        }

        Toast.makeText(requireContext(), "Playing: ${song.songName}", Toast.LENGTH_SHORT).show()
    }

    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        return list

    }

}