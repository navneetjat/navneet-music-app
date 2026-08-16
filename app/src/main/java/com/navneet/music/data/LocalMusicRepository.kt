package com.navneet.music.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.navneet.music.model.Song

class LocalMusicRepository(private val context: Context) {
    fun scan(): List<Song> {
        val result = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        context.contentResolver.query(collection, projection, selection, null, "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC")?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            while (c.moveToNext()) {
                val mediaId = c.getLong(id)
                val albumIdValue = c.getLong(albumId)
                result += Song(
                    id = mediaId.toString(),
                    title = c.getString(title).orEmpty(),
                    artist = c.getString(artist).orEmpty().ifBlank { "Unknown artist" },
                    album = c.getString(album).orEmpty().ifBlank { "Unknown album" },
                    durationMs = c.getLong(duration),
                    artworkUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumIdValue),
                    contentUri = ContentUris.withAppendedId(collection, mediaId)
                )
            }
        }
        return result
    }
}
