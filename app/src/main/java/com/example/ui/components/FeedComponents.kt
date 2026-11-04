package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.DownloadedVideoEntity
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

@Composable
fun AiCuratorBannerCard(
    greeting: String,
    insight: String,
    suggestedQueries: List<String>,
    isAiCurating: Boolean,
    onTriggerGeminiCuration: () -> Unit,
    onQuickQueryClick: (String) -> Unit,
    onOpenUrlDownloader: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(CrimsonPulse, CyberCyan, AmberVu)),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("ai_curator_banner")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_curator_banner_1791571091331),
                contentDescription = "Иллюстрация ИИ-студии",
                contentScale = ContentScale.Crop,
                alpha = 0.25f,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ObsidianBg.copy(alpha = 0.72f),
                                ObsidianCard.copy(alpha = 0.94f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = EmeraldShield.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldShield,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "0% РЕКЛАМЫ • ЛОКАЛЬНЫЙ ИИ",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldShield
                            )
                        }
                    }

                    Surface(
                        color = AmberVu.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable(onClick = onOpenUrlDownloader)
                            .testTag("banner_url_downloader_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = AmberVu,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Скачать по URL",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberVu
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick AI Search Chips based on local tastes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedQueries.forEach { q ->
                        FilterChip(
                            selected = false,
                            onClick = { onQuickQueryClick(q) },
                            label = {
                                Text(
                                    text = q,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ObsidianSurface,
                                labelColor = CyberCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onTriggerGeminiCuration,
                    enabled = !isAiCurating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPulse,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_curate_feed_button")
                ) {
                    if (isAiCurating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini 3.5 Flash перестраивает ленту...", style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ИИ-Пересборка ленты (Gemini 3.5 Flash)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoFeedCard(
    video: VideoItem,
    downloadEntry: DownloadedVideoEntity?,
    onPlayClick: () -> Unit,
    onQuickDownloadClick: () -> Unit,
    onBoostTopicClick: () -> Unit,
    onBlockTopicClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isDownloaded = downloadEntry?.status == "COMPLETED"
    val isDownloading = downloadEntry?.status == "DOWNLOADING"

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianElevated, RoundedCornerShape(18.dp))
            .clickable(onClick = onPlayClick)
            .testTag("video_card_${video.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 16:9 Thumbnail with AI Match Badge, Ad-Free Shield, and Duration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(ObsidianCard)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.img_ai_curator_banner_1791571091331),
                    placeholder = painterResource(id = R.drawable.img_ai_curator_banner_1791571091331),
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient scrim for badges
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBg.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    ObsidianBg.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Top-left AI Match Score Badge
                Surface(
                    color = ObsidianBg.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .border(1.dp, CyberCyan.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${video.aiMatchScore}% ИИ-совпадение",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                }

                // Top-right Ad-Free & SponsorBlock Badge
                Surface(
                    color = ObsidianBg.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldShield,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Без рекламы",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldShield
                        )
                    }
                }

                // Center Play Icon Overlay
                Surface(
                    color = CrimsonPulse.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Смотреть",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom-right Duration Badge
                Surface(
                    color = ObsidianBg.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = video.formattedDuration,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Video Info + Quick Download Button + Context Menu with built-in Downloader
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = video.aiReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${video.author} • ${video.viewsText} • ${video.publishedText}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    // Context Menu (`⋮`) with Video Downloader right at the top!
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("card_menu_${video.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Меню видео и скачиватель",
                                tint = TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(ObsidianCard)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Скачать видео (1080p / 720p / M4A)",
                                        fontWeight = FontWeight.Bold,
                                        color = AmberVu
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = AmberVu)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onQuickDownloadClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Больше таких видео (+Вес в ИИ)", color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = CyberCyan)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onBoostTopicClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Смотреть позже (В закладки)", color = TextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = EmeraldShield)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onBookmarkClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Скрыть тему (В блок-фильтр)", color = CrimsonPulse) },
                                leadingIcon = {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = CrimsonPulse)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onBlockTopicClick()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Download Progress if currently downloading
                if (isDownloading && downloadEntry != null) {
                    val pct = if (downloadEntry.totalBytes > 0) {
                        (downloadEntry.downloadedBytes.toFloat() / downloadEntry.totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else 0.35f
                    LinearProgressIndicator(
                        progress = { pct },
                        color = AmberVu,
                        trackColor = ObsidianElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // Bottom Action Bar on Card: Watch Ad-Free + Instant Download Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPulse,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDownloaded) "Смотреть офлайн" else "Смотреть",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = onQuickDownloadClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDownloaded) EmeraldShield.copy(alpha = 0.2f) else AmberVu,
                            contentColor = if (isDownloaded) EmeraldShield else ObsidianBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_download_button_${video.id}")
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = "Скачать видео",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isDownloaded -> "Скачано MP4"
                                isDownloading -> "Качается..."
                                else -> "Скачать MP4"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
