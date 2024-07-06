package com.example.musicapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicapp.DataHolder
import com.example.musicapp.R
import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song
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
        var isLoggedIn = sharedPreferences.getBoolean("logged_in", false)

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            // Kiểm tra nếu quyền đã được cấp
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, tải dữ liệu
                loadMusicDataInBackground()
            } else {
                // Quyền không được cấp, có thể hiển thị thông báo cho người dùng
                showPermissionDeniedMessage()
            }
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

            // Kiểm tra thư mục data=/storage/emulated/0/Android/data/com.example.musicapp/files/Music/
            val additionalSongs = checkAdditionalSongs()

            // Thêm các bài hát từ thư mục trên vào songList
            songList.addAll(additionalSongs)


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
            DataHolder.songList = songList
            DataHolder.albumList = albumList
            DataHolder.artistList = artistList

            val intent = Intent(this, MainActivity::class.java)
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
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DATA} NOT LIKE ? AND ${MediaStore.Audio.Media.DATA} NOT LIKE ? AND ${MediaStore.Audio.Media.TITLE} NOT LIKE ?"
        val selectionArgs = arrayOf("%recording%", "%ringtone%", "VOICE_%")
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
            selection = selection,
            selectionArgs = selectionArgs,
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        ) { cursor ->
            val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
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
            val imageResId = albumArtUri ?: R.drawable.ic_song_foreground.toString()

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
            val albumName =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
            val artist =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
            val numberOfSongs =
                cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
            val albumArtUri = getAlbumArtUri(id)
            val imageResId = albumArtUri ?: R.drawable.ic_song_foreground.toString()

            Album(id, albumName, artist, numberOfSongs, imageResId)
        }
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
            val imageResId = getAlbumArtUri(id) ?: R.drawable.ic_song_foreground.toString()

            Artist(id, name, numberOfAlbums, numberOfTracks, imageResId)
        }

    }

    private inline fun <T> queryMedia(
        uri: android.net.Uri,
        projection: Array<String>,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String,
        map: (android.database.Cursor) -> T
    ): MutableList<T> {
        val list = mutableListOf<T>()
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            while (it.moveToNext()) {
                list.add(map(it))
            }
        }
        return list
    }

    private fun checkAdditionalSongs(): List<Song> {
        val additionalSongs = mutableListOf<Song>()
        val musicDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)

        if (musicDir != null) {
            musicDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "mp3") {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(file.path)

                    val artist =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    val durationStr =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val image =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE)
                    val duration = durationStr?.toLong() ?: 0L

                    retriever.release()

                    val song = Song(
                        id = "0",
                        songName = file.nameWithoutExtension,
                        singerName = artist ?: "Unknown",
                        album = album ?: "Unknown",  // You may need to retrieve album info as well
                        duration = duration,
                        data = file.path,
                        image = image ?: R.drawable.ic_song_foreground.toString()
                    )
                    additionalSongs.add(song)
                }
            }
        }

        return additionalSongs
    }


    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 100
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Permission denied. Unable to load music data.", Toast.LENGTH_SHORT)
            .show()
    }


}