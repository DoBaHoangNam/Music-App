package com.example.musicapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivityGettingStartedBinding

class ActivityGettingStarted : AppCompatActivity() {

    private lateinit var binding: ActivityGettingStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGettingStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}