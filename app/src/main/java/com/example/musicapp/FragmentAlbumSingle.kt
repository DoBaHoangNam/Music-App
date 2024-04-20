package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.adapter.SongInAlbumAdapter
import com.example.musicapp.databinding.FragmentAlbumSingleBinding
import com.example.musicapp.model.Song
import com.example.musicapp.model.SongInAlbum
import com.example.musicapp.ui.ActivitySearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FragmentAlbumSingle : Fragment() {
    private lateinit var binding: FragmentAlbumSingleBinding
    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                fragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun displaySong() {
        binding.recvSongs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SongInAlbumAdapter(getListSong())
        binding.recvSongs.adapter = adapter
    }

    private fun getListSong(): MutableList<SongInAlbum> {
        val list = mutableListOf<SongInAlbum>()
        list.add(SongInAlbum("Tây Bắc Thả Chiều Vào Tranh ","5:06"))
        list.add(SongInAlbum("Tây Bắc Thả Chiều Vào Tranh ","5:06"))
        list.add(SongInAlbum("Tây Bắc Thả Chiều Vào Tranh ","5:06"))
        list.add(SongInAlbum("Tây Bắc Thả Chiều Vào Tranh ","5:06"))
        list.add(SongInAlbum("Tây Bắc Thả Chiều Vào Tranh ","5:06"))
        list.add(SongInAlbum("Tây Bắc Thả Chiều Vào Tranh ","5:06"))
        return list

    }


}