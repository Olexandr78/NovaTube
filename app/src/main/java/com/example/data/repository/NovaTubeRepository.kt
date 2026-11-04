package com.example.data.repository

import com.example.data.ai.AiTasteRuleUpdateResponse
import com.example.data.ai.GeminiPersonalizationService
import com.example.data.download.VideoDownloaderService
import com.example.data.local.DownloadedVideoEntity
import com.example.data.local.NovaTubeDao
import com.example.data.local.PreferenceTagEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.AiFeedCuratorResponse
import com.example.data.model.AiVideoSummaryResult
import com.example.data.model.VideoItem
import com.example.data.network.VideoNetworkEngine
import kotlinx.coroutines.flow.Flow

class NovaTubeRepository(
    private val dao: NovaTubeDao,
    private val networkEngine: VideoNetworkEngine,
    private val downloaderService: VideoDownloaderService,
    private val geminiService: GeminiPersonalizationService
) {
    val preferenceTagsFlow: Flow<List<PreferenceTagEntity>> = dao.getAllPreferenceTags()
    val watchHistoryFlow: Flow<List<WatchHistoryEntity>> = dao.getWatchHistory()
    val downloadedVideosFlow: Flow<List<DownloadedVideoEntity>> = dao.getDownloadedVideos()

    /**
     * Pre-seeds Sasha's local preference profile in Room DB on first launch so personalization
     * works immediately from real local database rows.
     */
    suspend fun ensureDefaultPreferencesSeeded() {
        if (dao.getPreferenceTagCount() == 0) {
            seedDefaultSashaTags()
        }
    }

    suspend fun resetToDefaultSashaPreferences() {
        dao.clearAllPreferenceTags()
        seedDefaultSashaTags()
    }

    suspend fun replacePreferencesFromSetup(
        favoriteTopics: List<Pair<String, String>>,
        blockedTopics: List<String>
    ) {
        dao.clearAllPreferenceTags()
        val entities = mutableListOf<PreferenceTagEntity>()
        favoriteTopics.forEachIndexed { index, (tag, category) ->
            val weight = (99 - index * 2).coerceIn(80, 99)
            entities.add(
                PreferenceTagEntity(
                    tag = tag.trim(),
                    category = category,
                    weight = weight,
                    isBlocked = false
                )
            )
        }
        blockedTopics.forEach { blocked ->
            if (blocked.isNotBlank()) {
                entities.add(
                    PreferenceTagEntity(
                        tag = blocked.trim(),
                        category = "Блок-фильтр",
                        weight = -100,
                        isBlocked = true
                    )
                )
            }
        }
        if (entities.isEmpty()) {
            seedDefaultSashaTags()
        } else {
            dao.insertPreferenceTags(entities)
        }
    }

    private suspend fun seedDefaultSashaTags() {
        val initialTags = listOf(
            PreferenceTagEntity(
                tag = "ГАЗ-24-10 Волга",
                category = "Автомеханика",
                weight = 99,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "ESP32 & Raspberry Pi",
                category = "Код и Железо",
                weight = 97,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "Need for Speed (Carbon & MW 2005)",
                category = "Гонки и Игры",
                weight = 96,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "3D-печать Ender-3 V3 SE",
                category = "Код и Железо",
                weight = 95,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "ЭЛТ и кассетные ВМ AKAI",
                category = "Ретро-техника",
                weight = 94,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "Python & Linux",
                category = "Код и Железо",
                weight = 95,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "Minecraft & Geometry Dash",
                category = "Гонки и Игры",
                weight = 91,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "Английский и Информатика",
                category = "Школа и ИТ",
                weight = 92,
                isBlocked = false
            ),
            PreferenceTagEntity(
                tag = "Roblox",
                category = "Блок-фильтр",
                weight = -100,
                isBlocked = true
            )
        )
        dao.insertPreferenceTags(initialTags)
    }

    fun getInitialCatalog(): List<VideoItem> = networkEngine.getCuratedCatalog()

    suspend fun loadLiveYouTubeHomeFeed(): List<VideoItem> {
        val prefs = dao.getPreferenceTagsSnapshot().filter { !it.isBlocked && it.weight > 0 }
        val queries = if (prefs.isNotEmpty()) {
            prefs.take(5).map { it.tag to it.category }
        } else {
            listOf(
                "ГАЗ-24-10 Волга ЗМЗ-402" to "Автомеханика",
                "ESP32 Raspberry Pi Python" to "Код и Железо",
                "Need for Speed Most Wanted 2005" to "Гонки и Игры",
                "Ender-3 V3 SE 3D печать" to "Код и Железо",
                "Кассетный видеомагнитофон AKAI ЭЛТ" to "Ретро-техника"
            )
        }
        return networkEngine.loadPersonalizedYouTubeHomeFeed(queries)
    }

    suspend fun searchVideos(query: String): List<VideoItem> = networkEngine.searchRealVideos(query)

    suspend fun hydrateVideoStream(video: VideoItem): VideoItem = networkEngine.hydrateVideoStreamIfNeeded(video)

    suspend fun resolveUrlForDownload(url: String): VideoItem = networkEngine.resolveCustomUrlToVideoItem(url)

    suspend fun runAiFeedCurator(videos: List<VideoItem>): AiFeedCuratorResponse {
        val prefs = dao.getPreferenceTagsSnapshot()
        val history = dao.getRecentWatchHistorySnapshot()
        return geminiService.curatePersonalizedFeed(prefs, history, videos)
    }

    suspend fun summarizeVideoWithAi(video: VideoItem): AiVideoSummaryResult {
        val prefs = dao.getPreferenceTagsSnapshot()
        return geminiService.summarizeVideoNoWater(video, prefs)
    }

    suspend fun applyNaturalLanguageRule(command: String): AiTasteRuleUpdateResponse {
        val result = geminiService.parseNaturalLanguageTasteCommand(command)
        result.addedPositiveTags.forEach { tag ->
            dao.insertPreferenceTag(
                PreferenceTagEntity(
                    tag = tag,
                    category = "ИИ-Правило",
                    weight = 92,
                    isBlocked = false
                )
            )
        }
        result.addedBlockedTags.forEach { tag ->
            dao.insertPreferenceTag(
                PreferenceTagEntity(
                    tag = tag,
                    category = "Блок-фильтр",
                    weight = -100,
                    isBlocked = true
                )
            )
        }
        return result
    }

    suspend fun addOrUpdateTag(tag: String, category: String, weight: Int, isBlocked: Boolean) {
        dao.insertPreferenceTag(
            PreferenceTagEntity(
                tag = tag.trim(),
                category = category,
                weight = if (isBlocked) -100 else weight.coerceIn(10, 100),
                isBlocked = isBlocked
            )
        )
    }

    suspend fun updateTagWeight(id: Int, weight: Int, isBlocked: Boolean) {
        dao.updateTagWeight(id, if (isBlocked) -100 else weight.coerceIn(5, 100), isBlocked)
    }

    suspend fun deleteTag(id: Int) {
        dao.deletePreferenceTag(id)
    }

    suspend fun recordVideoWatch(
        video: VideoItem,
        watchedSeconds: Int,
        liked: Boolean? = null,
        bookmarked: Boolean? = null
    ) {
        val existing = dao.getHistoryById(video.id)
        val entry = WatchHistoryEntity(
            videoId = video.id,
            title = video.title,
            author = video.author,
            category = video.category,
            tagsCsv = video.tags.joinToString(","),
            thumbnailUrl = video.thumbnailUrl,
            streamUrl = video.streamUrl,
            watchedSeconds = maxOf(watchedSeconds, existing?.watchedSeconds ?: 0),
            durationSeconds = video.durationSeconds,
            isLiked = liked ?: existing?.isLiked ?: false,
            isBookmarked = bookmarked ?: existing?.isBookmarked ?: false,
            adsSkippedCount = video.sponsorSegments.size.coerceAtLeast(1)
        )
        dao.upsertWatchHistory(entry)

        // Automatically boost matching local preference tag weights when user watches or likes a video
        if (liked == true) {
            val currentTags = dao.getPreferenceTagsSnapshot()
            for (vTag in video.tags) {
                val match = currentTags.firstOrNull { it.tag.equals(vTag, ignoreCase = true) }
                if (match != null && !match.isBlocked) {
                    dao.updateTagWeight(match.id, (match.weight + 4).coerceAtMost(100), false)
                }
            }
        }
    }

    suspend fun startVideoDownload(video: VideoItem, qualityLabel: String, streamUrl: String) {
        downloaderService.downloadVideoFile(video, qualityLabel, streamUrl)
    }

    suspend fun deleteDownloadedVideo(videoId: String) {
        downloaderService.deleteDownloadedVideo(videoId)
    }

    suspend fun clearHistory() {
        dao.clearWatchHistory()
    }
}
