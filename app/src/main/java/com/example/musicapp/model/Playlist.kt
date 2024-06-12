package com.example.musicapp.model

data class Playlist(
    var id:String? = null,
    var playlistName:String? = null,
    var numberOfSong:String? = null,
    var songs: MutableList<String>? = null
){
    constructor() : this(null,null, null, null)
}
