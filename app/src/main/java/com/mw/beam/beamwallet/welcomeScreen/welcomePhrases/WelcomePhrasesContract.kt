package com.mw.beam.beamwallet.welcomeScreen.welcomePhrases

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 10/30/18.
 */
interface WelcomePhrasesContract {
    interface View : MvpView {
        fun init()
        fun showValidationFragment(phrases: MutableList<String>)
        fun configPhrases(phrases: MutableList<String>)
        fun copyToClipboard(data: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun onNextPressed()
        fun onCopyPressed()
    }

    interface Repository : MvpRepository {
        fun getPhrases(): MutableList<String>
    }
}
