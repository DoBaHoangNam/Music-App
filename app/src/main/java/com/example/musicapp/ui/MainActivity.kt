package com.example.musicapp.ui

import com.example.musicapp.SongViewModel
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.model.Song
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), MediaPlayerControl {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: CircleImageView
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentSongIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable
    private lateinit var songList: MutableList<Song>
    private val songViewModel: SongViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        songList = intent.getParcelableArrayListExtra<Song>("song_list")?.toMutableList()
            ?: mutableListOf()

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
            Toast.makeText(this, "aaaaaaaaa", Toast.LENGTH_SHORT).show()
        }

        binding.playBtn.setOnClickListener {
            playPauseSong()
            Toast.makeText(this, "aaaaaaaaa", Toast.LENGTH_SHORT).show()
        }

        // Add click listeners for skip and back buttons
        binding.icNext.setOnClickListener {
            skipToNextSong() // Skip to the next song
            Toast.makeText(this, "bbbbbbb", Toast.LENGTH_SHORT).show()
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.icBack.setOnClickListener {
            backToPreviousSong() // Go back to the previous song
            Toast.makeText(this, "cccccccc", Toast.LENGTH_SHORT).show()
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.backBtn.setOnClickListener {
            backToPreviousSong() // Go back to the previous song
            Toast.makeText(this, "cccccccc", Toast.LENGTH_SHORT).show()
            Log.d("check_song", currentSongIndex.toString())
        }

        binding.nextBtn.setOnClickListener {
            skipToNextSong() // Skip to the next song
            Toast.makeText(this, "bbbbbbb", Toast.LENGTH_SHORT).show()
            Log.d("check_song", currentSongIndex.toString())
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


    fun showBottomSheet(songTitle: String, artistName: String, duration: String, imageUrl: String) {
        binding.tvSongName.text = songTitle
        binding.tvSingerName.text = artistName
        binding.endTimeTv.text = formatDuration(duration.toLong())
        binding.tvNameSongPlaying.text = songTitle
        binding.tvSingerOfSongPlaying.text = artistName
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_song_round)
                .into(binding.imgSong)
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_song_round)
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
            val currentTime = it.currentPosition
            binding.startTimeTv.text = formatDuration(currentTime.toLong())
            val progress = (currentTime / 1000).toInt()
            binding.seekBar.progress = progress
        }
    }


    override fun playPauseSong() {
        if (mediaPlayer == null) return

        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
            binding.icPlay.setBackgroundResource(R.drawable.baseline_play_arrow_24)
            binding.playBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
            handler.removeCallbacks(updateTimeRunnable)
        } else {
            mediaPlayer?.start()
            isPlaying = true
            binding.icPlay.setBackgroundResource(R.drawable.baseline_pause_24)
            binding.playBtn.setBackgroundResource(R.drawable.baseline_pause_24)
            handler.post(updateTimeRunnable)
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
        if (currentSongIndex < songList.size - 1) {
            currentSongIndex++
            playSong(songList[currentSongIndex])
        }
        saveCurrentSongIndex()
    }

    // Function to go back to the previous song
    private fun backToPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            playSong(songList[currentSongIndex])
        }
        saveCurrentSongIndex()
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
        songList = intent.getParcelableArrayListExtra<Song>("song_list")?.toMutableList()
            ?: mutableListOf()

        songViewModel.songList.value = songList


    }

    override fun onPause() {
        super.onPause()
        saveCurrentSongIndex()
    }

    private fun saveCurrentSongIndex() {
        val sharedPreferences = getSharedPreferences("MusicAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("currentSongIndex", currentSongIndex)
        editor.apply()
    }

    private fun restoreCurrentSongIndex() {
        val sharedPreferences = getSharedPreferences("MusicAppPreferences", Context.MODE_PRIVATE)
        currentSongIndex = sharedPreferences.getInt("currentSongIndex", 0)
        // Play the last played song
        updateBottomSheetWhenStart(
            songList[currentSongIndex].songName,
            songList[currentSongIndex].singerName,
            songList[currentSongIndex].duration.toString(),
            songList[currentSongIndex].image
        )
    }

    fun updateBottomSheetWhenStart(
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
        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_song_round)
                .into(binding.imgSong)
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_song_round)
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
}

