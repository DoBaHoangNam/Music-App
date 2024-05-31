package com.example.musicapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song

class SongViewModel : ViewModel() {
    val songList: MutableLiveData<MutableList<Song>> by lazy {
        MutableLiveData<MutableList<Song>>()
    }
    val albumList: MutableLiveData<MutableList<Album>> by lazy {
        MutableLiveData<MutableList<Album>>()
    }

    val artistList: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>()
    }

    val selectedSong: MutableLiveData<Song> by lazy {
        MutableLiveData<Song>()
    }

    private val _isFromSearchActivity = MutableLiveData<Boolean>()
    val isFromSearchActivity: LiveData<Boolean> = _isFromSearchActivity

    fun clearSourceFlag() {
        _isFromSearchActivity.value = false
    }
}
