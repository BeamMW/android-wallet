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
import android.os.Build
import android.os.LocaleList
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import java.util.*

object LocaleHelper {
    private const val enLocaleIndex = 0
    private const val enLanguageCode = "en"
    private var localeIndex = enLocaleIndex
    private var languageCode = enLanguageCode
    private val localeCodes: List<String> by lazy {
        App.self.resources.getStringArray(R.array.language_codes).toList()
    }

    val languages: List<String> by lazy {
        App.self.resources.getStringArray(R.array.languages).toList()
    }

    val currentLanguage: String
        get() = languages[localeIndex]

    val currentLanguageIndex: Int
        get() = localeIndex

    fun loadLocale() {
        languageCode = PreferencesManager.getString(PreferencesManager.KEY_LANGUAGE_CODE) ?: ""
        val indexFromSettings = localeCodes.indexOf(languageCode)


        if (indexFromSettings < 0) {
            val systemLocaleCode = Locale.getDefault().language

            if (localeCodes.contains(systemLocaleCode)) {
                localeIndex = localeCodes.indexOf(systemLocaleCode)
                languageCode = localeCodes[localeIndex]
            } else {
                localeIndex = enLocaleIndex
                languageCode = enLanguageCode
            }

        } else {
            localeIndex = indexFromSettings
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
                conf?.locales = LocaleList(AppConfig.LOCALE).apply { LocaleList.setDefault(this) }
            }

            Locale.setDefault(AppConfig.LOCALE)

            val newContext = if (conf == null) null else context.createConfigurationContext(conf)
            return android.content.ContextWrapper(newContext)
        }
    }

    fun selectLanguage(languageIndex: Int) {
        localeIndex = if (languageIndex >= localeCodes.size) 0 else languageIndex
        languageCode = localeCodes[localeIndex]

        updateApplicationConfig()
    }
}