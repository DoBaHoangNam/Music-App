package com.example.musicapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var layout = findViewById<SlidingUpPanelLayout>(R.id.slidingUp)
        binding.bottomSheetNowPlaying.root.visibility = View.INVISIBLE



        layout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                binding.smallControlBar.alpha = 1 - slideOffset
                binding.bottomSheetNowPlaying.root.visibility = View.VISIBLE
                binding.bottomSheetNowPlaying.root.alpha = slideOffset

            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?,
            ) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED && previousState == SlidingUpPanelLayout.PanelState.EXPANDED ) {
                    binding.smallControlBar.visibility = View.VISIBLE


                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED && previousState == SlidingUpPanelLayout.PanelState.COLLAPSED ) {
                    binding.smallControlBar.visibility = View.INVISIBLE

                }
            }

        })

        var navController = findNavController(R.id.fragmentContainerView2)
        var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(navController)
    }
}