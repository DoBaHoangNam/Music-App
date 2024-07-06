package com.example.musicapp.ui


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.DataHolder
import com.example.musicapp.MediaPlayerControl
import com.example.musicapp.MusicPlayerService
import com.example.musicapp.SongViewModel
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.model.Album
import com.example.musicapp.model.Artist
import com.example.musicapp.model.Song
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
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
    private var isFavorite = false
    private val SEEK_TIME_MS = 2000
    private var isSkippingForward = false
    private var isSkippingBackward = false
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private val KEY_SHARED_SONG = "share_song"
    private val KEY_SPEED_SONG = "playback_speed"
    private val KEY_PITCH_SONG = "playback_pitch"
    private var musicPlayerService: MusicPlayerService? = null
    private var serviceBound = false

    enum class SongSource {
        A, B
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicPlayerService.MusicServiceBinder
            musicPlayerService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private val musicPlayerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.musicplayer.ACTION_PLAY" -> {
                    playPauseSong()
                    Log.d("check_sercice", "currentIndex " + currentSongIndex.toString())
                }
                "com.example.musicplayer.ACTION_PAUSE" -> {
                    playPauseSong()
                    Log.d("check_sercice", "currentIndex " + currentSongIndex.toString())
                }
                "com.example.musicplayer.ACTION_NEXT" -> {
                    skipToNextSong()
                    Log.d("check_sercice", "currentIndex " + currentSongIndex.toString())
                }
                "com.example.musicplayer.ACTION_PREVIOUS" -> {
                    backToPreviousSong()
                    Log.d("check_sercice", "currentIndex " + currentSongIndex.toString())

                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference


        songList = DataHolder.songList
        albumList = DataHolder.albumList
        artistList = DataHolder.artistList
        musicPlayerService?.totalSong = songList.size

        getPlayedSongsList { playedSongs ->
            DataHolder.playedSongs = playedSongs
            Log.d("Main_Activity", "Played Songs: $playedSongs")
        }

        fetchFavoriteSongs { favoriteSongs ->
            DataHolder.favouriteSongs = favoriteSongs
            Log.d("Main_Activity", "Favorite songs: $favoriteSongs")
        }

        fetchMostPlayedSongs { mostPlayedSong ->
            DataHolder.mostPlayedSongs = mostPlayedSong
            Log.d("Main_Activity", "Most Played songs: $mostPlayedSong")
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

        binding.btnAddToPlaylist.setOnClickListener {
            val songNameToAdd = songList[currentSongIndex].songName
            Log.d("MainActivity", "Button clicked, songNameToAdd: $songNameToAdd")


            val intent = Intent(this, ActivityAlbumSelected::class.java).apply {
                putExtra("song_name", songNameToAdd)
            }
            startActivity(intent)

            layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }

        binding.btnShareLoti.setOnClickListener {
            val songToShare = songList[currentSongIndex]
            Log.d("MainActivity", "Button share clicked, song: $songToShare")

            val intent = Intent(this, ActivitySearchUser::class.java).apply {
                putExtra("song_name", songToShare)
            }
            startActivity(intent)
        }

        val filter = IntentFilter().apply {
            addAction("com.example.musicplayer.ACTION_PLAY")
            addAction("com.example.musicplayer.ACTION_PAUSE")
            addAction("com.example.musicplayer.ACTION_NEXT")
            addAction("com.example.musicplayer.ACTION_PREVIOUS")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(musicPlayerReceiver, filter)

        // Start and bind service
        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)



        binding.icPlay.setOnClickListener {
            playPauseSong()
            musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
            musicPlayerService?.song = songList[currentSongIndex]
        }

        binding.playBtn.setOnClickListener {
            playPauseSong()
            musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
            musicPlayerService?.song = songList[currentSongIndex]
            musicPlayerService?.currentSongIndex = currentSongIndex
            musicPlayerService?.isPlaying = isPlaying
        }

        // Add click listeners for skip and back buttons
        binding.icNext.setOnClickListener {
            skipToNextSong() // Skip to the next song
            Log.d("check_song", currentSongIndex.toString())
            musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
            musicPlayerService?.song = songList[currentSongIndex]
            musicPlayerService?.currentSongIndex = currentSongIndex
            musicPlayerService?.isPlaying = isPlaying
        }

        binding.icBack.setOnClickListener {
            backToPreviousSong() // Go back to the previous song
            Log.d("check_song", currentSongIndex.toString())
            musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
            musicPlayerService?.song = songList[currentSongIndex]
            musicPlayerService?.currentSongIndex = currentSongIndex
            musicPlayerService?.isPlaying = isPlaying
        }

        binding.backBtn.setOnClickListener {
            backToPreviousSong() // Go back to the previous song
            Log.d("check_song", currentSongIndex.toString())
            musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
            musicPlayerService?.song = songList[currentSongIndex]
            musicPlayerService?.currentSongIndex = currentSongIndex
            musicPlayerService?.isPlaying = isPlaying
        }

        binding.nextBtn.setOnClickListener {
            skipToNextSong() // Skip to the next song
            Log.d("check_song", currentSongIndex.toString())
            musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
            musicPlayerService?.song = songList[currentSongIndex]
            musicPlayerService?.currentSongIndex = currentSongIndex
            musicPlayerService?.isPlaying = isPlaying
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
                    checkIfSongIsFavorite(songList[currentSongIndex])


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

        binding.icAddToFavorite.setOnClickListener {
            toggleFavoriteStatus()
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
            checkPlaySong(songList[currentSongIndex], SongSource.A)
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
            if (binding.seekBar.progress == binding.seekBar.max) playNextSong()
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


    override fun checkPlaySong(song: Song, source: SongSource) {
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

        when (source) {
            SongSource.A -> {
                binding.icNext.visibility = View.VISIBLE
                binding.icBack.visibility = View.VISIBLE
                binding.backBtn.visibility = View.VISIBLE
                binding.nextBtn.visibility = View.VISIBLE
                binding.btnshuffle.visibility = View.VISIBLE
                binding.btnRepeat.visibility = View.VISIBLE
                binding.icAddToFavorite.visibility = View.VISIBLE
                binding.btnShareLoti.visibility = View.VISIBLE
                binding.btnAddToPlaylist.visibility = View.VISIBLE

                currentSongIndex = songList.indexOf(song)


                getPlayedSongsList { playedSongs ->
                    var recentPlayed = playedSongs
                    val existingSongIndex = recentPlayed.indexOf(song.songName)
                    if (existingSongIndex != -1) {
                        // Remove the existing song if found
                        recentPlayed.removeAt(existingSongIndex)
                    }
                    recentPlayed.add(song.songName)
                    savePlayedSongsList(recentPlayed)

                    DataHolder.playedSongs = recentPlayed
                    Log.d("Main_Activity", "$recentPlayed history")
                }

                checkIfSongIsFavorite(song)
                incrementPlayCount(song)

                fetchFavoriteSongs { favoriteSongs ->
                    DataHolder.favouriteSongs = favoriteSongs
                    Log.d("Main_Activity", "Favorite songs: $favoriteSongs")
                }

                fetchMostPlayedSongs { mostPlayedSong ->
                    DataHolder.mostPlayedSongs = mostPlayedSong
                    Log.d("Main_Activity", "Most Played songs: $mostPlayedSong")
                }
            }

            SongSource.B -> {
                binding.icNext.visibility = View.INVISIBLE
                binding.icBack.visibility = View.INVISIBLE
                binding.backBtn.visibility = View.INVISIBLE
                binding.nextBtn.visibility = View.INVISIBLE
                binding.btnshuffle.visibility = View.INVISIBLE
                binding.btnRepeat.visibility = View.INVISIBLE
                binding.icAddToFavorite.visibility = View.INVISIBLE
                binding.btnShareLoti.visibility = View.INVISIBLE
                binding.btnAddToPlaylist.visibility = View.INVISIBLE

            }
        }




    }

    override fun stopSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        binding.icPlay.setBackgroundResource(R.drawable.baseline_play_arrow_24)
        binding.playBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
    }

    override fun setPlaybackSpeed() {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val speed = sharedPreferences.getFloat("playback_speed", 1.0f) // Lấy tốc độ từ SharedPreferences, mặc định là 1.0f

        mediaPlayer?.let {
            it.playbackParams = it.playbackParams.setSpeed(speed)
        } ?: run {
            Log.e("Main_Activity", "MediaPlayer is null, cannot set playback speed")
        }

    }

    override fun setPlaybackPitch() {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val pitch = sharedPreferences.getInt("playback_pitch", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let {
                val params = PlaybackParams().apply {
                    this.pitch =
                        Math.pow(2.0, pitch / 12.0).toFloat() // Convert semitone to pitch ratio
                }
                it.playbackParams = params
            } ?: run {
                Log.e("Main_Activity", "MediaPlayer is null, cannot set playback params")
            }
        }
    }




    // Function to skip to the next song
    private fun skipToNextSong() {
        if (isShuffle) {
            currentSongIndex = (songList.indices).random()
        } else {
            currentSongIndex = if (currentSongIndex + 1 == songList.size) 0 else currentSongIndex + 1
        }
        checkPlaySong(songList[currentSongIndex], SongSource.A)
        saveCurrentSongIndex()
    }

    // Function to go back to the previous song
    private fun backToPreviousSong() {
        if (isShuffle) {
            currentSongIndex = (songList.indices).random()
        } else {
            currentSongIndex =
                if (currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
        }
        checkPlaySong(songList[currentSongIndex], SongSource.A)
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
        musicPlayerService?.checkPlaySong(songList[currentSongIndex], isPlaying)
        musicPlayerService?.updateCurrentIndex(currentSongIndex, songList[currentSongIndex], isPlaying)
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
        playSharedSongIfAvailable()
    }

    private fun playSelectedSongIfAvailable() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val selectedSongName = sharedPreferences.getString(KEY_SELECTED_SONG, null)
        selectedSongName?.let { songName ->
            val song = songList.find { it.songName == songName }
            song?.let {
                checkPlaySong(it, SongSource.A)
                saveCurrentSongIndex()
                Log.d("check_source", it.songName + " song")
                clearSelectedSongFromSharedPreferences()  // Clear the stored song name after playing
            }
        }
    }

    // Lưu danh sách bài hát đã phát
    private fun savePlayedSongsList(playedSongs: List<String>) {
        userId = auth.currentUser?.uid ?: ""
        val recentPlaylistRef: DatabaseReference =
            database.reference.child("user").child(userId).child("playedSongs")
        recentPlaylistRef.setValue(playedSongs)
    }

    // Lấy danh sách bài hát đã phát
    private fun getPlayedSongsList(callback: (MutableList<String>) -> Unit) {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        val recentPlaylistRef: DatabaseReference =
            database.reference.child("user").child(userId).child("playedSongs")
        recentPlaylistRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                val playedSongs = mutableListOf<String>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val song = snapshot.getValue(String::class.java)
                        song?.let { playedSongs.add(it) }
                    }
                }
                callback(playedSongs)
            } else {
                // Handle error if needed
                callback(mutableListOf())
            }
        }
    }

    private fun checkIfSongIsFavorite(song: Song) {
        val favoriteSongsRef = databaseReference.child("user").child(auth.currentUser?.uid ?: "")
            .child("favouriteSongs")
        favoriteSongsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isFavorite =
                    snapshot.children.any { it.getValue(String::class.java) == song.songName }
                updateFavoriteIcon()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to check if song is favorite: ${error.message}")
            }
        })
    }

    private fun toggleFavoriteStatus() {
        val favoriteSongsRef = databaseReference.child("user").child(auth.currentUser?.uid ?: "")
            .child("favouriteSongs")
        val currentSongName = songList[currentSongIndex].songName
        if (isFavorite) {
            favoriteSongsRef.orderByValue().equalTo(currentSongName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (favoriteSnapshot in snapshot.children) {
                            favoriteSnapshot.ref.removeValue()
                        }
                        isFavorite = false
                        updateFavoriteIcon()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MainActivity", "Failed to remove favorite song: ${error.message}")
                    }
                })
        } else {
            val newFavoriteRef = favoriteSongsRef.push()
            newFavoriteRef.setValue(currentSongName)
            isFavorite = true
            updateFavoriteIcon()
        }
    }

    private fun fetchFavoriteSongs(callback: (MutableList<String>) -> Unit) {
        val favoriteSongsRef = databaseReference.child("user").child(auth.currentUser?.uid ?: "")
            .child("favouriteSongs")
        favoriteSongsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteSongs = mutableListOf<String>()
                for (favoriteSnapshot in snapshot.children) {
                    val songName = favoriteSnapshot.getValue(String::class.java)
                    songName?.let { favoriteSongs.add(it) }
                }
                callback(favoriteSongs)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Main_Activity", "Failed to fetch favorite songs: ${error.message}")
            }
        })
    }


    private fun updateFavoriteIcon() {
        if (isFavorite) {
            binding.icLove.visibility = View.VISIBLE
            binding.icAddToFavorite.setImageResource(R.drawable.baseline_favorite_solid_24)
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                binding.icLove.visibility = View.INVISIBLE
            }
        } else {
            binding.icAddToFavorite.setImageResource(R.drawable.baseline_favorite_border_blue_24)
        }
    }

    private fun incrementPlayCount(song: Song) {
        val userUid = auth.currentUser?.uid ?: return
        val songRef = databaseReference.child("user").child(userUid).child("playCounts")
            .child(song.id.toString())  // Storing play counts under the user's node
        songRef.child("name").setValue(song.songName)  // Ensure the song name is stored
        songRef.child("playCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentPlayCount = snapshot.getValue(Int::class.java) ?: 0
                songRef.child("playCount").setValue(currentPlayCount + 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to update play count: ${error.message}")
            }
        })
    }

    private fun fetchMostPlayedSongs(callback: (MutableList<String>) -> Unit) {
        val userUid = auth.currentUser?.uid ?: return
        val playCountsRef = databaseReference.child("user").child(userUid).child("playCounts")
        playCountsRef.orderByChild("playCount").limitToLast(10)  // Fetch top 10 most played songs for the user
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mostPlayedSongs = mutableListOf<String>()
                    for (songSnapshot in snapshot.children) {
                        val songData = songSnapshot.value as? Map<String, Any>
                        if (songData != null) {
                            val songName = songData["name"] as? String
                            if (songName != null) {
                                mostPlayedSongs.add(songName)
                            }
                        }
                    }
                    callback(mostPlayedSongs)  // Reverse to get descending order
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to fetch most played songs: ${error.message}")
                }
            })
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
        if(key == KEY_SHARED_SONG){
            playSharedSongIfAvailable()
        }
        if(key == KEY_SPEED_SONG){
            setPlaybackSpeed()
        }
        if(key == KEY_PITCH_SONG){
            setPlaybackPitch()
        }

    }

    // Hàm để phát bài hát được chia sẻ nếu có sẵn trong SharedPreferences
    private fun playSharedSongIfAvailable() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sharedSongJson = sharedPreferences.getString(KEY_SHARED_SONG, null)
        sharedSongJson?.let { songJson ->
            val gson = Gson()
            val sharedSong = gson.fromJson(songJson, Song::class.java)
            sharedSong?.let {

                Log.d("check_source", it.songName + " shared song")
                DataHolder.songList.add(it)
                songList = DataHolder.songList
                checkPlaySong(it,SongSource.B)
                Log.d("check_source", songList.size.toString() + " check size")
                clearSharedSongFromSharedPreferences()  // Xóa bài hát được lưu trữ sau khi phát
            }
        }
    }


    // Hàm để xóa bài hát được chia sẻ khỏi SharedPreferences
    private fun clearSharedSongFromSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_SHARED_SONG)
        editor.apply()
    }


    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        if (serviceBound) {
            unbindService(connection)
            serviceBound = false
        }
        if (serviceBound) {
            musicPlayerService?.stopSelf()
        }
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
        handler.removeCallbacks(updateTimeRunnable)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(musicPlayerReceiver)
        stopService(Intent(this, MusicPlayerService::class.java))
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(connection)
            serviceBound = false
        }
    }

}

