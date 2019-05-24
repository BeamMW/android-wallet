package com.mw.beam.beamwallet.screens.language

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.LocaleHelper

class LanguageRepository: BaseRepository(), LanguageContract.Repository {

    override fun getLanguages(): List<String> {
        return LocaleHelper.languages
    }

    override fun getCurrentLanguageIndex(): Int {
        return LocaleHelper.currentLanguageIndex
    }

    override fun setLanguage(index: Int) {
        return LocaleHelper.selectLanguage(index)
    }
}