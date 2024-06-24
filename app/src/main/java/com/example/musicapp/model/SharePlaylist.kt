package com.example.musicapp.model

data class SharePlaylist(
    var id:String? = null,
    var playlistName:String? = null,
    var numberOfSong:String? = null,
    var songs: MutableList<Song>? = null
)
