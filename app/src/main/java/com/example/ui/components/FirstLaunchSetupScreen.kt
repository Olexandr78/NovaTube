package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.NovaTubeAppSettings
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

data class SetupPresetTopic(
    val tag: String,
    val category: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstLaunchSetupScreen(
    currentUser: FirebaseUser?,
    initialSettings: NovaTubeAppSettings,
    onOpenApkDownloadDialog: () -> Unit,
    onCompleteSetup: (
        updatedSettings: NovaTubeAppSettings,
        selectedFavoriteTopics: List<SetupPresetTopic>,
        selectedBlockedTopics: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentStep by remember { mutableIntStateOf(1) }
    var nickname by remember { mutableStateOf(initialSettings.userNickname.ifBlank { "Саша" }) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Available positive presets tailored to Sasha + general tech/gaming
    val presetTopics = remember {
        listOf(
            SetupPresetTopic("ГАЗ-24-10 Волга", "Автомеханика"),
            SetupPresetTopic("ESP32 & Raspberry Pi", "Код и Железо"),
            SetupPresetTopic("Need for Speed (Carbon & MW 2005)", "Гонки и Игры"),
            SetupPresetTopic("3D-печать Ender-3 V3 SE", "Код и Железо"),
            SetupPresetTopic("ЭЛТ и кассетные ВМ AKAI", "Ретро-техника"),
            SetupPresetTopic("Python & Linux", "Код и Железо"),
            SetupPresetTopic("Minecraft & Geometry Dash", "Гонки и Игры"),
            SetupPresetTopic("Английский и Информатика", "Школа и ИТ"),
            SetupPresetTopic("Оптические диски и кассетные плееры", "Ретро-техника"),
            SetupPresetTopic("Сборка кастомных девайсов (DIY)", "Код и Железо")
        )
    }

    val selectedPositiveTags = remember {
        mutableStateListOf(
            "ГАЗ-24-10 Волга",
            "ESP32 & Raspberry Pi",
            "Need for Speed (Carbon & MW 2005)",
            "3D-печать Ender-3 V3 SE",
            "ЭЛТ и кассетные ВМ AKAI",
            "Python & Linux",
            "Minecraft & Geometry Dash",
            "Английский и Информатика"
        )
    }

    var customPositiveInput by remember { mutableStateOf("") }
    val customPositiveTopics = remember { mutableStateListOf<SetupPresetTopic>() }

    val blockedTopics = remember {
        mutableStateListOf("Roblox")
    }
    var customBlockedInput by remember { mutableStateOf("") }

    // Step 3 preferences
    var adBlockEnabled by remember { mutableStateOf(initialSettings.adBlockEnabled) }
    var sponsorBlockEnabled by remember { mutableStateOf(initialSettings.sponsorBlockEnabled) }
    var autoLearnFromLikes by remember { mutableStateOf(initialSettings.autoLearnFromLikes) }
    var defaultDownloadQuality by remember { mutableStateOf(initialSettings.defaultDownloadQuality) }
    var preferNativeMp4Player by remember { mutableStateOf(initialSettings.preferNativeMp4Player) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
            .testTag("first_launch_setup_screen")
    ) {
        // Top Header with APK Export button and Step Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    tint = CrimsonPulse,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Мастер настройки NovaTube",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Шаг $currentStep из 3 • Персонализация и Аккаунт",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            }

            Surface(
                color = EmeraldShield,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clickable(onClick = onOpenApkDownloadDialog)
                    .testTag("setup_apk_download_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = ObsidianBg,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "APK",
                        style = MaterialTheme.typography.labelLarge,
                        color = ObsidianBg
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { currentStep / 3f },
            color = CrimsonPulse,
            trackColor = ObsidianElevated,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Always-Visible Google Sign-In & Account Status Banner inside Setup Wizard!
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        if (currentUser != null) listOf(EmeraldShield, CyberCyan)
                        else listOf(CrimsonPulse, CyberCyan)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (currentUser?.photoUrl != null) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Аватар Google",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (currentUser != null) EmeraldShield else CrimsonPulse,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentUser != null) {
                                    "Аккаунт Google подключён!"
                                } else {
                                    "Вход в аккаунт Google / YouTube"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = currentUser?.email
                                    ?: currentUser?.displayName
                                    ?: "Войдите в свой аккаунт Google прямо здесь для синхронизации с базой YouTube",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentUser != null) EmeraldShield else TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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

                // THE GOOGLE SIGN-IN BUTTON RIGHT INSIDE THE SETUP WIZARD
                Button(
                    onClick = {
                        isSigningIn = true
                        authError = null
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isSigningIn = false
                            },
                            onAuthError = { err ->
                                isSigningIn = false
                                authError = err
                            },
                            scope = coroutineScope,
                            onAuthCancelled = {
                                isSigningIn = false
                            }
                        )
                    },
                    enabled = !isSigningIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser != null) EmeraldShield.copy(alpha = 0.2f) else CrimsonPulse,
                        contentColor = if (currentUser != null) EmeraldShield else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Подключение аккаунта Google...", style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUser != null) {
                                "Сменить аккаунт Google (${currentUser.email ?: "Подключён"})"
                            } else {
                                "Войти в Google (Sign in with Google)"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step-Specific Content
        AnimatedContent(targetState = currentStep, label = "setup_step") { step ->
            when (step) {
                1 -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_ai_curator_banner_1791571091331),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ObsidianBg.copy(alpha = 0.5f))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Text(
                                        text = "Шаг 1: Профиль и Локальная ИИ-база",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Как к вам обращаться ИИ-куратору ленты?",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                label = { Text("Ваше имя / никнейм") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("setup_nickname_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Ваши предпочтения сохраняются локально на устройстве в Room БД. ИИ анализирует их без рекламных трекеров и строит персональную ленту из базы YouTube.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                2 -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Шаг 2: Что вы обожаете смотреть?",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Выберите любимые темы — по ним сразу загрузится живая база YouTube:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (presetTopics + customPositiveTopics).forEach { topic ->
                                    val isSelected = selectedPositiveTags.contains(topic.tag)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) selectedPositiveTags.remove(topic.tag)
                                            else selectedPositiveTags.add(topic.tag)
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        label = { Text(topic.tag, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberCyan,
                                            selectedLabelColor = ObsidianBg,
                                            selectedLeadingIconColor = ObsidianBg,
                                            containerColor = ObsidianCard,
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Add custom favorite topic
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customPositiveInput,
                                    onValueChange = { customPositiveInput = it },
                                    label = { Text("Добавить свою любимую тему...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clean = customPositiveInput.trim()
                                        if (clean.isNotEmpty()) {
                                            customPositiveTopics.add(SetupPresetTopic(clean, "Мои интересы"))
                                            if (!selectedPositiveTags.contains(clean)) {
                                                selectedPositiveTags.add(clean)
                                            }
                                            customPositiveInput = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Добавить", tint = CyberCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = ObsidianElevated)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = CrimsonPulse)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Блок-фильтр (Что скрывать навсегда)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CrimsonPulse
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                blockedTopics.forEach { bTag ->
                                    FilterChip(
                                        selected = true,
                                        onClick = { blockedTopics.remove(bTag) },
                                        label = { Text("🚫 $bTag", style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CrimsonPulse.copy(alpha = 0.22f),
                                            selectedLabelColor = CrimsonPulse
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customBlockedInput,
                                    onValueChange = { customBlockedInput = it },
                                    label = { Text("Добавить тему в чёрный список...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clean = customBlockedInput.trim()
                                        if (clean.isNotEmpty() && !blockedTopics.contains(clean)) {
                                            blockedTopics.add(clean)
                                            customBlockedInput = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "В блок", tint = CrimsonPulse)
                                }
                            }
                        }
                    }
                }

                else -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = AmberVu)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Шаг 3: Блокировка рекламы и Скачиватель",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SetupSwitchRow(
                                title = "AdGuard YouTube (0% рекламы)",
                                subtitle = "Вырезать все рекламные прероллы, баннеры и трекеры",
                                checked = adBlockEnabled,
                                onCheckedChange = { adBlockEnabled = it }
                            )

                            SetupSwitchRow(
                                title = "SponsorBlock Авто-перемотка",
                                subtitle = "Автоматически перематывать спонсорские вставки в видео",
                                checked = sponsorBlockEnabled,
                                onCheckedChange = { sponsorBlockEnabled = it }
                            )

                            SetupSwitchRow(
                                title = "Авто-обучение ИИ по лайкам",
                                subtitle = "Повышать вес локальных тегов на +4% при каждом лайке",
                                checked = autoLearnFromLikes,
                                onCheckedChange = { autoLearnFromLikes = it }
                            )

                            SetupSwitchRow(
                                title = "Нативный MP4-плеер по умолчанию",
                                subtitle = "Использовать прямой MP4 поток вместо YouTube Embed",
                                checked = preferNativeMp4Player,
                                onCheckedChange = { preferNativeMp4Player = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Качество скачивателя по умолчанию:",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("1080p MP4", "720p MP4", "480p MP4", "Audio M4A").forEach { q ->
                                    FilterChip(
                                        selected = defaultDownloadQuality == q,
                                        onClick = { defaultDownloadQuality = q },
                                        label = { Text(q, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberVu,
                                            selectedLabelColor = ObsidianBg
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Navigation Buttons: Back / Next / Complete Setup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Назад")
                }
            }

            if (currentStep < 3) {
                Button(
                    onClick = { currentStep++ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("setup_next_step_button")
                ) {
                    Text(
                        text = "Далее (Шаг ${currentStep + 1} из 3)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = {
                        val allAvailable = presetTopics + customPositiveTopics
                        val chosenTopics = allAvailable.filter { selectedPositiveTags.contains(it.tag) }
                        val finalSettings = initialSettings.copy(
                            isOnboardingCompleted = true,
                            userNickname = nickname.ifBlank { "Саша" },
                            adBlockEnabled = adBlockEnabled,
                            sponsorBlockEnabled = sponsorBlockEnabled,
                            autoLearnFromLikes = autoLearnFromLikes,
                            defaultDownloadQuality = defaultDownloadQuality,
                            preferNativeMp4Player = preferNativeMp4Player
                        )
                        onCompleteSetup(finalSettings, chosenTopics, blockedTopics.toList())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldShield,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.8f)
                        .height(52.dp)
                        .testTag("complete_setup_button")
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Запустить NovaTube",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSwitchRow(
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
