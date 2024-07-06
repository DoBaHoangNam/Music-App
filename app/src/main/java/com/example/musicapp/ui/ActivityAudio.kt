package com.example.musicapp.ui

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivityAudioBinding

class ActivityAudio : AppCompatActivity() {
    private lateinit var binding: ActivityAudioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val initialSpeed = sharedPreferences.getFloat("playback_speed", 1.0f)
        val initialProgress = ((initialSpeed - 0.5f) / 0.1f).toInt() // Calculate initial progress
        binding.changeSpeedSeekbar.progress = initialProgress
        binding.tvValueSpeed.text = String.format("%.1fx", initialSpeed)

        binding.changeSpeedSeekbar.max = 15 // Set max value to 15 to cover range 0.5x to 2.0x in 0.1x increments

        binding.changeSpeedSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Map progress (0 to 15) to speed (0.5x to 2.0x) in 0.1x increments
                val speed = 0.5f + (progress * 0.1f)
                binding.tvValueSpeed.text = String.format("%.1fx", speed)

                // Save the speed to SharedPreferences
                with(sharedPreferences.edit()) {
                    putFloat("playback_speed", speed)
                    apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Handle start of touch event
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Handle end of touch event
            }
        })

        val initialPitch = sharedPreferences.getInt("playback_pitch", 0)
        val initialPitchProgress = initialPitch + 5
        binding.changePitchSeekbar.progress = initialPitchProgress
        binding.tvValuePitch.text = String.format("%d semitones", initialPitch)
        binding.changePitchSeekbar.max = 12

        binding.changePitchSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pitch = progress - 6
                with(sharedPreferences.edit()) {
                    putInt("playback_pitch", pitch)
                    apply()
                }
                binding.tvValuePitch.text = String.format("%d semitones", pitch)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        // Reset the speed to 1.0x when exiting the activity
        val sharedPreferences = getSharedPreferences("MusicAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("playback_speed", 1.0f)
            apply()
        }
        with(sharedPreferences.edit()) {
            putInt("playback_pitch", 0)
            apply()
        }
    }

}
