package com.example.musicapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.adapter.PlaylistAdapter
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentPlaylistBinding
import com.example.musicapp.databinding.FragmentSongsBinding
import com.example.musicapp.model.Playlist
import com.example.musicapp.model.Song


class FragmentPlaylist : Fragment() {
    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        displayPlaylist()
        return binding.root
    }

    private fun displayPlaylist() {
        binding.recvPlaylist.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = PlaylistAdapter(getListPlaylist())
        binding.recvPlaylist.adapter = adapter
    }

    private fun getListPlaylist(): MutableList<Playlist> {
        val list = mutableListOf<Playlist>()
        list.add(Playlist("Recently added","100 Songs"))
        list.add(Playlist("Favorites","100 Songs"))
        list.add(Playlist("Most played","100 Songs"))
        list.add(Playlist("Recently played","100 Songs"))
        list.add(Playlist("Recent add","100 Songs"))

        return list

    }


}