package com.example.musicapp

import android.content.Context
import android.content.SharedPreferences

object AcceptedSongsManager {
    private const val PREF_NAME = "AcceptedSongsPrefs"
    private const val ACCEPTED_SONGS_KEY = "AcceptedSongs"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isSongAccepted(context: Context, songId: String): Boolean {
        val acceptedSongs = getPreferences(context).getStringSet(ACCEPTED_SONGS_KEY, emptySet())
        return acceptedSongs?.contains(songId) ?: false
    }

    fun setSongAccepted(context: Context, songId: String) {
        val preferences = getPreferences(context)
        val acceptedSongs = preferences.getStringSet(ACCEPTED_SONGS_KEY, mutableSetOf())?.toMutableSet()
        acceptedSongs?.add(songId)
        preferences.edit().putStringSet(ACCEPTED_SONGS_KEY, acceptedSongs).apply()
    }
}
