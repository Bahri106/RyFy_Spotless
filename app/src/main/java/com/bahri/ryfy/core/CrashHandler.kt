package com.bahri.ryfy.core

import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import com.bahri.ryfy.BuildConfig
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

object CrashHandler : Thread.UncaughtExceptionHandler {

    private const val TAG = "CrashHandler"
    private const val REPORT_URL = "https://bahrixyz.my.id/api/crash-report"

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Log.i(TAG, "CrashHandler installed successfully")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val device = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})"
            val version = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
            val errorType = throwable::class.java.name

            val location = throwable.stackTrace.firstOrNull()?.let {
                "${it.fileName ?: "Unknown"}:${it.lineNumber}"
            } ?: "Unknown"

            // 1. Save crash log locally
            saveLocalDump(device, version, errorType, location, stackTrace)

            // 2. Transmit crash report to server proxy synchronously with 2.5s timeout
            sendCrashReportSync(device, version, errorType, location, stackTrace)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle crash: ${e.message}", e)
        } finally {
            // Hand off to default system handler or exit cleanly
            defaultHandler?.uncaughtException(thread, throwable) ?: run {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }

    private fun saveLocalDump(device: String, version: String, errorType: String, location: String, stackTrace: String) {
        runCatching {
            val ctx = appContext ?: return
            val dir = File(ctx.filesDir, "crashes")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "latest_crash.txt")
            file.writeText(
                "Device: $device\n" +
                "Version: $version\n" +
                "Error: $errorType\n" +
                "Location: $location\n\n" +
                "Stacktrace:\n$stackTrace"
            )
        }
    }

    private fun sendCrashReportSync(device: String, version: String, errorType: String, location: String, stackTrace: String) {
        val networkThread = Thread {
            runCatching {
                val json = JSONObject().apply {
                    put("device", device)
                    put("version", version)
                    put("error_type", errorType)
                    put("location", location)
                    put("stacktrace", stackTrace)
                }

                val conn = URL(REPORT_URL).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 2500
                conn.readTimeout = 2500
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                conn.outputStream.use { os ->
                    os.write(json.toString().toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val code = conn.responseCode
                Log.i(TAG, "Crash report sent, response code: $code")
                conn.disconnect()
            }
        }

        networkThread.start()
        try {
            networkThread.join(2500)
        } catch (_: InterruptedException) {}
    }
}
