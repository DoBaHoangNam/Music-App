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
import com.example.musicapp.databinding.FragmentArtistSingleBinding
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song
import com.example.musicapp.ui.ActivitySearch

class FragmentArtistSingle : Fragment() {
    private lateinit var binding: FragmentArtistSingleBinding
    private lateinit var fragmentManager: FragmentManager
    private var mediaPlayerControl: MediaPlayerControl? = null
    private var songs: MutableList<Song> = mutableListOf()
    private val songViewModel: SongViewModel by activityViewModels()
    private var artist: Artist? = null

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
        artist = arguments?.getParcelable("artist")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtistSingleBinding.inflate(inflater, container, false)

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

        displayArtistDetails()
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

    private fun displayArtistDetails() {
        artist?.let { artist ->
            binding.artistNameTv.text = artist.name
            binding.infoArtistTv.text = artist.numberOfTracks.toString() + " songs | " + countTotalTime(getListSong())
            Glide.with(this)
                .load(artist.image)
                .placeholder(R.mipmap.ic_song_round_high)
                .into(binding.albumImg)
        }
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
            artist?.let { artist ->
                list.addAll(songList.filter { song ->
                    song.singerName == artist.name
                })
            }
        }
        Log.d("FragmentArtistSingle", "Filtered songs list: $list")
        return list
    }

    private fun countTotalTime(listSong: MutableList<Song>): String {
        var sumTime = 0L
        for(song in listSong){
            sumTime += song.duration
        }
        return formatDuration(sumTime)
    }

    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


}