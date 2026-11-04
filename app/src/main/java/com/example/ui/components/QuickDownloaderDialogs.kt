package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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

/**
 * Instant Menu Video Downloader BottomSheet that opens whenever the user taps "Скачать"
 * on any video card, in the ⋮ menu, or in the player.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickVideoDownloadModal(
    video: VideoItem,
    onDismiss: () -> Unit,
    onSelectQualityAndDownload: (qualityLabel: String, streamUrl: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary,
        modifier = Modifier.testTag("quick_download_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = AmberVu,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Скачиватель видео • Прямой поток",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Сохранение в память устройства (Movies/NovaTube)",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldShield
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ObsidianElevated)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ВЫБЕРИТЕ ФОРМАТ И КАЧЕСТВО:",
                style = MaterialTheme.typography.labelLarge,
                color = CyberCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            val options = video.qualityOptions.ifEmpty {
                listOf(
                    com.example.data.model.VideoQualityOption(
                        label = "1080p Full HD • MP4",
                        shortLabel = "1080p MP4",
                        estimatedSizeMb = 14.5,
                        streamUrl = video.streamUrl
                    ),
                    com.example.data.model.VideoQualityOption(
                        label = "720p HD • MP4",
                        shortLabel = "720p MP4",
                        estimatedSizeMb = 8.2,
                        streamUrl = video.streamUrl
                    )
                )
            }

            options.forEach { option ->
                val isAudio = option.shortLabel.contains("M4A", ignoreCase = true) ||
                    option.shortLabel.contains("Audio", ignoreCase = true)

                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(1.dp, ObsidianElevated, RoundedCornerShape(14.dp))
                        .clickable {
                            onSelectQualityAndDownload(option.shortLabel, option.streamUrl)
                        }
                        .testTag("download_quality_${option.shortLabel}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAudio) Icons.Default.AudioFile else Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = if (isAudio) CyberCyan else AmberVu,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "~${option.estimatedSizeMb} МБ • Прямая запись без рекламы",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            color = AmberVu,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = ObsidianBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Скачать",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ObsidianBg
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top-Menu Instant URL Video Downloader Dialog ("Скачиватель по ссылке сразу в меню").
 */
@Composable
fun UrlExtractorDownloaderDialog(
    onDismiss: () -> Unit,
    onStartUrlDownload: (url: String, quality: String) -> Unit
) {
    var urlText by remember {
        mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4")
    }
    var selectedQuality by remember { mutableStateOf("1080p MP4") }
    val qualities = listOf("1080p MP4", "720p MP4", "480p MP4", "Audio M4A")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberVu.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("url_downloader_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = AmberVu,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Быстрый Скачиватель по ссылке",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Вставьте прямую ссылку MP4, ссылку YouTube или Archive.org — видео сразу скачается в память устройства.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("URL видеопотока (MP4 / YouTube / Archive)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_url_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Быстрые тестовые потоки:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                urlText = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
                            }
                    ) {
                        Text(
                            text = "Ралли и Авто MP4",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                urlText = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                            }
                    ) {
                        Text(
                            text = "Техно Поток MP4",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    qualities.forEach { q ->
                        FilterChip(
                            selected = selectedQuality == q,
                            onClick = { selectedQuality = q },
                            label = { Text(q, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberVu,
                                selectedLabelColor = ObsidianBg
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onStartUrlDownload(urlText, selectedQuality) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberVu,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_url_download_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Скачать файл ($selectedQuality)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
