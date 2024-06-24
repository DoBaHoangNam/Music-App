package com.example.musicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicapp.model.Song
import com.example.musicapp.ui.MainActivity

class MusicPlayerService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    var isPlaying = false
    var currentSongIndex: Int = 0
    var song: Song? = null
    var totalSong = 0

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private val binder = MusicServiceBinder()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")

        // Initialize MediaSession
        mediaSession = MediaSessionCompat(this, "MusicService")
        // Optional: Handle media buttons
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_PLAY -> handleActionPlay()
            ACTION_PAUSE -> handleActionPause()
            ACTION_NEXT -> handleActionNext()
            ACTION_PREVIOUS -> handleActionPrevious()
        }
        return START_NOT_STICKY
    }

    fun handleActionPlay() {
        song?.let {
            // Handle play action
            Log.d(TAG, "handleActionPlay: Play action received")
            this.isPlaying = true
            updateNotification(it.songName, it.singerName, isPlaying)
            // Update the notification
            val intent = Intent(ACTION_PLAY)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } ?: run {
            Log.e(TAG, "handleActionPause: Song is not initialized")
        }
    }

    fun handleActionPause() {
        song?.let {
            // Handle pause action
            Log.d(TAG, "handleActionPause: Pause action received")
            this.isPlaying = false
            // Update the notification
            updateNotification(it.songName, it.singerName, isPlaying)
            val intent = Intent(ACTION_PAUSE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } ?: run {
            Log.e(TAG, "handleActionPause: Song is not initialized")
        }
    }

    fun handleActionNext() {
        song?.let {
            // Handle next action
            Log.d(TAG, "handleActionNext: Next action received")
            currentSongIndex += 1
            if (currentSongIndex + 1 == totalSong) 0 else currentSongIndex + 1
            updateNotification(it.songName, it.singerName, isPlaying)
            // Notify MainActivity
            val intent = Intent(ACTION_NEXT)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } ?: run {
            Log.e(TAG, "handleActionPause: Song is not initialized")
        }
    }

    fun handleActionPrevious() {
        song?.let {
            // Handle previous action
            Log.d(TAG, "handleActionPrevious: Previous action received")
            // Implement your logic to go back to the previous song
            currentSongIndex -= 1
            if (currentSongIndex - 1 < 0) totalSong - 1 else currentSongIndex - 1
            updateNotification(it.songName, it.singerName, isPlaying)
            // Notify MainActivity
            val intent = Intent(ACTION_PREVIOUS)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } ?: run {
            Log.e(TAG, "handleActionPause: Song is not initialized")
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind: Service bound")
        return binder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: Creating notification channel")
            val name = "Music Playback"
            val descriptionText = "Channel for music playback controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkPlaySong(song: Song, isPlaying: Boolean) {
        Log.d(TAG, "playSong: Playing song ${song.songName}")
        this.isPlaying = isPlaying
        updateNotification(song.songName, song.singerName, isPlaying)
    }

    fun updateCurrentIndex(currentIndex: Int, song: Song, isPlaying: Boolean) {
        currentSongIndex = currentIndex
        this.song = song
        this.isPlaying = isPlaying

    }


    private fun updateNotification(songName: String, singerName: String, isPlaying: Boolean) {
        Log.d(TAG, "updateNotification: Updating notification for $songName by $singerName")
        Log.d(TAG, "updateNotification: isPlaying $isPlaying")

        val playPauseIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).apply {
                action = ACTION_NEXT
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).apply {
                action = ACTION_PREVIOUS
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(songName)
            .setContentText(singerName)
            .setSmallIcon(R.drawable.ic_song_foreground)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_previous_24,
                    "Previous",
                    previousIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24,
                    if (isPlaying) "Pause" else "Play",
                    playPauseIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_next_24,
                    "Next",
                    nextIntent
                )
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)

        startForeground(1, builder.build())
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service destroyed")
        mediaSession.release()
        stopForeground(true) // Remove the persistent notification.
    }


    companion object {
        const val TAG = "MusicPlayerService"
        const val ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.musicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.musicplayer.ACTION_PREVIOUS"
        const val CHANNEL_ID = "music_channel_id"
    }
}
