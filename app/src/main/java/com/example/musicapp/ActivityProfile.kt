package com.example.musicapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicapp.databinding.ActivityProfileBinding

class ActivityProfile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)

        binding.editProfile.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)

        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.resetPwd.setOnClickListener {
            val intent = Intent(this, ActivityResetPassword::class.java)
            startActivity(intent)
        }

        setContentView(binding.root)
    }
}