package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.ui.MainTab
import com.example.ui.NovaTubeViewModel
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutGoogle
import com.example.ui.components.AiCuratorBannerCard
import com.example.ui.components.AiPersonalizationBrainScreen
import com.example.ui.components.DownloaderHubScreen
import com.example.ui.components.FirstLaunchSetupScreen
import com.example.ui.components.LocalLibraryScreen
import com.example.ui.components.QuickVideoDownloadModal
import com.example.ui.components.RealVideoPlayerScreen
import com.example.ui.components.SettingsScreen
import com.example.ui.components.UrlExtractorDownloaderDialog
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.AmberVu
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldShield
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ApkDirectDownloadDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppAuthGateRoot()
            }
        }
    }
}

@Composable
fun AppAuthGateRoot(
    auth: FirebaseAuth = Firebase.auth
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val viewModel: NovaTubeViewModel = viewModel(
        factory = NovaTubeViewModel.Factory(context)
    )
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    var showApkDialog by remember { mutableStateOf(false) }

    if (showApkDialog) {
        ApkDirectDownloadDialog(onDismiss = { showApkDialog = false })
    }

    if (!appSettings.isOnboardingCompleted) {
        FirstLaunchSetupScreen(
            currentUser = currentUser,
            initialSettings = appSettings,
            onOpenApkDownloadDialog = { showApkDialog = true },
            onCompleteSetup = { updatedSettings, favoriteTopics, blockedTopics ->
                viewModel.completeFirstLaunchSetup(
                    updatedSettings = updatedSettings,
                    selectedFavoriteTopics = favoriteTopics,
                    selectedBlockedTopics = blockedTopics
                )
            }
        )
    } else {
        NovaTubeApp(
            currentUser = currentUser,
            onSignOut = {
                signOutGoogle(
                    context = context,
                    onSignOutComplete = {},
                    scope = scope
                )
            },
            onOpenApkDownloadDialog = { showApkDialog = true },
            viewModel = viewModel
        )
    }
}

@Composable
fun NovaTubeApp(
    currentUser: FirebaseUser?,
    onSignOut: () -> Unit,
    onOpenApkDownloadDialog: () -> Unit = {},
    viewModel: NovaTubeViewModel
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val isAiCurating by viewModel.isAiCurating.collectAsStateWithLifecycle()
    val aiGreeting by viewModel.aiGreeting.collectAsStateWithLifecycle()
    val aiFeedInsight by viewModel.aiFeedInsight.collectAsStateWithLifecycle()
    val suggestedQueries by viewModel.suggestedQueries.collectAsStateWithLifecycle()
    val personalizedFeed by viewModel.personalizedFeed.collectAsStateWithLifecycle()
    val preferenceTags by viewModel.preferenceTags.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val downloadedVideos by viewModel.downloadedVideos.collectAsStateWithLifecycle()
    val activeVideo by viewModel.activeVideo.collectAsStateWithLifecycle()
    val activeVideoAiSummary by viewModel.activeVideoAiSummary.collectAsStateWithLifecycle()
    val isSummarizingVideo by viewModel.isSummarizingVideo.collectAsStateWithLifecycle()
    val quickDownloadModalVideo by viewModel.quickDownloadModalVideo.collectAsStateWithLifecycle()
    val showUrlDownloaderDialog by viewModel.showUrlDownloaderDialog.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val totalAdsBlockedCount by viewModel.totalAdsBlockedCount.collectAsStateWithLifecycle()

    if (activeVideo == null && currentTab != MainTab.FEED) {
        BackHandler {
            viewModel.selectTab(MainTab.FEED)
        }
    }

    val categories = listOf(
        "Все (ИИ-Поток)",
        "Автомеханика",
        "Код и Железо",
        "Гонки и Игры",
        "Ретро-техника",
        "Школа и ИТ"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (activeVideo == null) {
                NovaTubeTopBar(
                    currentUser = currentUser,
                    userNickname = appSettings.userNickname,
                    searchQuery = searchQuery,
                    downloadsCount = downloadedVideos.size,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onExecuteSearch = { viewModel.performSearch(searchQuery) },
                    onClearSearch = viewModel::resetToHomeFeed,
                    onOpenQuickUrlDownloader = viewModel::openUrlDownloaderDialog,
                    onNavigateToDownloaderTab = { viewModel.selectTab(MainTab.DOWNLOADER) },
                    onNavigateToSettingsTab = { viewModel.selectTab(MainTab.SETTINGS) },
                    onOpenApkDownloadDialog = onOpenApkDownloadDialog,
                    onSignOut = onSignOut
                )
            }
        },
        bottomBar = {
            if (activeVideo == null) {
                NavigationBar(
                    containerColor = ObsidianSurface,
                    contentColor = TextPrimary
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.FEED,
                        onClick = { viewModel.selectTab(MainTab.FEED) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Лента ИИ") },
                        label = { Text("Лента") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPulse,
                            selectedTextColor = CrimsonPulse,
                            indicatorColor = CrimsonPulse.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_tab_feed")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.DOWNLOADER,
                        onClick = { viewModel.selectTab(MainTab.DOWNLOADER) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (downloadedVideos.isNotEmpty()) {
                                        Badge(containerColor = AmberVu, contentColor = ObsidianBg) {
                                            Text("${downloadedVideos.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Скачиватель")
                            }
                        },
                        label = { Text("Скачать") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberVu,
                            selectedTextColor = AmberVu,
                            indicatorColor = AmberVu.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_tab_downloader")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.AI_BRAIN,
                        onClick = { viewModel.selectTab(MainTab.AI_BRAIN) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "ИИ-Вкусы") },
                        label = { Text("ИИ-Вкусы") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_tab_ai_brain")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.LIBRARY,
                        onClick = { viewModel.selectTab(MainTab.LIBRARY) },
                        icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Библиотека") },
                        label = { Text("Архив") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldShield,
                            selectedTextColor = EmeraldShield,
                            indicatorColor = EmeraldShield.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_tab_library")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.SETTINGS,
                        onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                        label = { Text("Настройки") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeVideo != null) {
                val vid = activeVideo!!
                val dlEntry = downloadedVideos.firstOrNull { it.videoId == vid.id }
                val histEntry = watchHistory.firstOrNull { it.videoId == vid.id }

                RealVideoPlayerScreen(
                    video = vid,
                    downloadedEntry = dlEntry,
                    historyEntry = histEntry,
                    aiSummary = activeVideoAiSummary,
                    isSummarizing = isSummarizingVideo,
                    relatedVideos = personalizedFeed.filterNot { it.id == vid.id },
                    onClose = viewModel::closeVideoPlayer,
                    onDownloadClick = viewModel::openQuickDownloadSheet,
                    onLikeClick = viewModel::toggleLikeVideo,
                    onBookmarkClick = viewModel::toggleBookmarkVideo,
                    onRequestAiSummary = viewModel::requestVideoAiSummary,
                    onSelectRelatedVideo = viewModel::openVideoPlayer
                )
            } else {
                when (currentTab) {
                    MainTab.FEED -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("feed_lazy_column"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    categories.forEach { cat ->
                                        val isSelected = selectedCategory == cat
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectCategory(cat) },
                                            label = {
                                                Text(
                                                    text = cat,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CrimsonPulse,
                                                selectedLabelColor = Color.White,
                                                containerColor = ObsidianSurface,
                                                labelColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            item {
                                AiCuratorBannerCard(
                                    greeting = aiGreeting,
                                    insight = aiFeedInsight,
                                    suggestedQueries = suggestedQueries,
                                    isAiCurating = isAiCurating,
                                    onTriggerGeminiCuration = viewModel::triggerGeminiFeedCuration,
                                    onQuickQueryClick = { q ->
                                        viewModel.performSearch(q)
                                    },
                                    onOpenUrlDownloader = viewModel::openUrlDownloaderDialog
                                )
                            }

                            if (isSearching) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = CyberCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Загрузка живых видео из базы YouTube...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = CyberCyan
                                        )
                                    }
                                }
                            }

                            items(personalizedFeed, key = { it.id }) { video ->
                                val dl = downloadedVideos.firstOrNull { it.videoId == video.id }
                                VideoFeedCard(
                                    video = video,
                                    downloadEntry = dl,
                                    onPlayClick = { viewModel.openVideoPlayer(video) },
                                    onQuickDownloadClick = { viewModel.openQuickDownloadSheet(video) },
                                    onBoostTopicClick = {
                                        val mainTag = video.tags.firstOrNull() ?: video.category
                                        viewModel.addPreferenceTag(mainTag, video.category, 98, false)
                                    },
                                    onBlockTopicClick = {
                                        val mainTag = video.tags.firstOrNull() ?: video.category
                                        viewModel.addPreferenceTag(mainTag, "Блок-фильтр", -100, true)
                                    },
                                    onBookmarkClick = {
                                        viewModel.toggleBookmarkVideo(video)
                                    }
                                )
                            }
                        }
                    }

                    MainTab.DOWNLOADER -> {
                        DownloaderHubScreen(
                            downloads = downloadedVideos,
                            feedVideos = personalizedFeed,
                            onStartUrlDownload = viewModel::startCustomUrlDownload,
                            onQuickDownloadVideo = viewModel::openQuickDownloadSheet,
                            onPlayDownloadedVideo = { dl ->
                                val matching = personalizedFeed.firstOrNull { it.id == dl.videoId }
                                    ?: VideoItem(
                                        id = dl.videoId,
                                        title = dl.title,
                                        author = dl.author,
                                        viewsText = "Офлайн файл",
                                        publishedText = dl.quality,
                                        durationSeconds = 60,
                                        thumbnailUrl = dl.thumbnailUrl,
                                        streamUrl = dl.remoteStreamUrl,
                                        category = "Скачанное",
                                        tags = listOf("Офлайн", dl.quality),
                                        description = "Воспроизведение локального файла: ${dl.localFilePath}"
                                    )
                                viewModel.openVideoPlayer(matching)
                            },
                            onDeleteDownload = viewModel::deleteDownload
                        )
                    }

                    MainTab.AI_BRAIN -> {
                        AiPersonalizationBrainScreen(
                            preferenceTags = preferenceTags,
                            aiInsight = aiFeedInsight,
                            isAiCurating = isAiCurating,
                            onTriggerGeminiReRank = viewModel::triggerGeminiFeedCuration,
                            onApplyNaturalLanguageCommand = viewModel::applyNaturalLanguageAiCommand,
                            onAddTag = viewModel::addPreferenceTag,
                            onUpdateTagWeight = viewModel::updateTagWeight,
                            onDeleteTag = viewModel::deleteTag
                        )
                    }

                    MainTab.LIBRARY -> {
                        LocalLibraryScreen(
                            history = watchHistory,
                            adsBlockedCount = totalAdsBlockedCount,
                            onOpenVideoById = { id ->
                                val found = personalizedFeed.firstOrNull { it.id == id }
                                val hist = watchHistory.firstOrNull { it.videoId == id }
                                if (found != null) {
                                    viewModel.openVideoPlayer(found)
                                } else if (hist != null) {
                                    viewModel.openVideoPlayer(
                                        VideoItem(
                                            id = hist.videoId,
                                            title = hist.title,
                                            author = hist.author,
                                            viewsText = "Из истории",
                                            publishedText = "Локальная БД",
                                            durationSeconds = hist.durationSeconds,
                                            thumbnailUrl = hist.thumbnailUrl,
                                            streamUrl = hist.streamUrl,
                                            category = hist.category,
                                            tags = hist.tagsCsv.split(",").filter { it.isNotBlank() },
                                            description = "Открыто из вашей локальной истории просмотров."
                                        )
                                    )
                                }
                            },
                            onClearHistory = viewModel::clearWatchHistory
                        )
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            currentUser = currentUser,
                            settings = appSettings,
                            preferenceTags = preferenceTags,
                            downloadsCount = downloadedVideos.size,
                            historyCount = watchHistory.size,
                            adsBlockedCount = totalAdsBlockedCount,
                            onUpdateSettings = viewModel::updateAppSettings,
                            onOpenAiBrainTab = { viewModel.selectTab(MainTab.AI_BRAIN) },
                            onReopenSetupWizard = viewModel::reopenSetupWizard,
                            onResetDefaultTastes = viewModel::resetDefaultSashaTastes,
                            onClearHistory = viewModel::clearWatchHistory,
                            onOpenApkExportDialog = onOpenApkDownloadDialog,
                            onSignOut = onSignOut
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = statusBannerMessage != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.clickable { viewModel.dismissStatusBanner() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = statusBannerMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    quickDownloadModalVideo?.let { targetVideo ->
        QuickVideoDownloadModal(
            video = targetVideo,
            onDismiss = viewModel::dismissQuickDownloadSheet,
            onSelectQualityAndDownload = { qualityLabel, streamUrl ->
                viewModel.startDownload(targetVideo, qualityLabel, streamUrl)
            }
        )
    }

    if (showUrlDownloaderDialog) {
        UrlExtractorDownloaderDialog(
            onDismiss = viewModel::dismissUrlDownloaderDialog,
            onStartUrlDownload = { url, quality ->
                viewModel.startCustomUrlDownload(url, quality)
            }
        )
    }
}

@Composable
private fun NovaTubeTopBar(
    currentUser: FirebaseUser?,
    userNickname: String,
    searchQuery: String,
    downloadsCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onOpenQuickUrlDownloader: () -> Unit,
    onNavigateToDownloaderTab: () -> Unit,
    onNavigateToSettingsTab: () -> Unit,
    onOpenApkDownloadDialog: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    tint = CrimsonPulse,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NovaTube • $userNickname",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentUser?.email ?: currentUser?.displayName ?: "Локальный ИИ • Войти в Google в Настройках",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentUser != null) EmeraldShield else CyberCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = EmeraldShield,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clickable(onClick = onOpenApkDownloadDialog)
                        .testTag("top_bar_apk_button")
                ) {
                    Text(
                        text = "APK",
                        style = MaterialTheme.typography.labelLarge,
                        color = ObsidianBg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    color = AmberVu,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clickable(onClick = onOpenQuickUrlDownloader)
                        .testTag("top_bar_quick_download_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Быстрый скачиватель",
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

                if (downloadsCount > 0) {
                    Surface(
                        color = EmeraldShield.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable(onClick = onNavigateToDownloaderTab)
                    ) {
                        Text(
                            text = "$downloadsCount",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldShield,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }

                // Quick Profile / Settings / Sign-Out pill
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(onClick = onNavigateToSettingsTab)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        if (currentUser?.photoUrl != null) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Аватар Google",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Аккаунт и Настройки",
                                tint = if (currentUser != null) EmeraldShield else CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (currentUser != null) {
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Выйти из аккаунта",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Живой поиск по базе YouTube (ГАЗ-24-10, ESP32, NFS)...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск в YouTube",
                    tint = CyberCyan
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Сбросить",
                                tint = TextSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onExecuteSearch,
                        modifier = Modifier.testTag("execute_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Найти в YouTube",
                            tint = CrimsonPulse
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onExecuteSearch() }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ObsidianCard,
                unfocusedContainerColor = ObsidianCard,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = ObsidianElevated
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_field")
        )
    }
}
