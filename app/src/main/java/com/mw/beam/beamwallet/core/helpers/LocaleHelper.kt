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
    private var localeIndex = enLocaleIndex
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
//        val indexFromSettings = PreferencesManager.getInt(PreferencesManager.KEY_LOCALE_INDEX, -1)
//
//        if (indexFromSettings < 0) {
//            val systemLocaleCode = Locale.getDefault().language
//
//            localeIndex = if (localeCodes.contains(systemLocaleCode)) {
//                localeCodes.indexOf(systemLocaleCode)
//            } else {
//                enLocaleIndex
//            }
//        } else {
//
//            localeIndex = if (indexFromSettings >= localeCodes.size) {
//                enLocaleIndex
//            } else {
//                indexFromSettings
//            }
//        }

        updateApplicationConfig()
    }

    private fun updateApplicationConfig() {
        PreferencesManager.putInt(PreferencesManager.KEY_LOCALE_INDEX, localeIndex)
        AppConfig.LOCALE = Locale(localeCodes[localeIndex])
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

        updateApplicationConfig()
    }
}