package com.example.musicapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.musicapp.databinding.BottomSheetNowPlayingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView

class BottomSheetNowPlaying : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetNowPlayingBinding
    private lateinit var imageView: CircleImageView
    private lateinit var playButton: TextView
    private var isRotating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetNowPlayingBinding.inflate(inflater, container, false)

        imageView = binding.imgSong


        binding.playBtn.setOnClickListener {
            Log.d("aaaaaaaaaaaaaaaaaa","aaaaaaaaaaaaaaaaaa")
            if (!isRotating) {
                startRotation()
                Toast.makeText(requireContext(), "Rotation started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Already rotating", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun startRotation() {
        isRotating = true
        val runnable: Runnable = object : Runnable {
            override fun run() {
                imageView.animate().rotationBy(360f).withEndAction(this).setDuration(3000)
                    .setInterpolator(LinearInterpolator()).start()
            }
        }

        imageView.animate().rotationBy(360f).withEndAction {
            isRotating = false
        }.setDuration(3000)
            .setInterpolator(LinearInterpolator()).start()
    }
}
