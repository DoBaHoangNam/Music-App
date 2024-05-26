package com.example.musicapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicapp.databinding.ActivitySettingsBinding
import com.example.musicapp.ui.ActivityLoginSignin
import com.example.musicapp.ui.MainActivity

class ActivitySettings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)


        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.profile.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)
        }

        binding.about.setOnClickListener {
            val intent = Intent(this, ActivityAbout::class.java)
            startActivity(intent)
        }
        binding.audio.setOnClickListener {
            val intent = Intent(this, ActivityAudio::class.java)
            startActivity(intent)
        }

        binding.logout.setOnClickListener {
            val intent = Intent(this, ActivityLoginSignin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // Đặt các cờ để xóa hết tất cả các Activity trước đó
            val editor = sharedPreferences.edit()
            editor.putBoolean("logged_in", false)
            editor.apply()
            startActivity(intent)
            finish() // Kết thúc Activity hiện tại
        }

    }
}