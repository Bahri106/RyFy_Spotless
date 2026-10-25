package com.bahri.ryfy.update

import org.json.JSONObject
import java.io.File

data class UpdateInfo(
    val versionName: String,
    val versionCode: Long,
    val publishedAt: String,
    val changelog: List<String>,
    val downloadUrl: String,
    val fileSizeBytes: Long = 0L
) {
    companion object {
        fun fromJson(jsonStr: String): UpdateInfo {
            val json = JSONObject(jsonStr)
            val changes = mutableListOf<String>()
            val changelogArr = json.optJSONArray("changelog")
            if (changelogArr != null) {
                for (i in 0 until changelogArr.length()) {
                    changes.add(changelogArr.getString(i))
                }
            }
            return UpdateInfo(
                versionName = json.optString("versionName", "1.0.0"),
                versionCode = json.optLong("versionCode", 1L),
                publishedAt = json.optString("publishedAt", ""),
                changelog = changes,
                downloadUrl = json.optString("downloadUrl", ""),
                fileSizeBytes = json.optLong("fileSizeBytes", 0L)
            )
        }
    }
}

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progressPercent: Int) : UpdateState
    data class ReadyToInstall(val apkFile: File, val info: UpdateInfo) : UpdateState
    data object UpToDate : UpdateState
    data class Error(val message: String) : UpdateState
}
