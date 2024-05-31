package com.example.musicapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.OnRecentSearchItemClickListener
import com.example.musicapp.RecentSearchListener
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.RecentSearchAdapter
import com.example.musicapp.adapter.SongSearchAdapter
import com.example.musicapp.databinding.ActivitySearchBinding
import com.example.musicapp.model.RecentSearch
import com.example.musicapp.model.Song
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class ActivitySearch : AppCompatActivity(), OnRecentSearchItemClickListener {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SongSearchAdapter
    private lateinit var adapter2: RecentSearchAdapter
    private lateinit var originalListSong: MutableList<Song>
    private val filterListSong = mutableListOf<Song>()
    private val recentSearch = mutableListOf<RecentSearch>()
    private val PREF_NAME = "MyPrefs"
    private val KEY_RECENT_SEARCH = "recent_search"
    private val KEY_SELECTED_SONG = "selected_song"
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        recentSearch.addAll(getRecentSearchFromSharedPreferences())


        binding.recvSearchList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongSearchAdapter(this, filterListSong) { song ->
            playSong(song)
        }
        binding.recvSearchList.adapter = adapter

        val songListFromIntent = intent.getParcelableArrayListExtra<Song>("song_list")
        Log.d("check_source", songListFromIntent?.size.toString() + " songList from intent")

        originalListSong = songListFromIntent?.toMutableList() ?: mutableListOf()

        // Log the size of the originalListSong to check if it was retrieved correctly
        Log.d("check_source", originalListSong.size.toString() + " originalListSongSearch")



        setupSearchView()
        showRecent()


        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun getRecentSearchFromSharedPreferences(): MutableList<RecentSearch> {
        val json = sharedPreferences.getString(KEY_RECENT_SEARCH, null)
        val type = object : TypeToken<MutableList<RecentSearch>>() {}.type
        return Gson().fromJson(json, type) ?: mutableListOf()
    }

    private fun saveRecentSearchToSharedPreferences(recentSearchList: MutableList<RecentSearch>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(recentSearchList)
        editor.putString(KEY_RECENT_SEARCH, json)
        editor.apply()
    }

    private fun showRecent() {
        binding.headingOfRecTV.text = "Recent Searches"
        binding.recvRecentList.visibility = View.VISIBLE
        binding.recvSearchList.visibility = View.INVISIBLE

        binding.recvRecentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter2 = RecentSearchAdapter(recentSearch, this, object : RecentSearchListener {
            override fun saveRecentSearch(recentSearchList: MutableList<RecentSearch>) {
                saveRecentSearchToSharedPreferences(recentSearchList)
            }
        })
        binding.recvRecentList.adapter = adapter2

    }

    private fun addToRecentSearchIfNotExist(query: String) {
        if (!recentSearch.any { it.content == query }) {
            recentSearch.add(RecentSearch(query))
            adapter2.notifyDataSetChanged()
            saveRecentSearchToSharedPreferences(recentSearch)
        }
    }


    private fun setupSearchView() {
        binding.searchEdt.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterSongItems(query)
                addToRecentSearchIfNotExist(query.trim())
                adapter2.notifyDataSetChanged()
                saveRecentSearchToSharedPreferences(recentSearch)
                if (query.isEmpty()) {
                    showRecent()
                }
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterSongItems(query)
                binding.headingOfRecTV.text = "Tracks"

                if (query.isEmpty()) {
                    showRecent()
                }
                return true
            }
        })
    }


    private fun filterSongItems(query: String) {
        binding.recvRecentList.visibility = View.INVISIBLE
        binding.recvSearchList.visibility = View.VISIBLE

        filterListSong.clear()

        originalListSong.forEach { item ->
            if (item.songName?.contains(
                    query,
                    ignoreCase = true
                ) == true || item.singerName?.contains(query, ignoreCase = true) == true
            ) {
                filterListSong.add(item)
            }
        }

        adapter.notifyDataSetChanged()
    }


    private fun playSong(song: Song) {
        saveSelectedSongToSharedPreferences(song.songName)
        finish()
    }

    private fun saveSelectedSongToSharedPreferences(songName: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SELECTED_SONG, songName)
        editor.apply()
    }


    override fun onItemClick(searchContent: String) {
        binding.searchEdt.setQuery(searchContent, true)
    }
}