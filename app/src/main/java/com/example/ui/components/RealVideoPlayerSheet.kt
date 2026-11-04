package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.download.LocalHardwareMp4Generator
import com.example.data.local.DownloadedVideoEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.AiVideoSummaryResult
import com.example.data.model.VideoItem
import com.example.ui.theme.AmberVu
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldShield
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.sin

private enum class VideoEngineMode {
    NATIVE_MP4_TEXTURE,
    YOUTUBE_MOBILE_WEB,
    YOUTUBE_EMBED_WEB
}

private val streamHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

/** Checks the ISO-BMFF file signature; an HLS fragment or HTML error page is not an MP4. */
private fun isValidMp4File(file: File): Boolean {
    if (!file.isFile || file.length() < 16L) return false
    return try {
        FileInputStream(file).use { input ->
            val header = ByteArray(12)
            if (input.read(header) != header.size) return false
            header[4] == 'f'.code.toByte() &&
                header[5] == 't'.code.toByte() &&
                header[6] == 'y'.code.toByte() &&
                header[7] == 'p'.code.toByte()
        }
    } catch (_: Exception) {
        false
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealVideoPlayerScreen(
    video: VideoItem,
    downloadedEntry: DownloadedVideoEntity?,
    historyEntry: WatchHistoryEntity?,
    aiSummary: AiVideoSummaryResult?,
    isSummarizing: Boolean,
    relatedVideos: List<VideoItem>,
    onClose: () -> Unit,
    onDownloadClick: (VideoItem) -> Unit,
    onLikeClick: (VideoItem) -> Unit,
    onBookmarkClick: (VideoItem) -> Unit,
    onRequestAiSummary: (VideoItem) -> Unit,
    onSelectRelatedVideo: (VideoItem) -> Unit
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current

    val localFileAvailable = remember(downloadedEntry) {
        downloadedEntry?.status == "COMPLETED" &&
            downloadedEntry.localFilePath.isNotBlank() &&
            File(downloadedEntry.localFilePath).exists() &&
            File(downloadedEntry.localFilePath).length() > 4096L
    }

    var engineMode by remember(video.id) {
        mutableStateOf(VideoEngineMode.NATIVE_MP4_TEXTURE)
    }

    // Guaranteed local MP4 file on disk so MediaPlayer plays via FileDescriptor with 0 network stalls!
    var playableLocalFile by remember(video.id) { mutableStateOf<File?>(null) }
    var isUsingSynthesizedStream by remember(video.id) { mutableStateOf(false) }

    var activeSurface by remember { mutableStateOf<Surface?>(null) }
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var bufferProgressPct by remember(video.id) { mutableIntStateOf(0) }
    var currentPositionSec by remember { mutableIntStateOf(0) }
    var durationSec by remember { mutableIntStateOf(video.durationSeconds.coerceAtLeast(15)) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var sponsorSkippedBanner by remember { mutableStateOf<String?>(null) }
    var videoResolutionLabel by remember { mutableStateOf("HD MP4") }

    // Step 1: Download only the actual stream URL, then verify that it is a complete MP4.
    // HLS playlist fragments must never be renamed to .mp4 and sent to MediaPlayer.
    LaunchedEffect(video.id, video.streamUrl, localFileAvailable, downloadedEntry?.localFilePath) {
        playableLocalFile = null
        isUsingSynthesizedStream = false
        isBuffering = true
        bufferProgressPct = 0

        if (localFileAvailable && downloadedEntry != null) {
            val downloadedFile = File(downloadedEntry.localFilePath)
            if (isValidMp4File(downloadedFile)) {
                playableLocalFile = downloadedFile
                bufferProgressPct = 100
                isBuffering = false
            } else {
                isBuffering = false
            }
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val cacheFolder = File(context.cacheDir, "novatube_real_v4_streams")
            if (!cacheFolder.exists()) cacheFolder.mkdirs()

            val safeId = video.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val netCacheFile = File(cacheFolder, "${safeId}_real.mp4")
            if (isValidMp4File(netCacheFile)) {
                playableLocalFile = netCacheFile
                bufferProgressPct = 100
                isBuffering = false
                return@withContext
            } else if (netCacheFile.exists()) {
                netCacheFile.delete()
            }

            val url = video.streamUrl.trim()
            val normalizedPath = url.substringBefore("?").substringBefore("#").lowercase()
            val isUnsupportedSegment = normalizedPath.contains("streaming-playlists") ||
                normalizedPath.contains("fragmented") ||
                normalizedPath.endsWith(".m3u8") ||
                normalizedPath.endsWith(".mpd")

            if (!url.startsWith("https://") && !url.startsWith("http://") || isUnsupportedSegment) {
                isBuffering = false
                return@withContext
            }

            val tempNet = File(cacheFolder, "${safeId}_real.tmp")
            try {
                tempNet.delete()
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) Chrome/124.0.0.0 Mobile")
                    .build()

                streamHttpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
                    val body = resp.body ?: throw IllegalStateException("Empty response")
                    val contentType = body.contentType()?.toString()?.lowercase().orEmpty()
                    if (contentType.contains("mpegurl") || contentType.contains("dash+xml") ||
                        contentType.contains("text/html")
                    ) {
                        throw IllegalStateException("The URL is not a direct MP4 file")
                    }

                    val expectedLen = body.contentLength()
                    body.byteStream().use { input ->
                        FileOutputStream(tempNet).use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var total = 0L
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                                total += count
                                if (expectedLen > 0L) {
                                    bufferProgressPct = ((total * 100L) / expectedLen)
                                        .toInt().coerceIn(1, 99)
                                }
                            }
                            output.fd.sync()
                        }
                    }

                    if (expectedLen > 0L && tempNet.length() != expectedLen) {
                        throw IllegalStateException("Incomplete MP4 response")
                    }
                    if (!isValidMp4File(tempNet)) {
                        throw IllegalStateException("Response is not a complete MP4 file")
                    }
                }

                if (netCacheFile.exists()) netCacheFile.delete()
                if (!tempNet.renameTo(netCacheFile)) {
                    tempNet.copyTo(netCacheFile, overwrite = true)
                    tempNet.delete()
                }
                if (!isValidMp4File(netCacheFile)) {
                    netCacheFile.delete()
                    throw IllegalStateException("Cached MP4 validation failed")
                }
                playableLocalFile = netCacheFile
                bufferProgressPct = 100
            } catch (_: Exception) {
                tempNet.delete()
            } finally {
                isBuffering = false
            }
        }
    }

    // Step 2: Bind MediaPlayer to playableLocalFile using FileInputStream.fd (100% immune to Stagefright HTTP bugs!)
    DisposableEffect(video.id, playableLocalFile?.absolutePath, activeSurface, engineMode) {
        val surface = activeSurface
        val fileToPlay = playableLocalFile
        if (engineMode != VideoEngineMode.NATIVE_MP4_TEXTURE ||
            surface == null ||
            !surface.isValid ||
            fileToPlay == null ||
            !fileToPlay.exists()
        ) {
            onDispose { }
        } else {
            val mp = MediaPlayer()
            mediaPlayerRef = mp
            var fis: FileInputStream? = null

            try {
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build()
                )
                mp.setScreenOnWhilePlaying(true)
                mp.setSurface(surface)

                fis = FileInputStream(fileToPlay)
                mp.setDataSource(fis.fd)

                mp.setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                    if (vWidth > 0 && vHeight > 0) {
                        videoResolutionLabel = "${vWidth}x${vHeight}"
                        textureViewRef?.let { tv ->
                            applyAspectRatioMatrix(tv, vWidth, vHeight)
                        }
                    }
                }

                mp.setOnPreparedListener { preparedMp ->
                    preparedMp.isLooping = true
                    isBuffering = false
                    val vW = preparedMp.videoWidth
                    val vH = preparedMp.videoHeight
                    if (vW > 0 && vH > 0) {
                        videoResolutionLabel = "${vW}x${vH}"
                        textureViewRef?.let { tv ->
                            applyAspectRatioMatrix(tv, vW, vH)
                        }
                    }
                    val total = preparedMp.duration / 1000
                    if (total > 0) durationSec = total
                    preparedMp.start()
                    isPlaying = true
                }

                mp.setOnErrorListener { _, _, _ ->
                    isBuffering = false
                    true
                }

                mp.prepareAsync()
            } catch (_: Exception) {
                isBuffering = false
            }

            onDispose {
                try {
                    if (mp.isPlaying) mp.stop()
                } catch (_: Exception) {
                }
                try {
                    mp.setSurface(null)
                    mp.release()
                } catch (_: Exception) {
                }
                try {
                    fis?.close()
                } catch (_: Exception) {
                }
                if (mediaPlayerRef === mp) {
                    mediaPlayerRef = null
                }
            }
        }
    }

    // Ambient synth sound generator when playing synthesized local hardware MP4 stream
    LaunchedEffect(video.id, isUsingSynthesizedStream, isPlaying, engineMode) {
        if (engineMode == VideoEngineMode.NATIVE_MP4_TEXTURE && isUsingSynthesizedStream && isPlaying) {
            withContext(Dispatchers.IO) {
                var audioTrack: AudioTrack? = null
                try {
                    val sampleRate = 22050
                    val minBuf = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(2048)

                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBuf)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    audioTrack.play()
                    val chordFreqs = listOf(110.0, 146.83, 164.81, 220.0)
                    val chunk = ShortArray(1024)
                    var phase = 0L
                    while (isActive) {
                        val noteFreq = chordFreqs[((phase / 8000) % chordFreqs.size).toInt()]
                        for (i in chunk.indices) {
                            val t = (phase + i).toDouble() / sampleRate
                            val wave = (sin(2.0 * Math.PI * noteFreq * t) * 0.6 +
                                sin(2.0 * Math.PI * (noteFreq * 1.5) * t) * 0.4)
                            chunk[i] = (wave * 2400).toInt().toShort()
                        }
                        phase += chunk.size
                        audioTrack.write(chunk, 0, chunk.size)
                    }
                } catch (_: Exception) {
                } finally {
                    try {
                        audioTrack?.stop()
                        audioTrack?.release()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    // Progress & SponsorBlock auto-skip loop
    LaunchedEffect(mediaPlayerRef, video.id, engineMode) {
        if (engineMode == VideoEngineMode.NATIVE_MP4_TEXTURE) {
            while (true) {
                val mp = mediaPlayerRef
                try {
                    if (mp != null && mp.isPlaying) {
                        isBuffering = false
                        val posSec = mp.currentPosition / 1000
                        currentPositionSec = posSec
                        val totalSec = mp.duration / 1000
                        if (totalSec > 0) durationSec = totalSec

                        val hitSegment = video.sponsorSegments.firstOrNull { seg ->
                            posSec in seg.startSec until seg.endSec
                        }
                        if (hitSegment != null) {
                            mp.seekTo(hitSegment.endSec * 1000)
                            sponsorSkippedBanner = "SponsorBlock: пропущен сегмент «${hitSegment.label}»"
                        }
                    }
                } catch (_: Exception) {
                }
                delay(400)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("video_player_screen")
    ) {
        // Top Player Bar with Back button and instant Download trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_player_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад в ленту",
                        tint = TextPrimary
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldShield,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                localFileAvailable && engineMode == VideoEngineMode.NATIVE_MP4_TEXTURE ->
                                    "ОФЛАЙН MP4 ($videoResolutionLabel)"
                                engineMode == VideoEngineMode.NATIVE_MP4_TEXTURE ->
                                    "MP4 ПОТОК ($videoResolutionLabel) • SPONSORBLOCK"
                                engineMode == VideoEngineMode.YOUTUBE_MOBILE_WEB ->
                                    "YOUTUBE MOBILE • ADGUARD"
                                else -> "YOUTUBE EMBED • ADGUARD"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (localFileAvailable) AmberVu else EmeraldShield
                        )
                    }
                    Text(
                        text = video.sourcePlatform,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = { onDownloadClick(video) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (localFileAvailable) EmeraldShield.copy(alpha = 0.2f) else AmberVu,
                    contentColor = if (localFileAvailable) EmeraldShield else ObsidianBg
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("player_top_download_button")
            ) {
                Icon(
                    imageVector = if (localFileAvailable) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = "Скачать видео",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (localFileAvailable) "Скачано" else "Скачать",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Video Engine Switcher Bar
        if (video.youtubeId.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianCard)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = engineMode == VideoEngineMode.NATIVE_MP4_TEXTURE,
                    onClick = { engineMode = VideoEngineMode.NATIVE_MP4_TEXTURE },
                    label = { Text("MP4 Видео (HW)", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldShield,
                        selectedLabelColor = ObsidianBg
                    )
                )
                FilterChip(
                    selected = engineMode == VideoEngineMode.YOUTUBE_MOBILE_WEB,
                    onClick = { engineMode = VideoEngineMode.YOUTUBE_MOBILE_WEB },
                    label = { Text("YouTube Прямой", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonPulse,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = engineMode == VideoEngineMode.YOUTUBE_EMBED_WEB,
                    onClick = { engineMode = VideoEngineMode.YOUTUBE_EMBED_WEB },
                    label = { Text("YouTube Embed", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = ObsidianBg
                    )
                )
            }
        }

        // 16:9 Hardware-Accelerated Video Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when (engineMode) {
                VideoEngineMode.NATIVE_MP4_TEXTURE -> {
                    AndroidView(
                        factory = { ctx ->
                            TextureView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                textureViewRef = this
                                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                    override fun onSurfaceTextureAvailable(
                                        st: SurfaceTexture,
                                        width: Int,
                                        height: Int
                                    ) {
                                        activeSurface?.release()
                                        activeSurface = Surface(st)
                                    }

                                    override fun onSurfaceTextureSizeChanged(
                                        st: SurfaceTexture,
                                        width: Int,
                                        height: Int
                                    ) {
                                        val mp = mediaPlayerRef
                                        if (mp != null) {
                                            try {
                                                if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                                                    applyAspectRatioMatrix(this@apply, mp.videoWidth, mp.videoHeight)
                                                }
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }

                                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                        try {
                                            mediaPlayerRef?.setSurface(null)
                                        } catch (_: Exception) {
                                        }
                                        activeSurface?.release()
                                        activeSurface = null
                                        return true
                                    }

                                    override fun onSurfaceTextureUpdated(st: SurfaceTexture) {
                                        if (isBuffering) {
                                            isBuffering = false
                                        }
                                    }
                                }
                            }
                        },
                        update = {
                            // Never re-call setSurface in update!
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isBuffering) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(ObsidianBg.copy(alpha = 0.78f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = CrimsonPulse,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (bufferProgressPct in 1..99) {
                                    "Загрузка реального видеопотока: $bufferProgressPct%"
                                } else {
                                    "Подготовка видеопотока..."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }

                    if (sponsorSkippedBanner != null) {
                        Surface(
                            color = ObsidianBg.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .border(1.dp, CyberCyan, RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sponsorSkippedBanner.orEmpty(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }

                VideoEngineMode.YOUTUBE_MOBILE_WEB,
                VideoEngineMode.YOUTUBE_EMBED_WEB -> {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                setBackgroundColor(android.graphics.Color.BLACK)
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.loadsImagesAutomatically = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.userAgentString =
                                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                                webChromeClient = object : WebChromeClient() {
                                    override fun getDefaultVideoPoster(): Bitmap {
                                        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    private val adHosts = listOf(
                                        "doubleclick.net",
                                        "googlesyndication.com",
                                        "googleads",
                                        "pagead",
                                        "adservice.google"
                                    )

                                    override fun shouldInterceptRequest(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): WebResourceResponse? {
                                        val reqUrl = request?.url?.toString()?.lowercase().orEmpty()
                                        if (adHosts.any { reqUrl.contains(it) }) {
                                            return WebResourceResponse(
                                                "text/plain",
                                                "UTF-8",
                                                ByteArrayInputStream(ByteArray(0))
                                            )
                                        }
                                        return super.shouldInterceptRequest(view, request)
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                var style = document.createElement('style');
                                                style.innerHTML = 'ytm-mobile-topbar-renderer, ytm-WatchMetadataRenderer, ytm-item-section-renderer, ytm-companion-ad-renderer, .ad-showing { display: none !important; } body { background: #000 !important; }';
                                                document.head.appendChild(style);
                                                var v = document.querySelector('video');
                                                if (v) { v.muted = false; v.play(); }
                                            })();
                                            """.trimIndent(),
                                            null
                                        )
                                    }
                                }
                            }
                        },
                        update = { webView ->
                            val targetTag = "${engineMode.name}_${video.youtubeId}"
                            if (webView.tag != targetTag) {
                                webView.tag = targetTag
                                if (engineMode == VideoEngineMode.YOUTUBE_MOBILE_WEB) {
                                    webView.loadUrl("https://m.youtube.com/watch?v=${video.youtubeId}&autoplay=1")
                                } else {
                                    val embedHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                        <style>
                                          html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
                                          iframe { width: 100%; height: 100%; border: 0; display: block; }
                                        </style>
                                        </head>
                                        <body>
                                          <iframe src="https://www.youtube.com/embed/${video.youtubeId}?autoplay=1&rel=0&modestbranding=1&playsinline=1"
                                                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                                  allowfullscreen></iframe>
                                        </body>
                                        </html>
                                    """.trimIndent()
                                    webView.loadDataWithBaseURL(
                                        "https://www.youtube.com",
                                        embedHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Native Transport & Speed Control Bar when in Native/Offline MP4 Texture mode
        if (engineMode == VideoEngineMode.NATIVE_MP4_TEXTURE) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Slider(
                    value = currentPositionSec.toFloat().coerceIn(0f, durationSec.toFloat().coerceAtLeast(1f)),
                    onValueChange = { newSec ->
                        currentPositionSec = newSec.toInt()
                        try {
                            mediaPlayerRef?.seekTo(currentPositionSec * 1000)
                        } catch (_: Exception) {
                        }
                    },
                    valueRange = 0f..durationSec.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPulse,
                        activeTrackColor = CrimsonPulse,
                        inactiveTrackColor = ObsidianElevated
                    ),
                    modifier = Modifier.height(24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val mp = mediaPlayerRef ?: return@IconButton
                                try {
                                    if (mp.isPlaying) {
                                        mp.pause()
                                        isPlaying = false
                                    } else {
                                        mp.start()
                                        isPlaying = true
                                    }
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier.testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                                tint = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                val target = (currentPositionSec - 10).coerceAtLeast(0)
                                currentPositionSec = target
                                try {
                                    mediaPlayerRef?.seekTo(target * 1000)
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Назад 10 сек",
                                tint = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                val target = (currentPositionSec + 10).coerceAtMost(durationSec)
                                currentPositionSec = target
                                try {
                                    mediaPlayerRef?.seekTo(target * 1000)
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Вперёд 10 сек",
                                tint = TextSecondary
                            )
                        }

                        Text(
                            text = "%02d:%02d / %02d:%02d".format(
                                currentPositionSec / 60,
                                currentPositionSec % 60,
                                durationSec / 60,
                                durationSec % 60
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            val nextSpeed = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                2.0f -> 0.75f
                                else -> 1.0f
                            }
                            playbackSpeed = nextSpeed
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    val mp = mediaPlayerRef
                                    if (mp != null) {
                                        mp.playbackParams = mp.playbackParams.setSpeed(nextSpeed)
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Скорость",
                                tint = CyberCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${playbackSpeed}x",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberCyan
                            )
                        }
                    }
                }
            }
        }

        // Scrollable Details, AI Personalization Badge, Actions, Gemini Summary, and Related Videos
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Surface(
                color = CyberCyan.copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = video.aiReason,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            }

            Text(
                text = video.title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${video.author} • ${video.viewsText} • ${video.publishedText}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isLiked = historyEntry?.isLiked == true
                val isBookmarked = historyEntry?.isBookmarked == true

                OutlinedButton(
                    onClick = { onLikeClick(video) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("like_video_button")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Нравится",
                        tint = if (isLiked) CrimsonPulse else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLiked) "Вкусы +4%" else "Нравится",
                        color = if (isLiked) CrimsonPulse else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = { onBookmarkClick(video) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("bookmark_video_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "В закладки",
                        tint = if (isBookmarked) AmberVu else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = { onDownloadClick(video) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberVu,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("player_action_download_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Скачать MP4",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            if (downloadedEntry != null && downloadedEntry.status == "DOWNLOADING") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val progress = if (downloadedEntry.totalBytes > 0) {
                            (downloadedEntry.downloadedBytes.toFloat() / downloadedEntry.totalBytes.toFloat())
                                .coerceIn(0f, 1f)
                        } else 0.3f
                        Text(
                            text = "Скачивание файла (${downloadedEntry.quality}) • ${downloadedEntry.speedKbps} КБ/с",
                            style = MaterialTheme.typography.labelLarge,
                            color = AmberVu
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = AmberVu,
                            trackColor = ObsidianElevated,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gemini AI De-Bloat & Key Takeaways Card ("ИИ-Разбор без воды")
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(CyberCyan, CrimsonPulse)),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Gemini 3.5 Flash • Разбор без воды",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Суть, технические факты и таймкоды",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { onRequestAiSummary(video) },
                            enabled = !isSummarizing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ai_summary_button")
                        ) {
                            if (isSummarizing) {
                                CircularProgressIndicator(
                                    color = ObsidianBg,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = if (aiSummary == null) "Сжать суть" else "Обновить",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = aiSummary != null) {
                        aiSummary?.let { summary ->
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = ObsidianElevated)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = summary.oneLineEssence,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "КЛЮЧЕВЫЕ ВЫВОДЫ:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyberCyan
                                )
                                summary.keyTakeaways.forEach { point ->
                                    Text(
                                        text = "• $point",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ЧИСТЫЕ ТАЙМКОДЫ (БЕЗ ИНТЕГРАЦИЙ):",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AmberVu
                                )
                                summary.cleanChapters.forEach { ch ->
                                    Text(
                                        text = ch,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Описание из YouTube и локальные теги",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        video.tags.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = ObsidianCard,
                                    labelColor = CyberCyan
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Далее из базы YouTube по вашим вкусам",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            relatedVideos.take(4).forEach { rel ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onSelectRelatedVideo(rel) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rel.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${rel.author} • ${rel.aiMatchScore}% совпадение",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                        IconButton(onClick = { onDownloadClick(rel) }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Скачать",
                                tint = AmberVu
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun applyAspectRatioMatrix(
    textureView: TextureView,
    videoWidth: Int,
    videoHeight: Int
) {
    val viewWidth = textureView.width.toFloat()
    val viewHeight = textureView.height.toFloat()
    if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0 || videoHeight <= 0) return

    val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
    val viewRatio = viewWidth / viewHeight

    val scaleX: Float
    val scaleY: Float
    if (videoRatio > viewRatio) {
        scaleX = 1f
        scaleY = viewRatio / videoRatio
    } else {
        scaleX = videoRatio / viewRatio
        scaleY = 1f
    }

    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    textureView.setTransform(matrix)
}
