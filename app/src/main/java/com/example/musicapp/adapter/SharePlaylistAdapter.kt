package com.example.musicapp.adapter

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.AcceptedSongsManager
import com.example.musicapp.R
import com.example.musicapp.model.SharePlaylist
import com.example.musicapp.model.Song
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.io.File

class SharePlaylistAdapter(
    private val items: MutableList<SharePlaylist>,
    private val navController: NavController,
    private val context: Context
) : RecyclerView.Adapter<SharePlaylistAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        val numberOfSong: TextView = itemView.findViewById(R.id.tvNumberOfSong)
        val option: Button = itemView.findViewById(R.id.btnMoreOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharePlaylistAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SharePlaylistAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.playlistName.text = currentItem.playlistName
        currentItem.songs = currentItem.songs ?: mutableListOf()
        currentItem.numberOfSong = currentItem.songs?.size.toString() + " tracks"
        holder.numberOfSong.text = currentItem.numberOfSong

        holder.itemView.setOnClickListener {
            getSong(currentItem)
        }

        holder.option.setOnClickListener { view ->
            showPopupMenu(view, currentItem)
        }
    }

    private fun showPopupMenu(view: View, playlist: SharePlaylist) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.option_for_share_playlist, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.editPlaylist -> {
                    showEditPlaylistDialog(playlist)
                    true
                }
                R.id.delete -> {
                    deletePlaylist(items.indexOf(playlist), playlist.id as String)
                    true
                }
                R.id.clearSharedPreferences -> {
                    clearSharedPreferences()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun showEditPlaylistDialog(playlist: SharePlaylist) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_playlist, null)
        val editText = dialogView.findViewById<EditText>(R.id.etChangePlaylistName)
        editText.setText(playlist.playlistName)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)

        dialogView.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnChange).setOnClickListener {
            val newName = editText.text.toString()
            if (newName.isNotBlank()) {
                playlist.playlistName = newName
                updatePlaylistNameInDatabase(playlist.id as String, playlist)
                notifyItemChanged(items.indexOf(playlist))
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please enter a playlist name", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updatePlaylistNameInDatabase(id: String, playlist: SharePlaylist) {
        val database: DatabaseReference = database.reference.child("user").child(userId).child("sharedPlaylist")
        database.child(id).setValue(playlist)
    }

    private fun deletePlaylist(position: Int, id: String) {
        val database: DatabaseReference = database.reference.child("user").child(userId).child("sharedPlaylist")
        database.child(id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                items.removeAt(position)
                notifyItemRemoved(position)
                removePlaylistFromPreferences(id)
            }
        }
    }

    private fun getSong(currentItem: SharePlaylist) {
        val storage = FirebaseStorage.getInstance()
        val listShareSong = mutableListOf<Song>()

        currentItem.songs?.forEach { song ->
            val songRef = storage.getReferenceFromUrl(song.data)
            songRef.downloadUrl.addOnSuccessListener { uri ->
                song.data = uri.toString()
                listShareSong.add(song)

                if (listShareSong.size == currentItem.songs!!.size) {
                    val bundle = Bundle().apply {
                        putParcelableArrayList("listShareSongInPlaylist", ArrayList(listShareSong))
                        putString("namePlaylist", currentItem.playlistName)
                        putString("playlistId", currentItem.id)
                    }
                    navController.navigate(R.id.action_fragmentSharedPlaylist_to_fragmentSharePlaylistSpecific, bundle)
                }
            }.addOnFailureListener { exception ->
                Log.e("DownloadAndUpdate", "Failed to retrieve song URL: ${exception.message}")
            }
        }
    }

    private fun clearSharedPreferences() {
        val sharedPreferences = context.getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(context, "SharedPreferences cleared", Toast.LENGTH_SHORT).show()
        val sharedPreferencess = context.getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferencess.getString("playlists", null)
        val type = object : TypeToken<List<SharePlaylist>>() {}.type
        val playlists: List<SharePlaylist>? = gson.fromJson(json, type)
        if (playlists != null) {
            for (playlist in playlists) {
                Log.d("SharedPlaylists", "Playlist ID: ${playlist.id}, Name: ${playlist.playlistName}, Number of Songs: ${playlist.numberOfSong}")
                playlist.songs?.forEach { song ->
                    Log.d("SharedPlaylists", "Song ID: ${song.id}, Name: ${song.songName}, Data: ${song.data}")
                }
            }
        } else {
            Log.d("SharedPlaylists", "No playlists found in SharedPreferences")
        }
    }

    private fun savePlaylistsToPreferences(playlists: List<SharePlaylist>) {
        val sharedPreferences = context.getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(playlists)
        editor.putString("playlists", json)
        editor.apply()
    }

    private fun getPlaylistsFromPreferences(): List<SharePlaylist> {
        val sharedPreferences = context.getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("playlists", null)
        val type = object : TypeToken<List<SharePlaylist>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
//
//    private fun handleNewSongs(newPlaylists: List<SharePlaylist>) {
//        val storedPlaylists = getPlaylistsFromPreferences()
//
//        for (newPlaylist in newPlaylists) {
//            val storedPlaylist = storedPlaylists.find { it.id == newPlaylist.id }
//            if (storedPlaylist != null) {
//                val newSongs = newPlaylist.songs?.filter { song ->
//                    storedPlaylist.songs?.none { it.id == song.id } == true
//                } ?: emptyList()
//
//                newSongs.forEach { newSong ->
//                    showNewSongDialog(newSong, newPlaylist)
//                }
//            }
//        }
//
//        savePlaylistsToPreferences(newPlaylists)
//    }
//
//    private fun showNewSongDialog(newSong: Song, playlist: SharePlaylist) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_accept_share, null)
//        val textView = dialogView.findViewById<TextView>(R.id.tvMessage)
//        textView.text = "A new song '${newSong.songName}' has been added to the playlist '${playlist.playlistName}'. Do you want to accept it?"
//
//        val dialog = AlertDialog.Builder(context)
//            .setView(dialogView)
//            .create()
//        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
//
//        dialogView.findViewById<TextView>(R.id.btnReject).setOnClickListener {
//            val playlistRef = database.reference.child("user").child(userId).child("sharedPlaylist").child(playlist.id!!)
//            val songRef = playlistRef.child("songs").child(newSong.id!!)
//            songRef.removeValue().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(context, "Song rejected and removed from playlist.", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(context, "Failed to remove song.", Toast.LENGTH_SHORT).show()
//                }
//            }
//            dialog.dismiss()
//        }
//
//        dialogView.findViewById<TextView>(R.id.btnAccept).setOnClickListener {
//            val storedPlaylists = getPlaylistsFromPreferences().toMutableList()
//            val storedPlaylist = storedPlaylists.find { it.id == playlist.id }
//            storedPlaylist?.songs?.add(newSong)
//
//            savePlaylistsToPreferences(storedPlaylists)
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }

    private fun removePlaylistFromPreferences(playlistId: String) {
        context.getSharedPreferences("SharedPlaylists", Context.MODE_PRIVATE)
        val playlists = getPlaylistsFromPreferences().toMutableList()
        val updatedPlaylists = playlists.filter { it.id != playlistId }
        savePlaylistsToPreferences(updatedPlaylists)
    }
}