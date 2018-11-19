package com.mw.beam.beamwallet.welcomeScreen

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 10/19/18.
 */
interface WelcomeContract {
    interface View : MvpView {
        fun showWelcomeMainFragment()
        fun showDescriptionFragment()
        fun showPasswordsFragment(phrases : Array<String>)
        fun showPhrasesFragment()
        fun showRecoverFragment()
        fun showValidationFragment(phrases: Array<String>)
        fun showMainActivity()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCreateWallet()
        fun onRecoverWallet()
        fun onGeneratePhrase()
        fun onOpenWallet()
        fun onProceedToPasswords(phrases : Array<String>)
        fun onProceedToValidation(phrases: Array<String>)
    }

    interface Repository : MvpRepository
}
