package com.bahri.ryfy.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.bahri.ryfy.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class UpdateManager(private val context: Context) {
    companion object {
        private const val TAG = "RyFyUpdateManager"
        const val UPDATE_URL = "https://raw.githubusercontent.com/Bahri106/RyFy_Spotless/main/version.json"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    fun checkForUpdates(manual: Boolean = true) {
        if (_state.value is UpdateState.Checking || _state.value is UpdateState.Downloading) return
        _state.value = UpdateState.Checking

        scope.launch {
            try {
                val request = Request.Builder()
                    .url(UPDATE_URL)
                    .header("User-Agent", "RyFy-Android-App")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _state.value = UpdateState.Error("HTTP error ${response.code}")
                        return@use
                    }
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        _state.value = UpdateState.Error("Respon server kosong")
                        return@use
                    }

                    val info = UpdateInfo.fromJson(body)
                    val currentCode = BuildConfig.VERSION_CODE.toLong()

                    if (info.versionCode > currentCode) {
                        _state.value = UpdateState.Available(info)
                    } else {
                        _state.value = UpdateState.UpToDate
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal memeriksa pembaruan: ${e.message}", e)
                _state.value = UpdateState.Error(e.message ?: "Koneksi gagal")
            }
        }
    }

    fun downloadUpdate(info: UpdateInfo) {
        if (_state.value is UpdateState.Downloading) return
        _state.value = UpdateState.Downloading(info, 0)

        scope.launch {
            try {
                val downloadDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val targetFile = File(downloadDir, "ryfy-update-${info.versionCode}.apk")

                val request = Request.Builder()
                    .url(info.downloadUrl)
                    .header("User-Agent", "RyFy-Android-App")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                    val body = response.body ?: throw Exception("Body kosong")
                    val totalBytes = body.contentLength()

                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var read: Int
                            var downloaded: Long = 0
                            var lastPercent = 0

                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloaded += read
                                if (totalBytes > 0) {
                                    val percent = ((downloaded * 100) / totalBytes).toInt()
                                    if (percent != lastPercent) {
                                        lastPercent = percent
                                        _state.value = UpdateState.Downloading(info, percent)
                                    }
                                }
                            }
                        }
                    }
                }

                _state.value = UpdateState.ReadyToInstall(targetFile, info)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengunduh pembaruan: ${e.message}", e)
                _state.value = UpdateState.Error(e.message ?: "Pengunduhan gagal")
            }
        }
    }

    fun installUpdate(apkFile: File): Boolean {
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memulai instalasi paket: ${e.message}", e)
            false
        }
    }

    fun dismissUpdate() {
        _state.value = UpdateState.Idle
    }
}
