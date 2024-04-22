package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.ActivitySettings
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.R
import com.example.musicapp.adapter.AlbumAdapter
import com.example.musicapp.databinding.FragmentForYouBinding
import com.example.musicapp.model.Album
import java.util.Calendar

class FragmentForYou : Fragment() {

    private lateinit var binding: FragmentForYouBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentForYouBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val calendar: Calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour in 5..12){
            binding.welcomeTv.text = "Good Morning"
            binding.statusLoti.setAnimation(R.raw.sun_loti)
        }else if (hour in 13..18){
            binding.welcomeTv.text = "Good Afternoon"
            binding.statusLoti.setAnimation(R.raw.sun_loti)
        }else{
            binding.welcomeTv.text = "Good Night"
            binding.statusLoti.setAnimation(R.raw.moon_loti)
        }


        displayAlbum()
        binding.icSearch.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySearch::class.java)
            startActivity(intent)
        }

        binding.icSetting.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySettings::class.java)
            startActivity(intent)
        }

        return binding.root


    }

    private fun displayAlbum() {
        binding.recvSuggestion.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = AlbumAdapter(getListAlbum(),findNavController())
        binding.recvSuggestion.adapter = adapter
    }

    private fun getListAlbum(): MutableList<Album> {
        val list = mutableListOf<Album>()
        list.add(Album("Son Tung MTP", R.drawable.album_image))
        list.add(Album("Son Tung MTP", R.drawable.album_image))
        list.add(Album("Son Tung MTP", R.drawable.album_image))
        list.add(Album("Son Tung MTP", R.drawable.album_image))
        return list
    }
}