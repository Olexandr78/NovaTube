package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SponsorSegment(
    val startSec: Int,
    val endSec: Int,
    val label: String
)

@Serializable
data class VideoQualityOption(
    val label: String,
    val shortLabel: String,
    val estimatedSizeMb: Double,
    val streamUrl: String
)

@Serializable
data class VideoItem(
    val id: String,
    val youtubeId: String = "",
    val title: String,
    val author: String,
    val authorAvatarUrl: String = "",
    val subscriberCount: String = "YouTube Канал",
    val viewsText: String,
    val publishedText: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val streamUrl: String,
    val category: String,
    val tags: List<String>,
    val description: String,
    val sponsorSegments: List<SponsorSegment> = emptyList(),
    val qualityOptions: List<VideoQualityOption> = emptyList(),
    val aiMatchScore: Int = 90,
    val aiReason: String = "Подобрано из базы YouTube по вашим локальным вкусам",
    val sourcePlatform: String = "YouTube Database • AdFree"
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
}

@Serializable
data class AiFeedCuratorResponse(
    val personalGreeting: String,
    val feedInsightSummary: String,
    val recommendedSearchQueries: List<String>,
    val videoRankings: List<AiVideoRankingItem>
)

@Serializable
data class AiVideoRankingItem(
    val videoId: String,
    val matchScore: Int,
    val personalizedReason: String
)

@Serializable
data class AiVideoSummaryResult(
    val oneLineEssence: String,
    val keyTakeaways: List<String>,
    val cleanChapters: List<String>,
    val whyItFitsUser: String
)
