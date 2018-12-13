package com.mw.beam.beamwallet.welcomeScreen.welcomePhrase

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 10/30/18.
 */
interface WelcomePhraseContract {
    interface View : MvpView {
        fun showValidationFragment(phrases: Array<String>)
        fun configPhrases(phrases: Array<String>)
        fun copyToClipboard(data: String)
        fun showCopiedAlert()
        fun showSaveAlert()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNextPressed()
        fun onCopyPressed()
        fun onDonePressed()
    }

    interface Repository : MvpRepository {
        val phrases: Array<String>
    }
}
