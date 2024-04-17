package com.example.musicapp

import com.example.musicapp.model.RecentSearch

interface RecentSearchListener {
    fun saveRecentSearch(recentSearchList: MutableList<RecentSearch>)
}