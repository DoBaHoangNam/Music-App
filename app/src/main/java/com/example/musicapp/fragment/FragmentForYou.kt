package com.example.musicapp.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.DataHolder
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.adapter.AlbumAdapter
import com.example.musicapp.adapter.TrendingAdapter
import com.example.musicapp.databinding.FragmentForYouBinding
import com.example.musicapp.model.Album
import com.example.musicapp.model.Song
import com.example.musicapp.model.User
import com.example.musicapp.ui.ActivitySearch
import com.example.musicapp.ui.ActivitySettings
import com.example.musicapp.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class FragmentForYou : Fragment() {

    private lateinit var binding: FragmentForYouBinding
    private var mediaPlayerControl: MediaPlayerControl? = null
    private var albumList: MutableList<Album> = mutableListOf()
    private var songs: MutableList<Song> = mutableListOf()
    private val songViewModel: SongViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private var listOfTrendingSong = mutableListOf<Song>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MediaPlayerControl) {
            mediaPlayerControl = context
        } else {
            throw RuntimeException("$context must implement MediaPlayerControl")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mediaPlayerControl = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            albumList = it.getParcelableArrayList<Album>("album_list")?.toMutableList()
                ?: mutableListOf()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentForYouBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val calendar: Calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour in 5..12) {
            binding.welcomeTv.text = "Good Morning"
            binding.statusLoti.setAnimation(R.raw.sun_loti)
        } else if (hour in 13..18) {
            binding.welcomeTv.text = "Good Afternoon"
            binding.statusLoti.setAnimation(R.raw.sun_loti)
        } else {
            binding.welcomeTv.text = "Good Night"
            binding.statusLoti.setAnimation(R.raw.moon_loti)
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        retrieveUser()

        songViewModel.albumList.observe(viewLifecycleOwner) { albumList ->
            displayAlbum(albumList)
        }

        binding.icSearch.setOnClickListener {
            songViewModel.songList.observe(viewLifecycleOwner) { songList ->
                songs = songList
            }
            val intent = Intent(requireContext(), ActivitySearch::class.java).apply {
                putParcelableArrayListExtra("song_list", ArrayList(songs))
            }
            startActivity(intent)
        }

        binding.icSetting.setOnClickListener {
            val intent = Intent(requireContext(), ActivitySettings::class.java)
            startActivity(intent)
        }

        binding.btnShuffle.setOnClickListener {
            songViewModel.songList.observe(viewLifecycleOwner) { songList ->
                songs = songList
            }
            if (songs.isNotEmpty()) {
                val randomIndex = (0 until songs.size).random()
                val randomSong = songs[randomIndex]
                mediaPlayerControl?.checkPlaySong(randomSong, MainActivity.SongSource.A)
                Log.d("check_source", randomSong.toString() + " shuffle")
                sharedPreferences =
                    requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("selected_song", randomSong.songName).apply()
            }
        }

        binding.btnHistory.setOnClickListener {
            var listSongPlayed = DataHolder.playedSongs
            val namePlaylist = "Recent Played"
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(listSongPlayed))
                putString("namePlaylist", namePlaylist)
            }
            findNavController().navigate(
                R.id.action_fragmentForYou_to_fragmentPlaylistSpecific,
                bundle
            )
        }

        binding.btnFavourite.setOnClickListener {
            var listSongFavorite = DataHolder.favouriteSongs
            val namePlaylist = "Favourite"
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(listSongFavorite))
                putString("namePlaylist", namePlaylist)
            }
            findNavController().navigate(
                R.id.action_fragmentForYou_to_fragmentPlaylistSpecific,
                bundle
            )
        }

        binding.btnMostPlay.setOnClickListener {
            var mostPlayedSongs = DataHolder.mostPlayedSongs
            val namePlaylist = "Most Played"
            val bundle = Bundle().apply {
                putStringArrayList("listSongInPlaylist", ArrayList(mostPlayedSongs))
                putString("namePlaylist", namePlaylist)
            }
            findNavController().navigate(
                R.id.action_fragmentForYou_to_fragmentPlaylistSpecific,
                bundle
            )
        }

        fetchSharePlaylist{trendingSongs ->
            listOfTrendingSong = trendingSongs
            displayTrendingSongs(listOfTrendingSong)
            Log.d("FragmentPlaylist", "$listOfTrendingSong" + "1")
        }

        return binding.root


    }

    private fun retrieveUser() {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        val userRef: DatabaseReference = database.reference.child("user").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        binding.welcomeUserTv.text = user.username?.substringBefore('@') ?: ""
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun displayAlbum(albumList: MutableList<Album>) {
        val defaultImage = R.mipmap.ic_song_round_high.toString()
        // Filter the album list to include only albums with a non-empty albumArt
        val filteredAlbumList =
            albumList.filter { it.albumArt.isNotEmpty() && it.albumArt != defaultImage }

        // Limit the list to the first 10 albums
        val limitedAlbumList = filteredAlbumList.shuffled().take(10).toMutableList()

        // Set up the RecyclerView with the filtered and limited list
        binding.recvSuggestion.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = AlbumAdapter(limitedAlbumList, findNavController())
        binding.recvSuggestion.adapter = adapter
        Log.d("album_check", limitedAlbumList.size.toString())
    }

    private fun displayTrendingSongs(trendingSongs: MutableList<Song>) {
        binding.recvTrending.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = TrendingAdapter(trendingSongs, requireContext())
        binding.recvTrending.adapter = adapter
        Log.d("album_check", trendingSongs.size.toString())
    }

    private fun fetchSharePlaylist(callback: (MutableList<Song>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid ?: ""
        val trendingSongRef = database.child("trending")

        trendingSongRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val trendingSongs = mutableListOf<Song>()
                for (trendingSongsSnapshot in dataSnapshot.children) {
                    val song = trendingSongsSnapshot.getValue(Song::class.java)
                    song?.let {
                        trendingSongs.add(it)
                    }
                }
                callback(trendingSongs)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("fetchSharePlaylist", "Failed to fetch playlists: ${databaseError.message}")
                callback(mutableListOf()) // Return an empty list on failure
            }
        })
    }

    companion object {
        fun newInstance(albumList: MutableList<Album>): FragmentForYou {
            val fragment = FragmentForYou()
            val args = Bundle()
            args.putParcelableArrayList("album_list", ArrayList(albumList))
            fragment.arguments = args
            return fragment
        }
    }


}