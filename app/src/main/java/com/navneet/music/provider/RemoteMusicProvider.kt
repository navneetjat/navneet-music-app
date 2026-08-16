package com.navneet.music.provider

import com.navneet.music.model.Song

/** Provider contract for a remote music source. */
interface RemoteMusicProvider : MusicProvider {
    suspend fun resolvePlayback(song: Song): PlaybackResolution
}

sealed interface PlaybackResolution {
    data class FullStream(val url: String, val mimeType: String? = null) : PlaybackResolution
    data class PreviewOnly(val url: String, val durationMs: Long) : PlaybackResolution
    data class Unavailable(val reason: String) : PlaybackResolution
}
