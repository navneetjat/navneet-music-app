package com.navneet.music.provider

/** Central registry for provider implementations. */
class ProviderRegistry(
    providers: List<MusicProvider> = emptyList()
) {
    private val items = providers.toMutableList()

    fun all(): List<MusicProvider> = items.toList()

    fun register(provider: MusicProvider) {
        if (items.none { it.id == provider.id }) items += provider
    }

    fun find(id: String): MusicProvider? = items.firstOrNull { it.id == id }
}
