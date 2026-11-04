package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.DownloadedVideoEntity
import com.example.data.local.PreferenceTagEntity
import com.example.data.local.WatchHistoryEntity
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
 * Dedicated "Скачиватель" (Downloader) screen right in the main menu.
 */
@Composable
fun DownloaderHubScreen(
    downloads: List<DownloadedVideoEntity>,
    feedVideos: List<VideoItem>,
    onStartUrlDownload: (url: String, quality: String) -> Unit,
    onQuickDownloadVideo: (VideoItem) -> Unit,
    onPlayDownloadedVideo: (DownloadedVideoEntity) -> Unit,
    onDeleteDownload: (String) -> Unit
) {
    var customUrl by remember {
        mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4")
    }
    var selectedQuality by remember { mutableStateOf("1080p MP4") }
    val qualities = listOf("1080p MP4", "720p MP4", "480p MP4", "Audio M4A")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("downloader_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(AmberVu, CrimsonPulse)),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                text = "Прямой Скачиватель Видео (MP4 / M4A)",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "Реальная загрузка файлов в память устройства без рекламы",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("Ссылка на видео (MP4 / YouTube / Archive.org)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = AmberVu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("downloader_tab_url_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onStartUrlDownload(customUrl, selectedQuality) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberVu,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("downloader_tab_start_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Скачать в память ($selectedQuality)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Active & Completed Downloads in Room DB
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Загрузки на устройстве (${downloads.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Movies/NovaTube",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
            }
        }

        if (downloads.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AmberVu,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Пока нет скачанных файлов",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Нажмите кнопку «Скачать» на любом видео ниже или вставьте ссылку выше.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(downloads, key = { it.videoId }) { dl ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (dl.status == "COMPLETED") EmeraldShield.copy(alpha = 0.5f) else AmberVu.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dl.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val mbDone = dl.downloadedBytes / (1024.0 * 1024.0)
                                val mbTotal = dl.totalBytes / (1024.0 * 1024.0)
                                Text(
                                    text = when (dl.status) {
                                        "COMPLETED" -> "ГОТОВО • ${dl.quality} • %.2f МБ • ${dl.speedKbps} КБ/с".format(mbDone)
                                        "FAILED" -> "ОШИБКА: ${dl.errorMessage ?: "Сбой сети"}"
                                        else -> "СКАЧИВАНИЕ • %.2f / %.2f МБ • ${dl.speedKbps} КБ/с".format(mbDone, mbTotal)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (dl.status) {
                                        "COMPLETED" -> EmeraldShield
                                        "FAILED" -> CrimsonPulse
                                        else -> AmberVu
                                    }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (dl.status == "COMPLETED") {
                                    Button(
                                        onClick = { onPlayDownloadedVideo(dl) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldShield,
                                            contentColor = ObsidianBg
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Офлайн", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                IconButton(onClick = { onDeleteDownload(dl.videoId) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить файл",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        }

                        if (dl.status == "DOWNLOADING") {
                            Spacer(modifier = Modifier.height(8.dp))
                            val pct = if (dl.totalBytes > 0) {
                                (dl.downloadedBytes.toFloat() / dl.totalBytes.toFloat()).coerceIn(0f, 1f)
                            } else 0.4f
                            LinearProgressIndicator(
                                progress = { pct },
                                color = AmberVu,
                                trackColor = ObsidianElevated,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Путь: ${dl.localFilePath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Quick 1-Click Download List from Feed right in the Downloader Tab
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Быстрое скачивание из вашей ИИ-ленты",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        items(feedVideos, key = { "quick_dl_${it.id}" }) { video ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
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
                            text = video.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${video.author} • ${video.formattedDuration} • Direct MP4",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onQuickDownloadVideo(video) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberVu,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MP4", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/**
 * Local AI Personalization Engine ("ИИ-Вкусы") backed by Room DB and Gemini 3.5 Flash.
 */
@Composable
fun AiPersonalizationBrainScreen(
    preferenceTags: List<PreferenceTagEntity>,
    aiInsight: String,
    isAiCurating: Boolean,
    onTriggerGeminiReRank: () -> Unit,
    onApplyNaturalLanguageCommand: (String) -> Unit,
    onAddTag: (tag: String, category: String, weight: Int, isBlocked: Boolean) -> Unit,
    onUpdateTagWeight: (id: Int, weight: Int, isBlocked: Boolean) -> Unit,
    onDeleteTag: (id: Int) -> Unit
) {
    var naturalRuleText by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }
    var newTagCategory by remember { mutableStateOf("Код и Железо") }
    var newTagIsBlocked by remember { mutableStateOf(false) }

    val positiveTags = preferenceTags.filter { !it.isBlocked }
    val blockedTags = preferenceTags.filter { it.isBlocked }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("ai_brain_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card with Gemini AI Re-Rank and Natural Language Command
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(CyberCyan, CrimsonPulse)),
                        RoundedCornerShape(18.dp)
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
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Локальный Мозг Персонализации",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "100% в Room БД на устройстве + Gemini 3.5 Flash",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = aiInsight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = naturalRuleText,
                        onValueChange = { naturalRuleText = it },
                        label = { Text("Скажите ИИ, что добавить или скрыть...") },
                        placeholder = { Text("Напр.: Больше про ГАЗ-24-10 и Python, убери роблокс") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_natural_rule_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (naturalRuleText.isNotBlank()) {
                                    onApplyNaturalLanguageCommand(naturalRuleText)
                                    naturalRuleText = ""
                                } else {
                                    onTriggerGeminiReRank()
                                }
                            },
                            enabled = !isAiCurating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("apply_ai_rule_button")
                        ) {
                            if (isAiCurating) {
                                CircularProgressIndicator(
                                    color = ObsidianBg,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (naturalRuleText.isNotBlank()) "Применить правило ИИ" else "ИИ-Пересборка ленты",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Manual Add Local Preference / Block Tag Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Добавить интерес или стоп-слово в локальную БД",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Тема (напр. STM32, Архитектура ЭВМ, NFS Underground)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_preference_tag_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = newTagIsBlocked,
                                onCheckedChange = { newTagIsBlocked = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CrimsonPulse,
                                    checkedTrackColor = CrimsonPulse.copy(alpha = 0.3f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (newTagIsBlocked) "Режим: Блок-фильтр" else "Режим: Любимая тема",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (newTagIsBlocked) CrimsonPulse else EmeraldShield
                            )
                        }

                        Button(
                            onClick = {
                                if (newTagName.isNotBlank()) {
                                    onAddTag(
                                        newTagName,
                                        if (newTagIsBlocked) "Блок-фильтр" else newTagCategory,
                                        if (newTagIsBlocked) -100 else 95,
                                        newTagIsBlocked
                                    )
                                    newTagName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (newTagIsBlocked) CrimsonPulse else EmeraldShield,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_preference_tag_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (newTagIsBlocked) "В блок" else "Добавить", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Blocked Topics Firewall Section
        item {
            Text(
                text = "Блок-фильтр тем (Никогда не показывать: ${blockedTags.size})",
                style = MaterialTheme.typography.titleMedium,
                color = CrimsonPulse
            )
        }

        items(blockedTags, key = { "blocked_${it.id}" }) { bTag ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CrimsonPulse.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = CrimsonPulse
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = bTag.tag,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Полная фильтрация из ленты и поиска",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonPulse
                            )
                        }
                    }
                    IconButton(onClick = { onDeleteTag(bTag.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить из фильтра",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Positive Local Preference Weights
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ваши локальные предпочтения и веса (${positiveTags.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        items(positiveTags, key = { "pos_${it.id}" }) { tag ->
            var sliderVal by remember(tag.id, tag.weight) {
                mutableFloatStateOf(tag.weight.toFloat().coerceIn(5f, 100f))
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tag.tag,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Категория: ${tag.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = CyberCyan.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${sliderVal.toInt()}% приоритет",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyberCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(onClick = { onDeleteTag(tag.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить тег",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    Slider(
                        value = sliderVal,
                        onValueChange = { sliderVal = it },
                        onValueChangeFinished = {
                            onUpdateTagWeight(tag.id, sliderVal.toInt(), false)
                        },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = ObsidianElevated
                        )
                    )
                }
            }
        }
    }
}

/**
 * Local Library Screen ("Библиотека"): AdBlock stats, Bookmarks, Liked videos, and Watch History.
 */
@Composable
fun LocalLibraryScreen(
    history: List<WatchHistoryEntity>,
    adsBlockedCount: Int,
    onOpenVideoById: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    val bookmarked = history.filter { it.isBookmarked }
    val liked = history.filter { it.isLiked }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("library_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Shield Stats Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldShield.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldShield,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Защита без рекламы активна",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Вырезано рекламных прероллов и спонсоров: $adsBlockedCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldShield
                            )
                        }
                    }
                }
            }
        }

        // Bookmarked / Watch Later
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = AmberVu)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Смотреть позже (${bookmarked.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }

        if (bookmarked.isEmpty()) {
            item {
                Text(
                    text = "Отмечайте видео флажком закладки в плеере, чтобы сохранить их сюда.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            items(bookmarked, key = { "bm_${it.videoId}" }) { item ->
                HistoryVideoRow(item = item, onClick = { onOpenVideoById(item.videoId) })
            }
        }

        // Liked Videos
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = CrimsonPulse)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Понравившиеся видео (${liked.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }

        if (liked.isEmpty()) {
            item {
                Text(
                    text = "Лайки автоматически обучают ваш локальный ИИ-алгоритм.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            items(liked, key = { "lk_${it.videoId}" }) { item ->
                HistoryVideoRow(item = item, onClick = { onOpenVideoById(item.videoId) })
            }
        }

        // Full Local Watch History
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Локальная история просмотров (${history.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                if (history.isNotEmpty()) {
                    OutlinedButton(onClick = onClearHistory) {
                        Text("Очистить", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        items(history, key = { "hist_${it.videoId}" }) { item ->
            HistoryVideoRow(item = item, onClick = { onOpenVideoById(item.videoId) })
        }
    }
}

@Composable
private fun HistoryVideoRow(
    item: WatchHistoryEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.author} • ${item.category} • Пропущено рекламы: ${item.adsSkippedCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Открыть",
                tint = CrimsonPulse
            )
        }
    }
}
