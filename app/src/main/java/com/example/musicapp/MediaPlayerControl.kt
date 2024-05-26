package com.example.musicapp

import com.example.musicapp.model.Song

interface MediaPlayerControl {
    fun playSong(song: Song)
    fun playPauseSong()
    fun stopSong()
}