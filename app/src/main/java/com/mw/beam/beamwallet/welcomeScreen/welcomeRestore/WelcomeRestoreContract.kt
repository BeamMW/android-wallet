package com.mw.beam.beamwallet.welcomeScreen.welcomeRestore

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/5/18.
 */
interface WelcomeRestoreContract {
    interface View : MvpView {
        fun init()
        fun configPhrases(phrasesCount : Int)
        fun handleRestoreButton()
        fun getPhrase() : Array<String>
        fun showPasswordsFragment(phrases : Array<String>)
        fun clearWindowState()
    }

    interface Presenter : MvpPresenter<View> {
        fun onRestorePressed()
        fun onPhraseChanged()
    }

    interface Repository : MvpRepository {
        fun restoreWallet() : Boolean
    }
}
