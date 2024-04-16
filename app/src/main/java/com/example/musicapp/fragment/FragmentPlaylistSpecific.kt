package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentForYouBinding
import com.example.musicapp.databinding.FragmentPlaylistSpecificBinding
import com.example.musicapp.model.Song

class FragmentPlaylistSpecific : Fragment() {
    private lateinit var binding: FragmentPlaylistSpecificBinding


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
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentPlaylistSpecific_to_fragmentPlaylist)
        }
        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    private fun displaySong() {
        binding.recvSongInPlayList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SongAdapter(getListSong())
        binding.recvSongInPlayList.adapter = adapter
    }

    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ","Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        return list

    }

}