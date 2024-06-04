package com.example.musicapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.musicapp.DataHolder
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.R
import com.example.musicapp.SongViewModel
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), MediaPlayerControl,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: CircleImageView
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentSongIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable
    private lateinit var songList: MutableList<Song>
    private lateinit var albumList: MutableList<Album>
    private lateinit var artistList: MutableList<Artist>
    private val songViewModel: SongViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private val KEY_SELECTED_SONG = "selected_song"
    private var isShuffle: Boolean = false
    private var isRepeat: Boolean = false
    private val SEEK_TIME_MS = 5000 // Thời gian tua (5 giây)
    private var isSkippingForward = false
    private var isSkippingBackward = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

//        songList = intent.getParcelableArrayListExtra<Song>("song_list")?.toMutableList()
//            ?: mutableListOf()
//        albumList = intent.getParcelableArrayListExtra<Album>("album_list")?.toMutableList()
//            ?: mutableListOf()
//        artistList = intent.getParcelableArrayListExtra<Artist>("artist_list")?.toMutableList()
//            ?: mutableListOf()
        songList = DataHolder.songList
        albumList = DataHolder.albumList
        artistList = DataHolder.artistList

        if (albumList != null) {
            Log.d("check_source", albumList.size.toString() + " album2")
        }
        if (songList != null) {
            Log.d("check_source", songList.size.toString() + " song2")
        }
        if (artistList != null) {
            Log.d("check_source", artistList.size.toString() + " artist2")
        }


        loadFragment()

        restoreCurrentSongIndex()


        var layout = findViewById<SlidingUpPanelLayout>(R.id.slidingUp)
        binding.bottomSheetNowPlaying.visibility = View.INVISIBLE
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


        imageView = binding.imgSong

        binding.tvNameSongPlaying.isSelected = true

        binding.btnCollapse.setOnClickListener {
            layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }



        binding.icPlay.setOnClickListener {
            playPauseSong()
        }

        binding.playBtn.setOnClickListener {
            playPauseSong()
        }

        // Add click listeners for skip and back buttons
        binding.icNext.setOnClickListener {
            skipToNextSong() // Skip to the next song
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.icBack.setOnClickListener {
            backToPreviousSong() // Go back to the previous song
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.backBtn.setOnClickListener {
            backToPreviousSong() // Go back to the previous song
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.nextBtn.setOnClickListener {
            skipToNextSong() // Skip to the next song
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.icNext.setOnLongClickListener {
            startSkippingForward()
            true
        }

        binding.icNext.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                stopSkippingForward()
            }
            false
        }

        binding.icBack.setOnLongClickListener {
            startSkippingBackward()
            true
        }

        binding.icBack.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                stopSkippingBackward()
            }
            false
        }

        binding.nextBtn.setOnLongClickListener {
            startSkippingForward()
            true
        }

        binding.nextBtn.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                stopSkippingForward()
            }
            false
        }

        binding.backBtn.setOnLongClickListener {
            startSkippingBackward()
            true
        }

        binding.backBtn.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                stopSkippingBackward()
            }
            false
        }



        binding.btnshuffle.setOnClickListener {
            isShuffle = !isShuffle
            updateShuffleButton()
        }

        binding.btnRepeat.setOnClickListener {
            isRepeat = !isRepeat
            updateRepeatButton()
        }

        mediaPlayer?.setOnCompletionListener {
            playNextSong()
        }




        updateTimeRunnable = object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, 1000) // Update every second
            }
        }


        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress * 1000) // Convert to milliseconds
                    binding.startTimeTv.text = formatDuration(progress.toLong() * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateTimeRunnable) // Stop updating the current time
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateTimeRunnable) // Resume updating the current time
            }
        })



        layout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                binding.smallControlBar.alpha = 1 - slideOffset
                binding.bottomSheetNowPlaying.visibility = View.VISIBLE
                binding.bottomSheetNowPlaying.alpha = slideOffset
                val translationY = bottomNavigationView.height * slideOffset
                bottomNavigationView.translationY = translationY
                bottomNavigationView.animate().translationY(bottomNavigationView.height.toFloat())
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()
                startRotation()

            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?,
            ) {
                val currentFragmentId =
                    findNavController(R.id.fragmentContainerView2).currentDestination?.id
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    bottomNavigationView.visibility = View.GONE
                    binding.bottomSheetNowPlaying.visibility = View.VISIBLE
                    binding.smallControlBar.visibility = View.GONE


                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    bottomNavigationView.animate().translationY(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    binding.bottomSheetNowPlaying.visibility = View.GONE
                    binding.smallControlBar.visibility = View.VISIBLE

                    if (currentFragmentId == R.id.fragmentPlaylistSpecific || currentFragmentId == R.id.fragmentAlbumSingle) {
                        bottomNavigationView.visibility = View.GONE
                    } else {
                        bottomNavigationView.visibility = View.VISIBLE
                    }

                }
            }


        })

        var navController = findNavController(R.id.fragmentContainerView2)
        var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(navController)

        binding.icVolume.setOnClickListener {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI
            )
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentForYou, R.id.fragmentSongs, R.id.fragmentArtist, R.id.fragmentPlaylist, R.id.fragmentAlbum -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }

                R.id.fragmentPlaylistSpecific, R.id.fragmentAlbumSingle, R.id.fragmentArtistSingle -> {
                    //bottomNavigationView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }

        var isFavorite = false
        binding.icAddToFavorite.setOnClickListener {

            if (isFavorite) {
                binding.icAddToFavorite.setImageResource(R.drawable.baseline_favorite_border_blue_24)
            } else {
                binding.icLove.visibility = View.VISIBLE
                binding.icAddToFavorite.setImageResource(R.drawable.baseline_favorite_24)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding.icLove.visibility = View.INVISIBLE
                }
            }
            isFavorite = !isFavorite
        }


    }

    private val skipForwardRunnable = object : Runnable {
        override fun run() {
            if (isSkippingForward) {
                seekForward()
                handler.postDelayed(this, 200) // Lặp lại sau mỗi 200ms
            }
        }
    }

    private val skipBackwardRunnable = object : Runnable {
        override fun run() {
            if (isSkippingBackward) {
                seekBackward()
                handler.postDelayed(this, 200) // Lặp lại sau mỗi 200ms
            }
        }
    }


    private fun updateShuffleButton() {
        if (isShuffle) {
            binding.btnshuffle.setImageResource(R.drawable.baseline_shuffle_on_24)
        } else {
            binding.btnshuffle.setImageResource(R.drawable.icon_shuffle)
        }
    }

    private fun updateRepeatButton() {
        if (isRepeat) {
            binding.btnRepeat.setImageResource(R.drawable.baseline_repeat_one_24)
        } else {
            binding.btnRepeat.setImageResource(R.drawable.baseline_repeat_24)
        }
    }

    private fun playNextSong() {
        mediaPlayer?.let { player ->
            player.release() // Release current media player
            val nextIndex = when {
                isRepeat -> currentSongIndex // Repeat current song
                isShuffle -> (songList.indices).random() // Shuffle songs
                else -> (currentSongIndex + 1) % songList.size // Play next song in sequence
            }
            currentSongIndex = nextIndex
            saveCurrentSongIndex()
            playSong(songList[currentSongIndex])
        }
    }


    fun showBottomSheet(songTitle: String, artistName: String, duration: String, imageUrl: String) {
        binding.tvSongName.text = songTitle
        binding.tvSingerName.text = artistName
        binding.endTimeTv.text = formatDuration(duration.toLong())
        binding.tvNameSongPlaying.text = songTitle
        binding.tvSingerOfSongPlaying.text = artistName
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_song_foreground)
                .into(binding.imgSong)
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_song_foreground)
                .into(binding.imgSongPlaying)

        }
        binding.slidingUp.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateCurrentTime() {
        mediaPlayer?.let {
            val currentPosition = it.currentPosition / 1000 // Get the current position in seconds
            binding.seekBar.progress = currentPosition
            binding.startTimeTv.text = formatDuration(currentPosition.toLong() * 1000)
            if(binding.seekBar.progress == binding.seekBar.max) playNextSong()
        }


    }


    override fun playPauseSong() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
                binding.icPlay.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                binding.playBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                handler.removeCallbacks(updateTimeRunnable)
            } else {
                player.start()
                isPlaying = true
                binding.icPlay.setBackgroundResource(R.drawable.baseline_pause_24)
                binding.playBtn.setBackgroundResource(R.drawable.baseline_pause_24)
                handler.post(updateTimeRunnable)
            }
        }

    }


    override fun playSong(song: Song) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.data)
            prepare()
            start()
        }
        isPlaying = true
        binding.icPlay.setBackgroundResource(R.drawable.baseline_pause_24)
        binding.playBtn.setBackgroundResource(R.drawable.baseline_pause_24)

        showBottomSheet(song.songName, song.singerName, song.duration.toString(), song.image ?: "")
        handler.post(updateTimeRunnable)

        updateTimeRunnable = object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, 1000) // Update every second
            }
        }

        // Update seek bar max value
        mediaPlayer?.let {
            binding.seekBar.max = (it.duration / 1000).toInt()
        }

        currentSongIndex = songList.indexOf(song)
    }

    override fun stopSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        binding.icPlay.setBackgroundResource(R.drawable.baseline_play_arrow_24)
        binding.playBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
    }

    // Function to skip to the next song
    private fun skipToNextSong() {
        if (isShuffle) {
            currentSongIndex = (songList.indices).random()
        } else {
            currentSongIndex = if (currentSongIndex + 1 > songList.size) 0 else currentSongIndex + 1
        }
        playSong(songList[currentSongIndex])
        saveCurrentSongIndex()
    }

    // Function to go back to the previous song
    private fun backToPreviousSong() {
        if (isShuffle) {
            currentSongIndex = (songList.indices).random()
        } else {
            currentSongIndex = if (currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
        }
        playSong(songList[currentSongIndex])
        saveCurrentSongIndex()
    }
    private fun startSkippingForward() {
        isSkippingForward = true
        handler.post(skipForwardRunnable)
    }
    private fun stopSkippingForward() {
        isSkippingForward = false
        handler.removeCallbacks(skipForwardRunnable)
    }

    private fun startSkippingBackward() {
        isSkippingBackward = true
        handler.post(skipBackwardRunnable)
    }

    private fun stopSkippingBackward() {
        isSkippingBackward = false
        handler.removeCallbacks(skipBackwardRunnable)
    }

    private fun seekBackward() {
        mediaPlayer?.let {
            val newPosition = it.currentPosition - SEEK_TIME_MS
            it.seekTo(newPosition.coerceAtLeast(0))
        }
    }




    private fun seekForward() {
        mediaPlayer?.let {
            val newPosition = it.currentPosition + SEEK_TIME_MS
            it.seekTo(newPosition.coerceAtMost(it.duration))
        }
    }






    fun startRotation() {
        object : Runnable {
            override fun run() {
                imageView.animate().rotationBy(360f).withEndAction(this).setDuration(10000)
                    .setInterpolator(LinearInterpolator()).start()
            }
        }

        imageView.animate().rotationBy(360f).withEndAction {
            startRotation()
        }.setDuration(10000)
            .setInterpolator(LinearInterpolator()).start()
    }

    private fun loadFragment() {
        songViewModel.songList.value = songList
        songViewModel.albumList.value = albumList
        songViewModel.artistList.value = artistList
    }

    override fun onPause() {
        super.onPause()
        saveCurrentSongIndex()
    }

    private fun saveCurrentSongIndex() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("currentSongIndex", currentSongIndex)
        editor.apply()
    }

    private fun restoreCurrentSongIndex() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        currentSongIndex = sharedPreferences.getInt("currentSongIndex", 0)
        // Play the last played song
        updateBottomSheetWhenStart(
            songList[currentSongIndex].songName,
            songList[currentSongIndex].singerName,
            songList[currentSongIndex].duration.toString(),
            songList[currentSongIndex].image
        )
    }

    private fun updateBottomSheetWhenStart(
        songTitle: String,
        artistName: String,
        duration: String,
        imageUrl: String
    ) {
        binding.tvSongName.text = songTitle
        binding.tvSingerName.text = artistName
        binding.endTimeTv.text = formatDuration(duration.toLong())
        binding.tvNameSongPlaying.text = songTitle
        binding.tvSingerOfSongPlaying.text = artistName
        binding.seekBar.max = (songList[currentSongIndex].duration / 1000).toInt()
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_song_round_high)
                .into(binding.imgSong)
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_song_round_high)
                .into(binding.imgSongPlaying)

        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(songList[currentSongIndex].data)
            prepare()
        }

        Log.d("check_song", currentSongIndex.toString())

    }

    override fun onBackPressed() {
        val layout = findViewById<SlidingUpPanelLayout>(R.id.slidingUp)
        if (layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else if (true) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        playSelectedSongIfAvailable()
    }

    private fun playSelectedSongIfAvailable() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val selectedSongName = sharedPreferences.getString(KEY_SELECTED_SONG, null)
        selectedSongName?.let { songName ->
            val song = songList.find { it.songName == songName }
            song?.let {
                playSong(it)
                saveCurrentSongIndex()
                Log.d("check_source", it.songName + " song")
                clearSelectedSongFromSharedPreferences()  // Clear the stored song name after playing
            }
        }
    }

    private fun clearSelectedSongFromSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_SELECTED_SONG)
        editor.apply()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_SELECTED_SONG) {
            playSelectedSongIfAvailable()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


}

