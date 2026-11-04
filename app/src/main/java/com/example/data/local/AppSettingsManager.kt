package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NovaTubeAppSettings(
    val isOnboardingCompleted: Boolean = false,
    val userNickname: String = "Саша",
    val adBlockEnabled: Boolean = true,
    val sponsorBlockEnabled: Boolean = true,
    val skipSelfPromoEnabled: Boolean = true,
    val showSkipToastEnabled: Boolean = true,
    val autoLearnFromLikes: Boolean = true,
    val strictBlockedTopicsFilter: Boolean = true,
    val aiSensitivityPercent: Int = 92,
    val defaultDownloadQuality: String = "1080p MP4",
    val defaultPlaybackSpeed: Float = 1.0f,
    val preferNativeMp4Player: Boolean = false,
    val crtRetroAccentEnabled: Boolean = true
)

class AppSettingsManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("novatube_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadFromPrefs())
    val settings: StateFlow<NovaTubeAppSettings> = _settings.asStateFlow()

    private fun loadFromPrefs(): NovaTubeAppSettings {
        return NovaTubeAppSettings(
            isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false),
            userNickname = prefs.getString("user_nickname", "Саша") ?: "Саша",
            adBlockEnabled = prefs.getBoolean("ad_block_enabled", true),
            sponsorBlockEnabled = prefs.getBoolean("sponsor_block_enabled", true),
            skipSelfPromoEnabled = prefs.getBoolean("skip_self_promo", true),
            showSkipToastEnabled = prefs.getBoolean("show_skip_toast", true),
            autoLearnFromLikes = prefs.getBoolean("auto_learn_likes", true),
            strictBlockedTopicsFilter = prefs.getBoolean("strict_block_filter", true),
            aiSensitivityPercent = prefs.getInt("ai_sensitivity_pct", 92),
            defaultDownloadQuality = prefs.getString("default_dl_quality", "1080p MP4") ?: "1080p MP4",
            defaultPlaybackSpeed = prefs.getFloat("default_playback_speed", 1.0f),
            preferNativeMp4Player = prefs.getBoolean("prefer_native_mp4", false),
            crtRetroAccentEnabled = prefs.getBoolean("crt_retro_accent", true)
        )
    }

    fun updateSettings(transform: (NovaTubeAppSettings) -> NovaTubeAppSettings) {
        val updated = transform(_settings.value)
        prefs.edit()
            .putBoolean("onboarding_completed", updated.isOnboardingCompleted)
            .putString("user_nickname", updated.userNickname)
            .putBoolean("ad_block_enabled", updated.adBlockEnabled)
            .putBoolean("sponsor_block_enabled", updated.sponsorBlockEnabled)
            .putBoolean("skip_self_promo", updated.skipSelfPromoEnabled)
            .putBoolean("show_skip_toast", updated.showSkipToastEnabled)
            .putBoolean("auto_learn_likes", updated.autoLearnFromLikes)
            .putBoolean("strict_block_filter", updated.strictBlockedTopicsFilter)
            .putInt("ai_sensitivity_pct", updated.aiSensitivityPercent)
            .putString("default_dl_quality", updated.defaultDownloadQuality)
            .putFloat("default_playback_speed", updated.defaultPlaybackSpeed)
            .putBoolean("prefer_native_mp4", updated.preferNativeMp4Player)
            .putBoolean("crt_retro_accent", updated.crtRetroAccentEnabled)
            .apply()
        _settings.value = updated
    }
}
