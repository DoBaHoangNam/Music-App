package com.example.musicapp

import com.example.musicapp.model.Song

interface MediaPlayerControl {
    fun playSong(song: Song)
    fun playSong(uri: String)
    fun playPauseSong()
    fun stopSong()
}