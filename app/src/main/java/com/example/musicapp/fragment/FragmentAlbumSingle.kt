package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.adapter.SongInAlbumAdapter
import com.example.musicapp.databinding.FragmentAlbumSingleBinding
import com.example.musicapp.model.Album
import com.example.musicapp.model.Song
import com.example.musicapp.model.SongInAlbum
import com.example.musicapp.ui.ActivitySearch

class FragmentAlbumSingle : Fragment() {
    private lateinit var binding: FragmentAlbumSingleBinding
    private lateinit var fragmentManager: FragmentManager
    private var album: Album? = null

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

        displayAlbumDetails()
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

    private fun displayAlbumDetails() {
        album?.let { album ->
            // Hiển thị thông tin album
            binding.albumNameTv.text = album.name
            binding.singerNameTv.text = album.artist
            Glide.with(this)
                .load(album.albumArt)
                .placeholder(R.mipmap.ic_song_round)
                .into(binding.albumImg)
            binding.infoAlbumTv.text = album.numberOfSongs.toString() + " songs"
        }
    }


    private fun displaySong() {
        binding.recvSongs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SongInAlbumAdapter(getListSong())
        binding.recvSongs.adapter = adapter
    }

    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        album?.let {
            // Giả sử album có danh sách bài hát, bạn cần thay đổi mô hình dữ liệu phù hợp
            it.songs?.forEach { song ->
                list.add(Song(song.id, song.songName, song.singerName, song.album,song.duration, song.data, song.image))
            }
            Log.d("check_source",it.songs.toString() + "aaaaa")
        }
        return list

    }


}