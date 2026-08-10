/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.core.helpers

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import com.mw.beam.beamwallet.core.AppConfig
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

object LocaleHelper {
    private const val enLanguageCode = "en"
    private val englishLanguage = SupportedLanguage(enLanguageCode, "English", "English")
    private val systemLocale: Locale = getSystemLocale()

    // One entry per values-<code> resource directory.
    val supportedLanguages = listOf(
            englishLanguage,
            SupportedLanguage("be", "Belarusian", "Беларуская"),
            SupportedLanguage("cs", "Czech", "Čeština"),
            SupportedLanguage("de", "German", "Deutsch"),
            SupportedLanguage("es", "Spanish", "Español"),
            SupportedLanguage("fi", "Finnish", "Suomi"),
            SupportedLanguage("fr", "French", "Français"),
            SupportedLanguage("id", "Indonesian", "Bahasa Indonesia"),
            SupportedLanguage("it", "Italian", "Italiano"),
            SupportedLanguage("ja", "Japanese", "日本語"),
            SupportedLanguage("ko", "Korean", "한국어"),
            SupportedLanguage("nl", "Dutch", "Nederlands"),
            SupportedLanguage("ru", "Russian", "Русский"),
            SupportedLanguage("sr", "Serbian", "Српски"),
            SupportedLanguage("sv", "Swedish", "Svenska"),
            SupportedLanguage("th", "Thai", "ภาษาไทย"),
            SupportedLanguage("tr", "Turkish", "Türkçe"),
            SupportedLanguage("uk", "Ukrainian", "Українська"),
            SupportedLanguage("vi", "Vietnamese", "Tiếng Việt"),
            SupportedLanguage("zh", "Chinese", "中文")
    )

    private var languageCode = enLanguageCode

    fun getCurrentLanguage(): SupportedLanguage {
        val l = supportedLanguages.firstOrNull { it.languageCode == languageCode }
                ?: englishLanguage
        return l
    }

    fun loadLocale() {
        languageCode = PreferencesManager.getString(PreferencesManager.KEY_LANGUAGE_CODE) ?: enLanguageCode
        val isSupportedSavedLanguage = supportedLanguages.any { it.languageCode == languageCode }

        if (!isSupportedSavedLanguage) {
            val systemLanguageCode = Locale.getDefault().language
            val isSupportedSystemLanguage = supportedLanguages.any { it.languageCode == systemLanguageCode }

            languageCode = if (isSupportedSystemLanguage) {
                systemLanguageCode
            } else {
                enLanguageCode
            }
        }

        updateApplicationConfig()
    }

    private fun updateApplicationConfig() {
        PreferencesManager.putString(PreferencesManager.KEY_LANGUAGE_CODE, languageCode)
        AppConfig.LOCALE = Locale(languageCode)
    }

    object ContextWrapper {
        fun wrap(context: Context?): android.content.ContextWrapper {
            val conf = context?.resources?.configuration
            conf?.setLocale(AppConfig.LOCALE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conf?.setLocales(LocaleList(AppConfig.LOCALE).apply { LocaleList.setDefault(this) })
            }

            Locale.setDefault(AppConfig.LOCALE)

            val newContext = if (conf == null) null else context.createConfigurationContext(conf)
            return android.content.ContextWrapper(newContext)
        }
    }

    fun selectLanguage(language: SupportedLanguage) {
        languageCode = language.languageCode
        updateApplicationConfig()
    }

    fun getSortedLanguages(languages: List<SupportedLanguage>): List<SupportedLanguage> {
        try {
            val sorted = languages.sortedWith(compareBy { it.englishName })
            val sortedLanguages: ArrayList<SupportedLanguage> = ArrayList(sorted.size)
            val otherLanguages: ArrayList<SupportedLanguage> = ArrayList(sorted.size)

            for (language in sorted) {
                var currentLanguageCode = language.languageCode
                if (currentLanguageCode == enLanguageCode) {
                    sortedLanguages.add(0, language)
                } else if (currentLanguageCode == systemLocale.language) {
                    sortedLanguages.add(1, language)
                } else if (currentLanguageCode != enLanguageCode && currentLanguageCode != systemLocale.language) {
                    otherLanguages.add(language)
                }
            }
            sortedLanguages.addAll(otherLanguages)
            return sortedLanguages
        } catch (e: NullPointerException) {
            return languages
        }
        catch (e: Exception) {
            return languages
        }
    }

    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            Resources.getSystem().configuration.locale
        }
    }

    data class SupportedLanguage(val languageCode: String, val englishName: String, val nativeName: String)
}