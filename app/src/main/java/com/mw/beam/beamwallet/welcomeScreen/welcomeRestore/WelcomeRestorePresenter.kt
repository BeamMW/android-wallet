package com.mw.beam.beamwallet.welcomeScreen.welcomeRestore

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestorePresenter(currentView: WelcomeRestoreContract.View, private val repository: WelcomeRestoreContract.Repository)
    : BasePresenter<WelcomeRestoreContract.View>(currentView),
        WelcomeRestoreContract.Presenter {

    override fun onStart() {
        super.onStart()
        view?.configPhrases(repository.phrasesCount)
    }

    override fun onRecoverPressed() {
        if (repository.recoverWallet()) {
            view?.showSnackBar("Coming soon...")
        }
    }

    override fun onPhraseChanged() {
        view?.handleRecoverButton()
    }
}
