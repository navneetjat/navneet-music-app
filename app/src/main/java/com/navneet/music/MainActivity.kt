package com.navneet.music

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.navneet.music.data.LocalMusicRepository
import com.navneet.music.model.Song
import com.navneet.music.player.PlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NavneetMusicApp() }
    }
}

private val Background = Color(0xFF080817)
private val SurfaceColor = Color(0xFF15152A)
private val Accent = Color(0xFF00D7B3)

@Composable
fun NavneetMusicApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted = it }
    val repository = remember { LocalMusicRepository(context) }
    val playback = remember { PlaybackManager(context) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var current by remember { mutableStateOf<Song?>(null) }
    var playing by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }

    DisposableEffect(playback) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                current = songs.firstOrNull { it.id == mediaItem?.mediaId }
            }
        }
        playback.addListener(listener)
        onDispose {
            playback.removeListener(listener)
            playback.release()
        }
    }

    LaunchedEffect(Unit) { launcher.launch(permission) }
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) songs = withContext(Dispatchers.IO) { repository.scan() }
    }

    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(primary = Accent, background = Background, surface = SurfaceColor)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Scaffold(
                containerColor = Background,
                bottomBar = {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        current?.let { MiniPlayer(it, playing) { if (playing) playback.pause() else playback.resume() } }
                        NavigationBar(containerColor = Color(0xFF102D39)) {
                            val labels = listOf("Home", "Search", "Library", "Extensions", "Settings")
                            val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.LibraryMusic, Icons.Default.Extension, Icons.Default.Settings)
                            labels.forEachIndexed { index, label -> NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(icons[index], label) }, label = { Text(label) }) }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    when (tab) {
                        0 -> HomeScreen(songs) { current = it; playback.play(it) }
                        1 -> SearchScreen(songs) { current = it; playback.play(it) }
                        2 -> LibraryScreen(songs) { current = it; playback.play(it) }
                        3 -> ExtensionsScreen()
                        4 -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable private fun Header(title: String, subtitle: String? = null) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
        Text(title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        subtitle?.let { Text(it, color = Color(0xFFAAA9BE), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) }
    }
}

@Composable private fun HomeScreen(songs: List<Song>, onPlay: (Song) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Navneet Music", "Your local library + modular music providers") }
        item { Text("Your Music", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
        if (songs.isEmpty()) item { EmptyCard("No local music found", "Add music to your device or install a compatible provider extension.") }
        items(songs.take(10), key = { it.id }) { SongRow(it, onPlay) }
    }
}

@Composable private fun SearchScreen(songs: List<Song>, onPlay: (Song) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, songs) { if (query.isBlank()) emptyList() else songs.filter { "${it.title} ${it.artist} ${it.album}".contains(query, true) } }
    Column(Modifier.fillMaxSize()) {
        Header("Search", "Search your local library")
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search songs, artists, albums…") })
        if (query.isBlank()) EmptyCard("Start searching", "Online providers will appear here after their extension interfaces are connected.", Modifier.padding(24.dp))
        else if (results.isEmpty()) EmptyCard("No results", "Try another title or artist.", Modifier.padding(24.dp))
        else LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) { items(results, key = { it.id }) { SongRow(it, onPlay) } }
    }
}

@Composable private fun LibraryScreen(songs: List<Song>, onPlay: (Song) -> Unit) {
    var section by remember { mutableStateOf("Songs") }
    Column(Modifier.fillMaxSize()) {
        Header("Library")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Songs", "Favorites", "History", "Local", "Playlists", "Downloads").forEach { label ->
                Surface(color = if (section == label) Accent.copy(alpha = .18f) else SurfaceColor, shape = RoundedCornerShape(50), modifier = Modifier.clickable { section = label }) {
                    Text(label, color = if (section == label) Accent else Color.LightGray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (section == "Songs" || section == "Local") LazyColumn { items(songs, key = { it.id }) { SongRow(it, onPlay) } }
        else EmptyCard("Nothing here yet", "This section will populate as you use the app.", Modifier.padding(24.dp))
    }
}

@Composable private fun ExtensionsScreen() {
    Column(Modifier.fillMaxSize()) {
        Header("Music Extensions", "Providers are modular and independent from the player")
        EmptyCard("No music provider installed", "The app intentionally does not use fake/demo audio. Add a compatible provider implementation to enable online search and streaming.", Modifier.padding(24.dp))
    }
}

@Composable private fun SettingsScreen() {
    Column(Modifier.fillMaxSize()) {
        Header("Settings")
        SettingGroup("Appearance", listOf("Dark theme" to "Always on in the initial build"))
        SettingGroup("Playback", listOf("Media3 / ExoPlayer" to "Local and provider streams use one playback pipeline", "Background playback" to "MediaSessionService enabled"))
        SettingGroup("Data", listOf("Search history" to "Coming with persistent storage", "Cache" to "Coming in the data layer"))
        SettingGroup("About", listOf("Version" to "0.1.0", "Architecture" to "Kotlin + Compose + Media3 + provider extensions"))
    }
}

@Composable private fun SettingGroup(title: String, items: List<Pair<String, String>>) {
    Text(title, color = Accent, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(vertical = 8.dp)) { items.forEach { (name, desc) -> Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) { Text(name, color = Color.White, fontSize = 16.sp); Text(desc, color = Color(0xFFAAA9BE), fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp)) } } }
    }
}

@Composable private fun SongRow(song: Song, onPlay: (Song) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onPlay(song) }.padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF26263D)), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Color(0xFFB8B6D0)) }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(song.title, color = Color.White, fontSize = 16.sp, maxLines = 1); Text(song.artist, color = Color(0xFFAAA9BE), fontSize = 13.sp, maxLines = 1) }
        IconButton(onClick = { onPlay(song) }) { Icon(Icons.Default.PlayArrow, "Play", tint = Accent) }
    }
}

@Composable private fun MiniPlayer(song: Song, playing: Boolean, onPlayPause: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF454353)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF5B5969)), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Color.White) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(song.title, color = Color.White, maxLines = 1); Text(song.artist, color = Color(0xFFD0CED9), fontSize = 12.sp, maxLines = 1) }
        IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White) }
    }
}

@Composable private fun EmptyCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(22.dp)) { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Color(0xFFAAA9BE), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp)) }
    }
}
