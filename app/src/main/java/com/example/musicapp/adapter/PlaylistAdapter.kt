package com.example.musicapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
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
import com.example.musicapp.R
import com.example.musicapp.model.Playlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PlaylistAdapter(
    private val items: MutableList<Playlist>,
    private val navController: NavController,
    private val context: Context
) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        val numberOfSong: TextView = itemView.findViewById(R.id.tvNumberOfSong)
        val option: Button = itemView.findViewById(R.id.btnMoreOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.playlistName.text = currentItem.playlistName
        currentItem.songs = currentItem.songs ?: mutableListOf()
        currentItem.numberOfSong = currentItem.songs?.size.toString() + " tracks"
        holder.numberOfSong.text = currentItem.numberOfSong

        holder.itemView.setOnClickListener {
            var songList = currentItem.songs
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(songList))
                putString("namePlaylist", currentItem.playlistName)
                putString("playlistId", currentItem.id)
            }
            navController.navigate(R.id.action_fragmentPlaylist_to_fragmentPlaylistSpecific, bundle)
        }

        holder.option.setOnClickListener { view ->
            showPopupMenu(view, currentItem)
        }


    }

    private fun showPopupMenu(view: View, playlist: Playlist) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.option_for_playlist, popup.menu)
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
                R.id.addTrack -> {
                    val bundle = Bundle().apply {
                        putString("playlist", playlist.playlistName)
                        putString("playlistId", playlist.id)
                    }
                    navController.navigate(R.id.action_fragmentPlaylist_to_fragmentSelectTracks, bundle)
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


    private fun showEditPlaylistDialog(playlist: Playlist) {
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

    private fun updatePlaylistNameInDatabase(id: String, playlist: Playlist) {
        Log.d("PlaylistAdapter", "$id")
        val database: DatabaseReference = database.reference.child("user").child(userId).child("playlists")
        database.child(id).setValue(playlist)
    }

    private fun deletePlaylist(position: Int,id: String) {
        val database: DatabaseReference = database.reference.child("user").child(userId).child("playlists")
        database.child(id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }


}