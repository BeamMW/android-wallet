package com.mw.beam.beamwallet.screens.language

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface LanguageContract {

    interface View: MvpView {
        fun init(languages: List<String>, currentLanguage: Int)
        fun showConfirmDialog(languageIndex: Int)
    }

    interface Presenter: MvpPresenter<View> {
        fun onSelectLanguage(index: Int)
        fun onRestartPressed(index: Int)
    }

    interface Repository: MvpRepository {
        fun getLanguages(): List<String>
        fun getCurrentLanguageIndex(): Int
        fun setLanguage(index: Int)
    }
}