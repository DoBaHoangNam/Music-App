package com.example.musicapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: CircleImageView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var layout = findViewById<SlidingUpPanelLayout>(R.id.slidingUp)
        binding.bottomSheetNowPlaying.visibility = View.INVISIBLE
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


        imageView = binding.imgSong

        binding.tvNameSongPlaying.isSelected = true


        layout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                binding.smallControlBar.alpha = 1 - slideOffset
                binding.bottomSheetNowPlaying.visibility = View.VISIBLE
                binding.bottomSheetNowPlaying.alpha = slideOffset
                val translationY = bottomNavigationView.height * slideOffset
                bottomNavigationView.translationY = translationY
                bottomNavigationView.animate().translationY(bottomNavigationView.height.toFloat())
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()
                startRotation()


            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?,
            ) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    bottomNavigationView.visibility = View.GONE

                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    bottomNavigationView.animate().translationY(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    bottomNavigationView.visibility = View.VISIBLE

                }
            }

        })

        var navController = findNavController(R.id.fragmentContainerView2)
        var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(navController)

        binding.icVolume.setOnClickListener {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        }


    }


    private fun startRotation() {
        object : Runnable {
            override fun run() {
                imageView.animate().rotationBy(360f).withEndAction(this).setDuration(10000)
                    .setInterpolator(LinearInterpolator()).start()
            }
        }

        imageView.animate().rotationBy(360f).withEndAction {
            startRotation()
        }.setDuration(10000)
            .setInterpolator(LinearInterpolator()).start()
    }
}