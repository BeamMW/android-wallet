package com.mw.beam.beamwallet.welcomeScreen.welcomeValidation

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/1/18.
 */
interface WelcomeValidationContract {
    interface View : MvpView {
        fun getData(): Array<String>?
        fun configPhrases(phrasesToValidate: MutableList<Int>, phrases : Array<String>)
        fun showPasswordsFragment(phrases : Array<String>)
        fun handleNextButton()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNextPressed()
        fun onPhraseChanged()
    }

    interface Repository : MvpRepository {
        fun getPhrasesToValidate(): MutableList<Int>
        var phrases : Array<String>?
    }
}
