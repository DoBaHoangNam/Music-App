package com.example.musicapp.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.model.SharePlaylist
import com.example.musicapp.model.Song
import com.example.musicapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import java.io.File

class UserSearchAdapter(
    private val requireContext: Context,
    private val items: MutableList<User>,
    private val song: Song
) :
    RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val email: TextView = itemView.findViewById(R.id.tvEmail)
        val image: ImageView = itemView.findViewById(R.id.imgUser)

        fun bind(user: User) {
            username.text = user.username
            email.text = user.email
            Glide.with(itemView.context)
                .load(R.drawable.baseline_person_24)
                .placeholder(R.drawable.baseline_person_24)
                .into(image)


            itemView.setOnClickListener {
                createOrUpdateSharedPlaylist(user, song)
                (requireContext as Activity).finish()
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.bind(currentItem)


    }

    override fun getItemCount(): Int {
        return items.size
    }


    private fun createOrUpdateSharedPlaylist(user: User, song: Song) {
        val database = FirebaseDatabase.getInstance().reference
        val userId = user.id as String
        var playlistName = ""

        retrieveUser { username ->
            playlistName = "shared music from $username"
            Log.d("UserSearchAdapter", "Retrieved username: $username")
            // Do something with the username
        }

        // Create a copy of the song to store in the playlist
        val sharedSong = Song(
            id = song.id,
            songName = song.songName,
            singerName = song.singerName,
            album = song.album,
            duration = song.duration,
            data = "",  // This will be updated with Firebase Storage URI
            image = song.image
        )

        // Upload song file to Firebase Storage
        val file = File(song.data)
        val storageRef = FirebaseStorage.getInstance().reference.child("songs/${file.name}")
        val uploadTask = storageRef.putFile(Uri.fromFile(file))

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnSuccessListener { downloadUri ->
            val uploadedUri = downloadUri.toString()
            sharedSong.data = uploadedUri

            val playlistRef = database.child("user").child(userId).child("sharedPlaylist").child(playlistName)

            playlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val playlist: SharePlaylist? = dataSnapshot.getValue(SharePlaylist::class.java)
                    if (playlist != null) {
                        // Initialize songs if null
                        if (playlist.songs == null) {
                            playlist.songs = mutableListOf()
                        }

                        val existingSong = playlist.songs?.find { it.id == sharedSong.id }
                        if (existingSong != null) {
                            // Remove the existing song
                            playlist.songs?.remove(existingSong)
                        }
                        // Add the new version of the song
                        playlist.songs?.add(sharedSong)
                        playlist.numberOfSong = playlist.songs!!.size.toString()
                        playlistRef.setValue(playlist)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext, "Playlist $playlistName updated successfully for user ${user.username}", Toast.LENGTH_SHORT).show()
                                Log.d("UserSearchAdapter", "Playlist $playlistName updated successfully for user ${user.username}")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext, "Failed to update playlist: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("UserSearchAdapter", "Failed to update playlist: ${e.message}")
                            }
                    } else {
                        val newPlaylist = SharePlaylist(
                            id = playlistName, // Use playlistName as the ID
                            playlistName = playlistName,
                            numberOfSong = "1",
                            songs = mutableListOf(sharedSong)  // Start with the first song
                        )

                        playlistRef.setValue(newPlaylist)
                            .addOnSuccessListener {
                                Log.d("UserSearchAdapter", "Playlist $playlistName created successfully for user ${user.username}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserSearchAdapter", "Failed to create playlist: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("UserSearchAdapter", "Failed to check playlist existence: ${databaseError.message}")
                }
            })
        }.addOnFailureListener { e ->
            Log.e("UserSearchAdapter", "Failed to upload song: ${e.message}")
        }
    }






    private fun retrieveUser(callback: (String) -> Unit) {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        val userRef: DatabaseReference = database.reference.child("user").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var currentUsername = ""
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        currentUsername =  user.username?.substringBefore('@') ?: ""
                        Log.d("UserSearchAdapter", "username:  $currentUsername")
                    }
                }
                callback(currentUsername)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                callback("")
            }
        })
    }






}