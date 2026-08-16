package com.navneet.music.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.navneet.music.model.Song
import com.google.common.util.concurrent.ListenableFuture

/**
 * UI-side controller for the single player owned by PlaybackService.
 * The Activity never owns a second ExoPlayer instance.
 */
class PlaybackManager(context: Context) {
    private val appContext = context.applicationContext
    private val controllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(
            appContext,
            SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        ).buildAsync()

    private var controller: MediaController? = null
    private var pendingSong: Song? = null
    private val pendingListeners = mutableListOf<Player.Listener>()

    init {
        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }
                    .onSuccess { connected ->
                        controller = connected
                        pendingListeners.forEach(connected::addListener)
                        pendingListeners.clear()
                        pendingSong?.let { song ->
                            pendingSong = null
                            play(song)
                        }
                    }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    val player: Player?
        get() = controller

    fun addListener(listener: Player.Listener) {
        val connected = controller
        if (connected != null) connected.addListener(listener)
        else pendingListeners += listener
    }

    fun removeListener(listener: Player.Listener) {
        controller?.removeListener(listener)
        pendingListeners.remove(listener)
    }

    fun play(song: Song) {
        val connected = controller
        if (connected == null) {
            pendingSong = song
            return
        }

        val uri = song.contentUri ?: return
        val item = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.artworkUri)
                    .build()
            )
            .build()

        connected.setMediaItem(item)
        connected.prepare()
        connected.play()
    }

    fun pause() = controller?.pause()
    fun resume() = controller?.play()
    fun release() {
        MediaController.releaseFuture(controllerFuture)
        controller = null
        pendingSong = null
        pendingListeners.clear()
    }
}
