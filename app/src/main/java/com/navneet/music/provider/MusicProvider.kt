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

class ProviderRegistry {
    private val providers = mutableListOf<MusicProvider>()
    fun register(provider: MusicProvider) { if (providers.none { it.info.id == provider.info.id }) providers += provider }
    fun all(): List<MusicProvider> = providers.toList()
    fun find(id: String): MusicProvider? = providers.firstOrNull { it.info.id == id }
}
