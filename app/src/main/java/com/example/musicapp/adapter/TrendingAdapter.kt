package com.example.musicapp.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.model.Song
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson

class TrendingAdapter(
    private val items: MutableList<Song>,
    private val requireContext: Context

) :
    RecyclerView.Adapter<TrendingAdapter.ViewHolder>() {
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private val KEY_SHARED_SONG = "share_song"


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singerName: TextView = itemView.findViewById(R.id.singerTv)
        val image: ImageView = itemView.findViewById(R.id.imgAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrendingAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.singerName.text = currentItem.songName
        Glide.with(holder.itemView.context)
            .load(currentItem.image) // Load the image from the URL or URI
            .placeholder(R.mipmap.ic_song_round_high) // Placeholder image
            .into(holder.image)

        holder.itemView.setOnClickListener {
            getSong(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getSong(currentItem: Song) {
        val storage = FirebaseStorage.getInstance()
        val songRef = storage.getReferenceFromUrl(currentItem.data)
        songRef.downloadUrl.addOnSuccessListener { uri ->
            // Here you can handle the song URI if needed
            currentItem.data = uri.toString()  // Update the song data with the actual download URL
            saveSelectedSongToSharedPreferences(currentItem)
        }.addOnFailureListener { exception ->
            Log.e("DownloadAndUpdate", "Failed to retrieve song URL: ${exception.message}")
            // Handle the error appropriately
        }

    }

    private fun saveSelectedSongToSharedPreferences(song: Song) {
        sharedPreferences = requireContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val songJson = gson.toJson(song) // Chuyển đổi đối tượng Song thành chuỗi JSON
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SHARED_SONG, songJson) // Lưu chuỗi JSON vào SharedPreferences
        editor.apply()
        Log.d("FragmentPlaylistSpecific", "saveSelectedSongToSharedPreferences: $song")
    }
}