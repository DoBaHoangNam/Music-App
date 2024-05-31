package com.example.musicapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicapp.R
import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song
import com.google.gson.internal.`$Gson$Types`
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ActivitySplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        // Kiểm tra trạng thái đăng nhập
        val sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("logged_in", false)

        if (isLoggedIn) {
            // Nếu đã đăng nhập, kiểm tra quyền truy cập và tải dữ liệu
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
        } else {
            // Nếu chưa đăng nhập, chuyển đến ActivityGettingStarted
            navigateToGettingStartedScreen()
        }
    }

    private fun loadMusicDataInBackground() {
        val startTime = System.currentTimeMillis() // Thời điểm bắt đầu tải dữ liệu

        // Sử dụng coroutine để tải dữ liệu trong nền
        CoroutineScope(Dispatchers.IO).launch {
            val songListDeferred = async { getListSong() }
            val albumListDeferred = async { getListAlbums() }
            val artistListDeferred = async { getListArtist() }

            val songList = songListDeferred.await()
            val albumList = albumListDeferred.await()
            val artistList = artistListDeferred.await()


            Log.d("check_source", albumList.size.toString() + " album")
            Log.d("check_source", songList.size.toString() + " song")
            Log.d("check_source", artistList.size.toString() + " artist")

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
                navigateToMainScreen(songList, albumList, artistList, delayTime)
            }
        }
    }

    private fun navigateToMainScreen(
        songList: MutableList<Song>,
        albumList: MutableList<Album>,
        artistList: MutableList<Artist>,
        delayTime: Long
    ) {
        // Sử dụng Handler để chuyển sang màn hình tiếp theo sau một khoảng thời gian delayTime
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences("login_state", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("logged_in", false)

            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java).apply {
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putParcelableArrayListExtra("album_list", ArrayList(albumList))
                    putParcelableArrayListExtra("artist_list", ArrayList(artistList))
                }
            } else {
                Intent(this, ActivityGettingStarted::class.java)
            }
            startActivity(intent)
            finish()
        }, delayTime)
    }

    private fun navigateToGettingStartedScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ActivityGettingStarted::class.java)
            startActivity(intent)
            finish()
        }, 3000)

    }


    private fun getListSong(): MutableList<Song> {
        return queryMedia(
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            ),
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        ) { cursor ->
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
            val artist =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
            val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
            val duration =
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
            val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
            val albumId =
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
            val albumArtUri = getAlbumArtUri(albumId)
            val imageResId = albumArtUri ?: R.mipmap.ic_song_round.toString()

            Song(id, title, artist, album, duration, data, imageResId)
        }
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

    private fun getListAlbums(): MutableList<Album> {
        return queryMedia(
            uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection = arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                MediaStore.Audio.Albums.ALBUM_ART
            ),
            sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"
        ) { cursor ->
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
            val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
            val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
            val numberOfSongs = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
            val albumArtUri = getAlbumArtUri(id)
            val imageResId = albumArtUri ?: R.mipmap.ic_song_round.toString()

            val songs = getSongsForAlbum(id)

            Album(id, albumName, artist, numberOfSongs, imageResId, songs)
        }
    }

    private fun getSongsForAlbum(albumId: Long): List<Song> {
        val songs = mutableListOf<Song>()
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            ),
            "${MediaStore.Audio.Media.ALBUM_ID}=?",
            arrayOf(albumId.toString()),
            MediaStore.Audio.Media.TITLE + " ASC"
        )

        if (cursor == null) {
            return songs
        }


        cursor.use {
            while (it.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumIdRetrieved = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val albumArtUri = getAlbumArtUri(albumIdRetrieved)
                val imageResId = albumArtUri ?: R.mipmap.ic_song_round.toString()

                val song = Song(id, title, artist, album, duration, data, imageResId)
                songs.add(song)
            }
        }


        return songs
    }


    private fun getListArtist(): MutableList<Artist> {
        return queryMedia(
            uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            projection = arrayOf(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS
            ),
            sortOrder = MediaStore.Audio.Albums.ARTIST + " ASC"
        ) { artistCursor ->
            val id =
                artistCursor.getLong(artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
            val name =
                artistCursor.getString(artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
            val numberOfAlbums =
                artistCursor.getInt(artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
            val numberOfTracks =
                artistCursor.getInt(artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
            val imageResId = getAlbumArtUri(id) ?: R.mipmap.ic_song_round.toString()

            Artist(id, name, numberOfAlbums, numberOfTracks, imageResId)
        }

    }

    private inline fun <T> queryMedia(
        uri: android.net.Uri,
        projection: Array<String>,
        sortOrder: String,
        map: (android.database.Cursor) -> T
    ): MutableList<T> {
        val list = mutableListOf<T>()
        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)
        cursor?.use {
            while (it.moveToNext()) {
                list.add(map(it))
            }
        }
        return list
    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 100
    }
}