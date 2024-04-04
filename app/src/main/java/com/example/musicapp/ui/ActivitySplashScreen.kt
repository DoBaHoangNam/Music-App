package com.example.musicapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.musicapp.R

class ActivitySplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("logged_in", false)

            if (isLoggedIn) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, ActivityGettingStarted::class.java)
                startActivity(intent)
                finish()
            }
        },3000)
    }
}