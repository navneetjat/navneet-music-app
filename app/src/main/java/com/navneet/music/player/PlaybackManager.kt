package com.navneet.music.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.navneet.music.model.Song

class PlaybackManager(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    fun play(song: Song) {
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
        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    fun release() { player.release() }
}
