package com.mw.beam.beamwallet.welcomeScreen.welcomeValidation

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.Phrases

/**
 * Created by vain onnellinen on 11/1/18.
 */
interface WelcomeValidationContract {
    interface View : MvpView {
        fun init()
        fun getData(): Phrases?
        fun configPhrases(phrasesToValidate: MutableList<Int>, phrases : Phrases)
        fun showPasswordsFragment()
        fun handleNextButton()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNextPressed()
        fun onPhraseChanged()
    }

    interface Repository : MvpRepository {
        fun getPhrasesToValidate(): MutableList<Int>
        var phrases : Phrases?
    }
}
