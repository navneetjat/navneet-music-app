package com.navneet.music.provider

import com.navneet.music.model.MusicProviderInfo
import com.navneet.music.model.Song

interface MusicProvider {
    val info: MusicProviderInfo
    suspend fun search(query: String): List<Song>
    suspend fun resolveStream(song: Song): StreamResult
}

data class StreamResult(
    val songId: String,
    val uri: String,
    val mimeType: String? = null,
    val durationMs: Long? = null
)
