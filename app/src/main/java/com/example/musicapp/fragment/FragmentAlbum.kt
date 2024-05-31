package com.example.musicapp.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.ui.ActivitySearch

import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.AlbumAdapter
import com.example.musicapp.databinding.FragmentAlbumBinding
import com.example.musicapp.model.Album
import com.example.musicapp.model.Song
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class FragmentAlbum : Fragment() {
    private lateinit var binding: FragmentAlbumBinding
    private var mediaPlayerControl: MediaPlayerControl? = null
    private var albumList: MutableList<Album> = mutableListOf()
    private val songViewModel: SongViewModel by activityViewModels()
    private var songs: MutableList<Song> = mutableListOf()

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
            albumList = it.getParcelableArrayList<Album>("album_list")?.toMutableList()
                ?: mutableListOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)

        songViewModel.albumList.observe(viewLifecycleOwner) { albumList ->
            displayAlbum(albumList)
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

    private fun displayAlbum(albumList: MutableList<Album>) {
        val adapter = AlbumAdapter(albumList,findNavController())
        binding.recvAlbum.adapter = adapter
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.STRETCH
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP

        }

        binding.recvAlbum.layoutManager = layoutManager
        Log.d("check_source", albumList.size.toString() + "3")
    }


}