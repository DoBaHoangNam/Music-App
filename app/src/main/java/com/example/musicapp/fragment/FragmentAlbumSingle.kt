package com.example.musicapp.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.SongInAlbumAdapter
import com.example.musicapp.databinding.FragmentAlbumSingleBinding
import com.example.musicapp.model.Album
import com.example.musicapp.model.Song
import com.example.musicapp.ui.ActivitySearch


class FragmentAlbumSingle : Fragment() {
    private lateinit var binding: FragmentAlbumSingleBinding
    private lateinit var fragmentManager: FragmentManager
    private var album: Album? = null
    private var mediaPlayerControl: MediaPlayerControl? = null
    private var songs: MutableList<Song> = mutableListOf()
    private val songViewModel: SongViewModel by activityViewModels()


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
        album = arguments?.getParcelable("album")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumSingleBinding.inflate(inflater, container, false)

        var isFavorite = false

        binding.icFavorite.setOnClickListener {
            if (isFavorite) {
                binding.icFavorite.setImageResource(R.drawable.baseline_favorite_border_blue_24)
            } else {
                binding.icFavorite.setImageResource(R.drawable.baseline_favorite_red_24)
            }
            isFavorite = !isFavorite
        }

        songViewModel.songList.observe(viewLifecycleOwner) { songList ->
            songs = songList
            Log.d("FragmentArtistSingle", " songs list2: $songs")
        }

        displayAlbumDetails()
        displaySong()

        fragmentManager = requireActivity().supportFragmentManager
        binding.btnBack.setOnClickListener {
            fragmentManager.popBackStack()
        }
        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(songs))
            }
            startActivity(intent)
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
    private fun displayAlbumDetails() {
        album?.let { album ->
            // Hiển thị thông tin album
            binding.albumNameTv.text = album.name
            binding.singerNameTv.text = album.artist
            Glide.with(this)
                .load(album.albumArt)
                .placeholder(R.mipmap.ic_song_round_high)
                .into(binding.albumImg)
            binding.infoAlbumTv.text = album.numberOfSongs.toString() + " songs"
        }
    }


    private fun displaySong() {
        binding.recvSongs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val adapter = SongInAlbumAdapter(requireContext(),getListSong()){ song ->
            mediaPlayerControl?.playSong(song)

        }
        binding.icPLayAll.setOnClickListener {
            if (getListSong().isNotEmpty()) {
                val firstSong = getListSong()[0]
                mediaPlayerControl?.playSong(firstSong)
                adapter.setSelectedItem(0)  // Highlight the first item in the adapter
            }
        }

        binding.icShuffle.setOnClickListener {
            val songList = getListSong()
            if (songList.isNotEmpty()) {
                val randomIndex = (0 until songList.size).random()
                val randomSong = songList[randomIndex]
                mediaPlayerControl?.playSong(randomSong)
                adapter.setSelectedItem(randomIndex)  // Highlight the randomly selected item in the adapter
            }
        }

        binding.recvSongs.adapter = adapter
    }

    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        var songList = songViewModel.songList.value
        Log.d("FragmentArtistSingle", " songs list: $songList")
        if (songList != null) {
            album?.let { album ->
                list.addAll(songList.filter { song ->
                    song.album == album.name
                })
            }
        }
        Log.d("FragmentArtistSingle", "Filtered songs list: $list")
        return list

    }


}