package com.example.musicapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicapp.R
import com.example.musicapp.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivitySplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        } else {
            loadMusicDataInBackground()
        }
    }

    private fun loadMusicDataInBackground() {
        val startTime = System.currentTimeMillis() // Thời điểm bắt đầu tải dữ liệu

        // Sử dụng coroutine để tải dữ liệu trong nền
        CoroutineScope(Dispatchers.IO).launch {
            val songList = getListSong()

            // Tính thời gian đã trôi qua kể từ khi bắt đầu tải dữ liệu
            val elapsedTime = System.currentTimeMillis() - startTime

            // Tính toán thời gian cần chờ để tổng thời gian là 3000ms
            val delayTime = if (elapsedTime >= 3000) {
                0L // Không cần chờ nữa nếu thời gian đã trôi qua lớn hơn hoặc bằng 3000ms
            } else {
                3000 - elapsedTime // Ngược lại, tính thời gian cần chờ để tổng thời gian là 3000ms
            }

            // Chuyển sang màn hình tiếp theo sau khi tải dữ liệu xong
            withContext(Dispatchers.Main) {
                navigateToNextScreen(songList, delayTime)
            }
        }
    }

    private fun navigateToNextScreen(songList: MutableList<Song>, delayTime: Long) {
        // Sử dụng Handler để chuyển sang màn hình tiếp theo sau một khoảng thời gian delayTime
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("logged_in", false)

            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java).apply {
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                }
            } else {
                Intent(this, ActivityGettingStarted::class.java)
            }
            startActivity(intent)
            finish()
        }, delayTime)
    }


    private fun getListSong(): MutableList<Song> {
        val songList = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val data = cursor.getString(dataColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val albumArtUri = getAlbumArtUri(albumId)
                val imageResId = albumArtUri ?: R.mipmap.ic_song_round.toString()

                val song = Song(id, title, artist, album, duration, data, imageResId)
                songList.add(song)
            }
        }
        return songList
    }

    private fun getAlbumArtUri(albumId: Long): String? {
        val albumArtUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID}=?"
        val selectionArgs = arrayOf(albumId.toString())

        val cursor = contentResolver.query(
            albumArtUri,
            projection,
            selection,
            selectionArgs,
            null
        )

        var albumArt: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                albumArt = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
            }
        }
        return albumArt
    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 100
    }
}