package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.ActivitySearch

import com.example.musicapp.R
import com.example.musicapp.adapter.AlbumAdapter
import com.example.musicapp.adapter.ArtistAdapter
import com.example.musicapp.databinding.FragmentAlbumBinding
import com.example.musicapp.model.Album
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class FragmentAlbum : Fragment() {
    private lateinit var binding: FragmentAlbumBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)
        displayAlbum()
        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun displayAlbum() {
        val adapter = AlbumAdapter(getListAlbum())
        binding.recvAlbum.adapter = adapter
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.STRETCH
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP

        }

        binding.recvAlbum.layoutManager = layoutManager
    }

    private fun getListAlbum(): MutableList<Album> {
        val list = mutableListOf<Album>()
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Album("Sèn Hoàng Mỹ Lam", R.drawable.img_song))

        return list

    }

}