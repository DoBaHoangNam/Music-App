package com.example.musicapp.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.R
import com.example.musicapp.adapter.SongInAlbumAdapter
import com.example.musicapp.databinding.FragmentArtistSingleBinding
import com.example.musicapp.model.Song
import com.example.musicapp.model.SongInAlbum
import com.example.musicapp.ui.ActivitySearch

class FragmentArtistSingle : Fragment() {
    private lateinit var binding: FragmentArtistSingleBinding
    private lateinit var fragmentManager: FragmentManager
    private var mediaPlayerControl: MediaPlayerControl? = null

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
        displaySong()

        fragmentManager = requireActivity().supportFragmentManager
        binding.btnBack.setOnClickListener {
            fragmentManager.popBackStack()
        }
        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java)
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

    private fun displaySong() {
//        binding.recvSongs.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        val adapter = SongInAlbumAdapter(requireContext(),getListSong())
//        binding.recvSongs.adapter = adapter
    }

    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        return list

    }

}