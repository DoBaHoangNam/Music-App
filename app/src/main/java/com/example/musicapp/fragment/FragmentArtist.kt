package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.musicapp.ActivitySettings
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.adapter.ArtistAdapter
import com.example.musicapp.databinding.FragmentArtistBinding
import com.example.musicapp.model.Artist
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class FragmentArtist : Fragment() {
    private lateinit var binding: FragmentArtistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtistBinding.inflate(inflater, container, false)
        displayAlbum()
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

    private fun displayAlbum() {
//        binding.recvArtist.layoutManager =
//            GridLayoutManager(requireContext(),3)
        val adapter = ArtistAdapter(getListArtist(), findNavController())
        binding.recvArtist.adapter = adapter
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_BETWEEN
            alignItems = AlignItems.STRETCH
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP

        }

        binding.recvArtist.layoutManager = layoutManager
//            GridLayoutManager(requireContext(),3)
//        val adapter = ArtistAdapter(getListArtist())
    }

    private fun getListArtist(): MutableList<Artist> {
        val list = mutableListOf<Artist>()
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(Artist("Sơn Tung MTP", R.drawable.album_image))
        list.add(Artist("Sèn Hoàng Mỹ Lam", R.drawable.img_song))


        return list

    }


}