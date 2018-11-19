package com.mw.beam.beamwallet.welcomeScreen

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomePresenter(currentView: WelcomeContract.View, private val repository: WelcomeContract.Repository)
    : BasePresenter<WelcomeContract.View>(currentView),
        WelcomeContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.showWelcomeMainFragment()
    }

    override fun onCreateWallet() {
        view?.showDescriptionFragment()
    }

    override fun onGeneratePhrase() {
        view?.showPhrasesFragment()
    }

    override fun onOpenWallet() {
        view?.showMainActivity()
    }

    override fun onProceedToPasswords(phrases : Array<String>) {
        view?.showPasswordsFragment(phrases)
    }

    override fun onProceedToValidation(phrases: Array<String>) {
        view?.showValidationFragment(phrases)
    }

    override fun onRecoverWallet() {
        view?.showRecoverFragment()
    }
}
