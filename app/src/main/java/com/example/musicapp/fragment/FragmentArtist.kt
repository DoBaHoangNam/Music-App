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
import com.example.musicapp.adapter.ArtistAdapter
import com.example.musicapp.databinding.FragmentArtistBinding
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class FragmentArtist : Fragment() {
    private lateinit var binding: FragmentArtistBinding
    private var mediaPlayerControl: MediaPlayerControl? = null
    private var artistList: MutableList<Artist> = mutableListOf()
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
            artistList = it.getParcelableArrayList<Artist>("artist_list")?.toMutableList()
                ?: mutableListOf()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtistBinding.inflate(inflater, container, false)

        songViewModel.artistList.observe(viewLifecycleOwner) { artistList ->
            displayAlbum(artistList)
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

    private fun displayAlbum(artistList: MutableList<Artist>) {
//        binding.recvArtist.layoutManager =
//            GridLayoutManager(requireContext(),3)
        val adapter = ArtistAdapter(artistList, findNavController())
        binding.recvArtist.adapter = adapter
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_BETWEEN
            alignItems = AlignItems.STRETCH
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP

        }

        binding.recvArtist.layoutManager = layoutManager
        Log.d("check_source", artistList.size.toString() + "artist3")
    }



}