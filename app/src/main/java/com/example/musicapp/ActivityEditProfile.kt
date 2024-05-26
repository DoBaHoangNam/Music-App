package com.example.musicapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicapp.databinding.ActivityEditProfileBinding

class ActivityEditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdate.setOnClickListener {
            finish()
        }

        setContentView(binding.root)
    }
}