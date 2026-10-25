package com.bahri.ryfy.core

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object AppLanguageManager {

    const val LANG_SYSTEM = "system"
    const val LANG_EN = "en"
    const val LANG_IN = "in"
    const val LANG_ES = "es"
    const val LANG_JA = "ja"
    const val LANG_ZH = "zh"
    const val LANG_RU = "ru"
    const val LANG_AR = "ar"
    const val LANG_DE = "de"
    const val LANG_FR = "fr"
    const val LANG_PT = "pt"

    private const val PREFS_NAME = "ryfy_language_prefs"
    private const val PREF_KEY_LANGUAGE = "app_language_preference"

    private val _currentLanguage = MutableStateFlow(LANG_SYSTEM)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    data class LanguageOption(
        val code: String,
        val nativeName: String,
        val englishName: String
    )

    val supportedLanguages = listOf(
        LanguageOption(LANG_SYSTEM, "Default Sistem", "System Default"),
        LanguageOption(LANG_EN, "English", "English"),
        LanguageOption(LANG_IN, "Bahasa Indonesia", "Indonesian"),
        LanguageOption(LANG_ES, "Español", "Spanish"),
        LanguageOption(LANG_JA, "日本語", "Japanese"),
        LanguageOption(LANG_ZH, "中文 (简体)", "Chinese"),
        LanguageOption(LANG_RU, "Русский", "Russian"),
        LanguageOption(LANG_AR, "العربية", "Arabic"),
        LanguageOption(LANG_DE, "Deutsch", "German"),
        LanguageOption(LANG_FR, "Français", "French"),
        LanguageOption(LANG_PT, "Português", "Portuguese")
    )

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(PREF_KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM
        _currentLanguage.value = savedLang
        applyLocaleToProcess(savedLang)
    }

    fun wrapContext(context: Context): Context {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(PREF_KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM
        if (savedLang == LANG_SYSTEM) {
            return context
        }

        val targetLocale = resolveLocale(savedLang)
        Locale.setDefault(targetLocale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        return context.createConfigurationContext(config)
    }

    fun setLanguage(context: Context, langCode: String, activity: Activity? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSaved = prefs.getString(PREF_KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM

        if (currentSaved.equals(langCode, ignoreCase = true)) {
            return
        }

        prefs.edit().putString(PREF_KEY_LANGUAGE, langCode).commit()
        _currentLanguage.value = langCode
        applyLocaleToProcess(langCode)

        var osRecreating = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val targetList = if (langCode == LANG_SYSTEM) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(langCode)
                }
                if (localeManager?.applicationLocales != targetList) {
                    localeManager?.applicationLocales = targetList
                    osRecreating = true
                }
            }
        }

        if (!osRecreating) {
            val targetActivity = activity ?: (context as? Activity)
            targetActivity?.recreate()
        }
    }

    private fun applyLocaleToProcess(langCode: String) {
        if (langCode == LANG_SYSTEM) return
        val targetLocale = resolveLocale(langCode)
        Locale.setDefault(targetLocale)
    }

    fun resolveLocale(code: String): Locale {
        return when (code.lowercase()) {
            LANG_EN -> Locale.ENGLISH
            LANG_IN -> Locale("in", "ID")
            LANG_ES -> Locale("es", "ES")
            LANG_JA -> Locale.JAPANESE
            LANG_ZH -> Locale.SIMPLIFIED_CHINESE
            LANG_RU -> Locale("ru", "RU")
            LANG_AR -> Locale("ar")
            LANG_DE -> Locale.GERMAN
            LANG_FR -> Locale.FRENCH
            LANG_PT -> Locale("pt", "BR")
            else -> Locale.getDefault()
        }
    }
}
