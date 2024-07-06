package com.example.musicapp

import com.example.musicapp.model.Song
import com.example.musicapp.ui.MainActivity

interface MediaPlayerControl {
    fun checkPlaySong(song: Song, source: MainActivity.SongSource)
    fun playPauseSong()
    fun stopSong()
    fun setPlaybackSpeed()
    fun setPlaybackPitch()
}