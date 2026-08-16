package com.navneet.music.model

import android.net.Uri

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artworkUri: Uri?,
    val contentUri: Uri?,
    val providerId: String = "local"
)

data class ProviderCapabilities(
    val search: Boolean = false,
    val streaming: Boolean = false,
    val fullLengthPlayback: Boolean = false,
    val lyrics: Boolean = false,
    val downloads: Boolean = false
)

data class MusicProviderInfo(
    val id: String,
    val name: String,
    val version: String,
    val capabilities: ProviderCapabilities
)
