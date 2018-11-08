package com.mw.beam.beamwallet.welcomeScreen.welcomeRecover

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/5/18.
 */
interface WelcomeRecoverContract {
    interface View : MvpView {
        fun init()
        fun configPhrases(phrasesCount : Int)
        fun handleRecoverButton()
    }

    interface Presenter : MvpPresenter<View> {
        fun onRecoverPressed()
        fun onPhraseChanged()
    }

    interface Repository : MvpRepository {
        var phrasesCount : Int
        fun recoverWallet() : Boolean
    }
}
