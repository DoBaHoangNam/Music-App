package com.example.musicapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicapp.databinding.ActivityResetPasswordBinding

class ActivityResetPassword : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResetPasswordBinding.inflate(layoutInflater)

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnUpdate.setOnClickListener {
            finish()
        }

        setContentView(binding.root)
    }
}