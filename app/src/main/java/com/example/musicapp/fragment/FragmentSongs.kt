package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.ActivitySettings
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentSongsBinding
import com.example.musicapp.model.Song


class FragmentSongs : Fragment() {
    private lateinit var binding: FragmentSongsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongsBinding.inflate(inflater, container, false)
        displaySong()
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

    private fun displaySong() {
        binding.recvSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SongAdapter(getListSong())
        binding.recvSong.adapter = adapter
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