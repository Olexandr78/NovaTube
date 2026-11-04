package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object ApkSelfExporter {

    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun getInstalledApkFile(context: Context): File {
        return File(context.applicationInfo.sourceDir)
    }

    suspend fun exportAndUploadApk(
        context: Context,
        onStatusUpdate: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sourceApk = getInstalledApkFile(context)
            if (!sourceApk.exists()) {
                return@withContext Result.failure(IllegalStateException("APK не найден в sourceDir"))
            }

            // 1. Copy to local External Downloads / Movies folder
            val localDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!localDir.exists()) localDir.mkdirs()
            val localCopy = File(localDir, "app-debug.apk")
            sourceApk.copyTo(localCopy, overwrite = true)

            onStatusUpdate("Генерация прямой внешней ссылки на app-debug.apk (${"%.1f".format(localCopy.length() / (1024.0 * 1024.0))} МБ)...")

            // 2. Try Litterbox (Catbox temporary hosting - 72h, very reliable for binary APKs)
            uploadToLitterbox(localCopy)?.let { url ->
                return@withContext Result.success(url)
            }

            // 3. Try Catbox permanent upload
            uploadToCatbox(localCopy)?.let { url ->
                return@withContext Result.success(url)
            }

            // 4. Try 0x0.st
            uploadTo0x0(localCopy)?.let { url ->
                return@withContext Result.success(url)
            }

            // 5. Try temp.sh
            uploadToTempSh(localCopy)?.let { url ->
                return@withContext Result.success(url)
            }

            Result.failure(IllegalStateException("Локальная копия сохранена в ${localCopy.absolutePath}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uploadToLitterbox(apkFile: File): String? {
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("reqtype", "fileupload")
                .addFormDataPart("time", "72h")
                .addFormDataPart(
                    "fileToUpload",
                    "NovaTube-app-debug.apk",
                    apkFile.asRequestBody("application/vnd.android.package-archive".toMediaType())
                )
                .build()

            val req = Request.Builder()
                .url("https://litterbox.catbox.moe/resources/internals/api.php")
                .header("User-Agent", "NovaTubeApkExporter/1.0")
                .post(body)
                .build()

            uploadClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val text = resp.body?.string()?.trim().orEmpty()
                if (text.startsWith("https://")) text else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun uploadToCatbox(apkFile: File): String? {
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("reqtype", "fileupload")
                .addFormDataPart(
                    "fileToUpload",
                    "NovaTube-app-debug.apk",
                    apkFile.asRequestBody("application/vnd.android.package-archive".toMediaType())
                )
                .build()

            val req = Request.Builder()
                .url("https://catbox.moe/user/api.php")
                .header("User-Agent", "NovaTubeApkExporter/1.0")
                .post(body)
                .build()

            uploadClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val text = resp.body?.string()?.trim().orEmpty()
                if (text.startsWith("https://")) text else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun uploadTo0x0(apkFile: File): String? {
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "NovaTube-app-debug.apk",
                    apkFile.asRequestBody("application/vnd.android.package-archive".toMediaType())
                )
                .build()

            val req = Request.Builder()
                .url("https://0x0.st")
                .header("User-Agent", "curl/8.5.0")
                .post(body)
                .build()

            uploadClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val text = resp.body?.string()?.trim().orEmpty()
                if (text.startsWith("https://")) text else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun uploadToTempSh(apkFile: File): String? {
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "NovaTube-app-debug.apk",
                    apkFile.asRequestBody("application/vnd.android.package-archive".toMediaType())
                )
                .build()

            val req = Request.Builder()
                .url("https://temp.sh/upload")
                .post(body)
                .build()

            uploadClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val text = resp.body?.string()?.trim().orEmpty()
                if (text.startsWith("http")) text else null
            }
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun ApkDirectDownloadDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apkFile = remember { ApkSelfExporter.getInstalledApkFile(context) }
    val apkSizeMb = remember(apkFile) {
        "%.2f МБ".format(apkFile.length() / (1024.0 * 1024.0))
    }

    var isUploading by remember { mutableStateOf(false) }
    var statusText by remember {
        mutableStateOf("Генерируем прямую ссылку для скачивания app-debug.apk ($apkSizeMb)...")
    }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var copiedToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isUploading = true
        val res = ApkSelfExporter.exportAndUploadApk(context) { msg ->
            statusText = msg
        }
        isUploading = false
        res.onSuccess { url ->
            downloadUrl = url
            statusText = "Готово! Скачайте app-debug.apk по прямой ссылке или QR-коду ниже:"
        }.onFailure { err ->
            statusText = err.message ?: "Не удалось получить внешнюю ссылку"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, EmeraldShield, RoundedCornerShape(22.dp))
                .testTag("apk_export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = EmeraldShield,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Скачать NovaTube APK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "app-debug.apk • Размер: $apkSizeMb",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldShield
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                if (isUploading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = EmeraldShield)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        color = EmeraldShield,
                        trackColor = ObsidianElevated,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (downloadUrl != null) {
                    val url = downloadUrl!!
                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Code for instant phone camera scanning
                    val qrUrl = remember(url) {
                        "https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=" +
                            URLEncoder.encode(url, "UTF-8")
                    }
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(160.dp)
                    ) {
                        AsyncImage(
                            model = qrUrl,
                            contentDescription = "QR код для скачивания APK",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = url,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Прямая ссылка на app-debug.apk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("NovaTube APK URL", url))
                                copiedToast = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldShield,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (copiedToast) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copiedToast) "Скопировано!" else "Копировать URL",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (_: Exception) {
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                } else if (!isUploading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isUploading = true
                                statusText = "Повторная выгрузка app-debug.apk..."
                                val res = ApkSelfExporter.exportAndUploadApk(context) { statusText = it }
                                isUploading = false
                                res.onSuccess { downloadUrl = it }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberVu,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сгенерировать ссылку ещё раз", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
