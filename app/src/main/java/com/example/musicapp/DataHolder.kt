package com.example.musicapp

import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song

object DataHolder {
    lateinit var songList: MutableList<Song>
    lateinit var albumList: MutableList<Album>
    lateinit var artistList: MutableList<Artist>
    var playedSongs: MutableList<String> = mutableListOf()
    var favouriteSongs: MutableList<String> = mutableListOf()
    var mostPlayedSongs: MutableList<String> = mutableListOf()
}