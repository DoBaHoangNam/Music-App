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
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.fragment.FragmentAlbum
import com.example.musicapp.fragment.FragmentArtist
import com.example.musicapp.fragment.FragmentForYou
import com.example.musicapp.fragment.FragmentPlaylist
import com.example.musicapp.fragment.FragmentPlaylistSpecific
import com.example.musicapp.fragment.FragmentSongs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: CircleImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var layout = findViewById<SlidingUpPanelLayout>(R.id.slidingUp)
        binding.bottomSheetNowPlaying.visibility = View.INVISIBLE
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


        imageView = binding.imgSong

        binding.tvNameSongPlaying.isSelected = true

        binding.btnCollapse.setOnClickListener {
            layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }


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
                val currentFragmentId = findNavController(R.id.fragmentContainerView2).currentDestination?.id
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    bottomNavigationView.visibility = View.GONE

                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    bottomNavigationView.animate().translationY(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()

                    if (currentFragmentId == R.id.fragmentPlaylistSpecific || currentFragmentId == R.id.fragmentAlbumSingle ) {
                        bottomNavigationView.visibility = View.GONE
                    }else{
                    bottomNavigationView.visibility = View.VISIBLE}

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


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentForYou, R.id.fragmentSongs, R.id.fragmentArtist, R.id.fragmentPlaylist, R.id.fragmentAlbum -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.fragmentPlaylistSpecific, R.id.fragmentAlbumSingle -> {
                    //bottomNavigationView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }

        var isFavorite = false
        binding.icAddToFavorite.setOnClickListener {

            if (isFavorite) {
                binding.icAddToFavorite.setImageResource(R.drawable.baseline_favorite_border_blue_24)
            } else {
                binding.icLove.visibility = View.VISIBLE
                binding.icAddToFavorite.setImageResource(R.drawable.baseline_favorite_24)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding.icLove.visibility = View.INVISIBLE
                }
            }
            isFavorite = !isFavorite
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