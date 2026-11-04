package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.data.local.NovaTubeAppSettings
import com.example.data.local.PreferenceTagEntity
import com.example.ui.auth.onGoogleSignInClicked
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
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    currentUser: FirebaseUser?,
    settings: NovaTubeAppSettings,
    preferenceTags: List<PreferenceTagEntity>,
    downloadsCount: Int,
    historyCount: Int,
    adsBlockedCount: Int,
    onUpdateSettings: ((NovaTubeAppSettings) -> NovaTubeAppSettings) -> Unit,
    onOpenAiBrainTab: () -> Unit,
    onReopenSetupWizard: () -> Unit,
    onResetDefaultTastes: () -> Unit,
    onClearHistory: () -> Unit,
    onOpenApkExportDialog: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var nicknameInput by remember(settings.userNickname) { mutableStateOf(settings.userNickname) }
    var aiSensSlider by remember(settings.aiSensitivityPercent) {
        mutableFloatStateOf(settings.aiSensitivityPercent.toFloat())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Account & Google Sign-In Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(CrimsonPulse, CyberCyan)),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentUser?.photoUrl != null) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Аватар Google",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName ?: "Профиль: ${settings.userNickname}",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = currentUser?.email ?: "Локальный профиль • Google не подключён",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentUser != null) EmeraldShield else TextSecondary
                            )
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrimsonPulse
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isSigningIn = true
                                authError = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = { isSigningIn = false },
                                    onAuthError = { err ->
                                        isSigningIn = false
                                        authError = err
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = { isSigningIn = false }
                                )
                            },
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPulse,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentUser != null) "Сменить Google" else "Войти в Google",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        if (currentUser != null) {
                            OutlinedButton(
                                onClick = onSignOut,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("settings_sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = CrimsonPulse,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Выйти", color = CrimsonPulse)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = {
                            nicknameInput = it
                            onUpdateSettings { s -> s.copy(userNickname = it) }
                        },
                        label = { Text("Локальное имя для ИИ-приветствий") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. AI Personalization & Local Taste Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Персонализация и Локальный ИИ",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Активных тегов в Room БД: ${preferenceTags.count { !it.isBlocked }} • Заблокировано: ${preferenceTags.count { it.isBlocked }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsToggleItem(
                        title = "Авто-обучение весов по лайкам (+4%)",
                        subtitle = "Автоматически усиливать любимые темы при нажатии «Нравится»",
                        checked = settings.autoLearnFromLikes,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(autoLearnFromLikes = v) } }
                    )

                    SettingsToggleItem(
                        title = "Жёсткий блок-фильтр (Анти-Roblox и стоп-слова)",
                        subtitle = "Полностью вырезать заблокированные темы из ленты и живого поиска YouTube",
                        checked = settings.strictBlockedTopicsFilter,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(strictBlockedTopicsFilter = v) } }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Чувствительность ИИ-рекомендаций: ${aiSensSlider.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Slider(
                        value = aiSensSlider,
                        onValueChange = { aiSensSlider = it },
                        onValueChangeFinished = {
                            onUpdateSettings { it.copy(aiSensitivityPercent = aiSensSlider.toInt()) }
                        },
                        valueRange = 50f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = ObsidianElevated
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenAiBrainTab,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Редактор тегов ИИ", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onReopenSetupWizard,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reopen_setup_wizard_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Мастер сетапа", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onResetDefaultTastes,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Восстановить базовые вкусы Саши (ГАЗ-24-10, ESP32, NFS, ретро)")
                    }
                }
            }
        }

        // 3. AdBlock & SponsorBlock Engine Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldShield)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Анти-Реклама и SponsorBlock",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Всего заблокировано рекламных вставок: $adsBlockedCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldShield
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsToggleItem(
                        title = "Сетевой фильтр рекламы YouTube (AdGuard)",
                        subtitle = "Блокировать рекламные домены doubleclick, googleads и прероллы",
                        checked = settings.adBlockEnabled,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(adBlockEnabled = v) } }
                    )

                    SettingsToggleItem(
                        title = "SponsorBlock: пропуск спонсорских интеграций",
                        subtitle = "Автоматически перематывать встроенную рекламу внутри роликов",
                        checked = settings.sponsorBlockEnabled,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(sponsorBlockEnabled = v) } }
                    )

                    SettingsToggleItem(
                        title = "Пропуск саморекламы и просьб о подписке",
                        subtitle = "Перематывать неинформативные вступления и концовки",
                        checked = settings.skipSelfPromoEnabled,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(skipSelfPromoEnabled = v) } }
                    )

                    SettingsToggleItem(
                        title = "Показывать тост-уведомление при перемотке",
                        subtitle = "Отображать плашку сколько секунд спонсора сэкономлено",
                        checked = settings.showSkipToastEnabled,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(showSkipToastEnabled = v) } }
                    )
                }
            }
        }

        // 4. Video Player & Built-in Downloader Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = AmberVu)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Плеер и Скачиватель видео",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Качество скачивания по умолчанию:",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1080p MP4", "720p MP4", "480p MP4", "Audio M4A").forEach { q ->
                            FilterChip(
                                selected = settings.defaultDownloadQuality == q,
                                onClick = {
                                    onUpdateSettings { it.copy(defaultDownloadQuality = q) }
                                },
                                label = { Text(q, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberVu,
                                    selectedLabelColor = ObsidianBg
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Скорость воспроизведения по умолчанию:",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                            FilterChip(
                                selected = settings.defaultPlaybackSpeed == spd,
                                onClick = {
                                    onUpdateSettings { it.copy(defaultPlaybackSpeed = spd) }
                                },
                                label = { Text("${spd}x", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan,
                                    selectedLabelColor = ObsidianBg
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleItem(
                        title = "Предпочитать нативный MP4-плеер",
                        subtitle = "Открывать прямой поток VideoView вместо YouTube WebView по умолчанию",
                        checked = settings.preferNativeMp4Player,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(preferNativeMp4Player = v) } }
                    )

                    SettingsToggleItem(
                        title = "Ретро-ЭЛТ и кибер-акценты интерфейса",
                        subtitle = "Фирменная тёмная стилистика с индикаторами в духе старой электроники",
                        checked = settings.crtRetroAccentEnabled,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(crtRetroAccentEnabled = v) } }
                    )
                }
            }
        }

        // 5. Storage, History & Direct APK Export
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = EmeraldShield)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Память, История и Выгрузка APK",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Скачано файлов: $downloadsCount (Movies/NovaTube) • В истории: $historyCount видео",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenApkExportDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldShield,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_export_apk_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "СКАЧАТЬ APK НА ТЕЛЕФОН / ПК (Ссылка + QR)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onClearHistory,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CrimsonPulse)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Очистить локальную историю просмотров", color = CrimsonPulse)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldShield,
                checkedTrackColor = EmeraldShield.copy(alpha = 0.3f)
            )
        )
    }
}
