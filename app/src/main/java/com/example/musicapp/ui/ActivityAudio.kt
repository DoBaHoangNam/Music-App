package com.example.musicapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }
}