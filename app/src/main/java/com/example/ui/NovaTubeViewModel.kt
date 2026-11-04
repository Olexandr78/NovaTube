package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiPersonalizationService
import com.example.data.download.VideoDownloaderService
import com.example.data.local.AppSettingsManager
import com.example.data.local.DownloadedVideoEntity
import com.example.data.local.NovaTubeAppSettings
import com.example.data.local.NovaTubeDatabase
import com.example.data.local.PreferenceTagEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.AiVideoSummaryResult
import com.example.data.model.VideoItem
import com.example.data.network.VideoNetworkEngine
import com.example.data.repository.NovaTubeRepository
import com.example.ui.components.SetupPresetTopic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    FEED,
    DOWNLOADER,
    AI_BRAIN,
    LIBRARY,
    SETTINGS
}

data class QuickDownloadTarget(
    val video: VideoItem
)

class NovaTubeViewModel(
    private val repository: NovaTubeRepository,
    private val settingsManager: AppSettingsManager
) : ViewModel() {

    val appSettings: StateFlow<NovaTubeAppSettings> = settingsManager.settings

    private val _currentTab = MutableStateFlow(MainTab.FEED)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Все (ИИ-Поток)")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawVideos = MutableStateFlow(repository.getInitialCatalog())
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isAiCurating = MutableStateFlow(false)
    val isAiCurating: StateFlow<Boolean> = _isAiCurating.asStateFlow()

    private val _aiGreeting = MutableStateFlow(
        "Привет, ${settingsManager.settings.value.userNickname}! Лента настроена по твоим локальным вкусам"
    )
    val aiGreeting: StateFlow<String> = _aiGreeting.asStateFlow()

    private val _aiFeedInsight = MutableStateFlow(
        "Приоритет: ГАЗ-24-10 «Волга», ESP32/Raspberry Pi, NFS (MW & Carbon), Ender-3 V3 SE, ЭЛТ и ВМ AKAI. Анти-Roblox фильтр активен."
    )
    val aiFeedInsight: StateFlow<String> = _aiFeedInsight.asStateFlow()

    private val _suggestedQueries = MutableStateFlow(
        listOf(
            "ГАЗ-24-10 Волга ЗМЗ-402",
            "ESP32 Raspberry Pi Python",
            "Need for Speed Most Wanted 2005",
            "3D-печать Ender-3 V3 SE",
            "Кассетный ВМ AKAI & ЭЛТ"
        )
    )
    val suggestedQueries: StateFlow<List<String>> = _suggestedQueries.asStateFlow()

    private val _activeVideo = MutableStateFlow<VideoItem?>(null)
    val activeVideo: StateFlow<VideoItem?> = _activeVideo.asStateFlow()

    private val _activeVideoAiSummary = MutableStateFlow<AiVideoSummaryResult?>(null)
    val activeVideoAiSummary: StateFlow<AiVideoSummaryResult?> = _activeVideoAiSummary.asStateFlow()

    private val _isSummarizingVideo = MutableStateFlow(false)
    val isSummarizingVideo: StateFlow<Boolean> = _isSummarizingVideo.asStateFlow()

    private val _quickDownloadModalVideo = MutableStateFlow<VideoItem?>(null)
    val quickDownloadModalVideo: StateFlow<VideoItem?> = _quickDownloadModalVideo.asStateFlow()

    private val _showUrlDownloaderDialog = MutableStateFlow(false)
    val showUrlDownloaderDialog: StateFlow<Boolean> = _showUrlDownloaderDialog.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _totalAdsBlockedCount = MutableStateFlow(42)
    val totalAdsBlockedCount: StateFlow<Int> = _totalAdsBlockedCount.asStateFlow()

    val preferenceTags: StateFlow<List<PreferenceTagEntity>> = repository.preferenceTagsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedVideos: StateFlow<List<DownloadedVideoEntity>> = repository.downloadedVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically computes the personalized & filtered feed from local Room DB preferences + settings
    val personalizedFeed: StateFlow<List<VideoItem>> = combine(
        _rawVideos,
        preferenceTags,
        _selectedCategory,
        appSettings
    ) { videos, prefs, category, settings ->
        val blockedKeywords = if (settings.strictBlockedTopicsFilter) {
            prefs.filter { it.isBlocked || it.weight < 0 }
                .map { it.tag.lowercase().trim() }
                .filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        val positivePrefs = prefs.filter { !it.isBlocked && it.weight > 0 }
        val sensitivityBoost = (settings.aiSensitivityPercent - 75).coerceAtLeast(0) / 5

        videos
            .filter { video ->
                val hitsBlocked = blockedKeywords.any { blocked ->
                    video.title.lowercase().contains(blocked) ||
                        video.tags.any { t -> t.lowercase().contains(blocked) }
                }
                !hitsBlocked
            }
            .map { video ->
                var bestScore = video.aiMatchScore
                var bestReason = video.aiReason
                for (pref in positivePrefs) {
                    val keyword = pref.tag.substringBefore(" ").lowercase()
                    val matchesTag = video.tags.any {
                        it.contains(pref.tag, ignoreCase = true) || pref.tag.contains(it, ignoreCase = true)
                    } || (keyword.length >= 3 && video.title.lowercase().contains(keyword))

                    if (matchesTag) {
                        val computedScore = (pref.weight + sensitivityBoost).coerceIn(75, 99)
                        if (computedScore >= bestScore) {
                            bestScore = computedScore
                            bestReason = "$computedScore% совпадение • Локальный интерес: «${pref.tag}»"
                        }
                    }
                }
                video.copy(aiMatchScore = bestScore, aiReason = bestReason)
            }
            .filter { video ->
                when (category) {
                    "Все (ИИ-Поток)" -> true
                    else -> video.category.equals(category, ignoreCase = true) ||
                        video.tags.any { it.contains(category, ignoreCase = true) }
                }
            }
            .let { filtered ->
                if (category != "Все (ИИ-Поток)") {
                    filtered.sortedByDescending { it.aiMatchScore }
                } else {
                    // Round-robin interleave across categories so the feed is always diverse!
                    val byCat = filtered
                        .groupBy { it.category }
                        .values
                        .map { group -> group.sortedByDescending { it.aiMatchScore } }
                        .sortedByDescending { group -> group.firstOrNull()?.aiMatchScore ?: 0 }

                    val interleaved = mutableListOf<VideoItem>()
                    val maxLen = byCat.maxOfOrNull { it.size } ?: 0
                    for (i in 0 until maxLen) {
                        for (group in byCat) {
                            if (i < group.size) {
                                interleaved.add(group[i])
                            }
                        }
                    }
                    interleaved
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureDefaultPreferencesSeeded()
            _isSearching.value = true
            val liveYouTubeFeed = repository.loadLiveYouTubeHomeFeed()
            if (liveYouTubeFeed.isNotEmpty()) {
                _rawVideos.value = liveYouTubeFeed
            }
            _isSearching.value = false
        }
    }

    fun updateAppSettings(transform: (NovaTubeAppSettings) -> NovaTubeAppSettings) {
        settingsManager.updateSettings(transform)
        val newNick = settingsManager.settings.value.userNickname.ifBlank { "Саша" }
        _aiGreeting.value = "Привет, $newNick! Лента настроена по твоим локальным вкусам"
    }

    fun completeFirstLaunchSetup(
        updatedSettings: NovaTubeAppSettings,
        selectedFavoriteTopics: List<SetupPresetTopic>,
        selectedBlockedTopics: List<String>
    ) {
        settingsManager.updateSettings { updatedSettings.copy(isOnboardingCompleted = true) }
        val nick = updatedSettings.userNickname.ifBlank { "Саша" }
        _aiGreeting.value = "Привет, $nick! Твоя персональная лента готова"
        val topTopicsSummary = selectedFavoriteTopics.take(5).joinToString(", ") { it.tag }
        val blockedSummary = if (selectedBlockedTopics.isNotEmpty()) {
            " • В блок-фильтре: ${selectedBlockedTopics.joinToString(", ")}"
        } else ""
        _aiFeedInsight.value = "Приоритет: $topTopicsSummary$blockedSummary"

        if (selectedFavoriteTopics.isNotEmpty()) {
            _suggestedQueries.value = selectedFavoriteTopics.take(5).map { it.tag }
        }

        viewModelScope.launch {
            repository.replacePreferencesFromSetup(
                favoriteTopics = selectedFavoriteTopics.map { it.tag to it.category },
                blockedTopics = selectedBlockedTopics
            )
            _isSearching.value = true
            val liveYouTubeFeed = repository.loadLiveYouTubeHomeFeed()
            if (liveYouTubeFeed.isNotEmpty()) {
                _rawVideos.value = liveYouTubeFeed
            }
            _isSearching.value = false
            _statusBannerMessage.value = "Персонализация сохранена! Загружена свежая лента YouTube"
        }
    }

    fun reopenSetupWizard() {
        settingsManager.updateSettings { it.copy(isOnboardingCompleted = false) }
    }

    fun resetDefaultSashaTastes() {
        viewModelScope.launch {
            repository.resetToDefaultSashaPreferences()
            _statusBannerMessage.value = "Восстановлены фирменные вкусы Саши (ГАЗ-24-10, ESP32, NFS, ретро)"
            val liveYouTubeFeed = repository.loadLiveYouTubeHomeFeed()
            if (liveYouTubeFeed.isNotEmpty()) {
                _rawVideos.value = liveYouTubeFeed
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(query: String = _searchQuery.value) {
        _searchQuery.value = query
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchVideos(query)
            _rawVideos.value = results
            _selectedCategory.value = "Все (ИИ-Поток)"
            _isSearching.value = false
            _statusBannerMessage.value = if (query.isBlank()) {
                "Лента сброшена к основному каталогу"
            } else {
                "Найдено видео без рекламы по запросу «$query»: ${results.size}"
            }
        }
    }

    fun resetToHomeFeed() {
        _searchQuery.value = ""
        _selectedCategory.value = "Все (ИИ-Поток)"
        _rawVideos.value = repository.getInitialCatalog()
    }

    fun triggerGeminiFeedCuration() {
        if (_isAiCurating.value) return
        viewModelScope.launch {
            _isAiCurating.value = true
            _statusBannerMessage.value = "ИИ-подбиратель ищет новые разнообразные видео по всем вашим рубрикам..."

            // Fetch a fresh rotated & multi-source batch so the user gets new diverse videos every time
            val freshBatch = repository.loadLiveYouTubeHomeFeed()
            val baseList = if (freshBatch.isNotEmpty()) freshBatch else repository.getInitialCatalog()

            val response = repository.runAiFeedCurator(baseList)
            _aiGreeting.value = response.personalGreeting
            _aiFeedInsight.value = response.feedInsightSummary
            if (response.recommendedSearchQueries.isNotEmpty()) {
                _suggestedQueries.value = response.recommendedSearchQueries
            }

            val rankingMap = response.videoRankings.associateBy { it.videoId }
            val ranked = baseList.map { video ->
                val rank = rankingMap[video.id]
                if (rank != null) {
                    video.copy(
                        aiMatchScore = rank.matchScore.coerceIn(1, 99),
                        aiReason = "${rank.matchScore}% совпадение • ${rank.personalizedReason}"
                    )
                } else {
                    video
                }
            }

            // Interleave categories so the feed never shows 3 videos of the same topic in a row
            val grouped = ranked.groupBy { it.category }.values.map { it.shuffled() }
            val diverseList = mutableListOf<VideoItem>()
            val maxGroup = grouped.maxOfOrNull { it.size } ?: 0
            for (i in 0 until maxGroup) {
                for (g in grouped) {
                    if (i < g.size) {
                        val item = g[i]
                        val displayScore = (99 - diverseList.size).coerceIn(80, 99)
                        diverseList.add(item.copy(aiMatchScore = displayScore))
                    }
                }
            }

            _rawVideos.value = diverseList.ifEmpty { ranked }
            _isAiCurating.value = false
            _statusBannerMessage.value = "Лента обновлена! Подобраны разнообразные видео из разных рубрик"
        }
    }

    fun openVideoPlayer(video: VideoItem) {
        _activeVideo.value = video
        _activeVideoAiSummary.value = null
        if (appSettings.value.adBlockEnabled || appSettings.value.sponsorBlockEnabled) {
            _totalAdsBlockedCount.value += video.sponsorSegments.size.coerceAtLeast(1)
        }
        viewModelScope.launch {
            repository.recordVideoWatch(video, watchedSeconds = 15)
        }
    }

    fun closeVideoPlayer() {
        _activeVideo.value = null
        _activeVideoAiSummary.value = null
    }

    fun requestVideoAiSummary(video: VideoItem) {
        if (_isSummarizingVideo.value) return
        viewModelScope.launch {
            _isSummarizingVideo.value = true
            val summary = repository.summarizeVideoWithAi(video)
            _activeVideoAiSummary.value = summary
            _isSummarizingVideo.value = false
        }
    }

    fun toggleLikeVideo(video: VideoItem) {
        viewModelScope.launch {
            val current = watchHistory.value.firstOrNull { it.videoId == video.id }
            val newLiked = !(current?.isLiked ?: false)
            repository.recordVideoWatch(
                video = video,
                watchedSeconds = 30,
                liked = if (appSettings.value.autoLearnFromLikes) newLiked else false
            )
            _statusBannerMessage.value = if (newLiked) {
                "Лайк сохранён локально! Вес тегов «${video.tags.firstOrNull() ?: video.category}» повышен"
            } else {
                "Отметка «Нравится» снята"
            }
        }
    }

    fun toggleBookmarkVideo(video: VideoItem) {
        viewModelScope.launch {
            val current = watchHistory.value.firstOrNull { it.videoId == video.id }
            val newBookmark = !(current?.isBookmarked ?: false)
            repository.recordVideoWatch(video, watchedSeconds = 10, bookmarked = newBookmark)
            _statusBannerMessage.value = if (newBookmark) {
                "Добавлено в «Смотреть позже» (локальная БД)"
            } else {
                "Удалено из закладок"
            }
        }
    }

    // --- Quick Menu Downloader Actions ---
    fun openQuickDownloadSheet(video: VideoItem) {
        _quickDownloadModalVideo.value = video
    }

    fun dismissQuickDownloadSheet() {
        _quickDownloadModalVideo.value = null
    }

    fun openUrlDownloaderDialog() {
        _showUrlDownloaderDialog.value = true
    }

    fun dismissUrlDownloaderDialog() {
        _showUrlDownloaderDialog.value = false
    }

    fun startDownload(video: VideoItem, qualityLabel: String, streamUrl: String) {
        _quickDownloadModalVideo.value = null
        _statusBannerMessage.value = "Загрузка «${video.title.take(28)}...» ($qualityLabel) запущена!"
        viewModelScope.launch {
            repository.startVideoDownload(video, qualityLabel, streamUrl)
        }
    }

    fun startCustomUrlDownload(rawUrl: String, qualityLabel: String) {
        if (rawUrl.isBlank()) return
        _showUrlDownloaderDialog.value = false
        viewModelScope.launch {
            _statusBannerMessage.value = "Извлечение прямого видеопотока из ссылки..."
            val resolvedVideo = repository.resolveUrlForDownload(rawUrl)
            _rawVideos.value = listOf(resolvedVideo) + _rawVideos.value.filterNot { it.id == resolvedVideo.id }
            val targetUrl = resolvedVideo.qualityOptions.firstOrNull { it.shortLabel == qualityLabel }?.streamUrl
                ?: resolvedVideo.streamUrl
            _statusBannerMessage.value = "Скачивание «${resolvedVideo.title.take(26)}» начато!"
            repository.startVideoDownload(resolvedVideo, qualityLabel, targetUrl)
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            repository.deleteDownloadedVideo(videoId)
            _statusBannerMessage.value = "Файл удалён из памяти устройства"
        }
    }

    // --- Local Preference Tags Management ---
    fun addPreferenceTag(tag: String, category: String, weight: Int, isBlocked: Boolean) {
        if (tag.isBlank()) return
        viewModelScope.launch {
            repository.addOrUpdateTag(tag, category, weight, isBlocked)
            _statusBannerMessage.value = if (isBlocked) {
                "Тема «$tag» добавлена в локальный блок-фильтр"
            } else {
                "Интерес «$tag» ($weight%) добавлен в локальный ИИ-профиль"
            }
        }
    }

    fun updateTagWeight(id: Int, weight: Int, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.updateTagWeight(id, weight, isBlocked)
        }
    }

    fun deleteTag(id: Int) {
        viewModelScope.launch {
            repository.deleteTag(id)
        }
    }

    fun applyNaturalLanguageAiCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            _isAiCurating.value = true
            val res = repository.applyNaturalLanguageRule(command)
            _aiFeedInsight.value = res.explanation.ifBlank { "Правило «$command» применено к локальной базе." }
            _isAiCurating.value = false
            _statusBannerMessage.value = "ИИ обновил ваши локальные предпочтения!"
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _statusBannerMessage.value = "Локальная история просмотров очищена"
        }
    }

    fun dismissStatusBanner() {
        _statusBannerMessage.value = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            val db = NovaTubeDatabase.getInstance(appCtx)
            val networkEngine = VideoNetworkEngine()
            val downloader = VideoDownloaderService(appCtx, db.dao())
            val gemini = GeminiPersonalizationService()
            val repo = NovaTubeRepository(db.dao(), networkEngine, downloader, gemini)
            val settingsManager = AppSettingsManager(appCtx)
            return NovaTubeViewModel(repo, settingsManager) as T
        }
    }
}
