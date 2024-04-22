package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.ActivitySettings
import com.example.musicapp.R
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.adapter.PlaylistAdapter
import com.example.musicapp.databinding.FragmentPlaylistBinding
import com.example.musicapp.model.Playlist


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

        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java)
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

    private fun displayPlaylist() {
        binding.recvPlaylist.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = PlaylistAdapter(getListPlaylist(),findNavController())
        binding.recvPlaylist.adapter = adapter
    }

    private fun getListPlaylist(): MutableList<Playlist> {
        val list = mutableListOf<Playlist>()
        list.add(Playlist("Recently added", "100 Songs"))
        list.add(Playlist("Favorites", "100 Songs"))
        list.add(Playlist("Most played", "100 Songs"))
        list.add(Playlist("Recently played", "100 Songs"))
        list.add(Playlist("Recent add", "100 Songs"))

        return list

    }


}