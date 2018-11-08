package com.mw.beam.beamwallet.welcomeScreen.welcomeRecover

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRecoverPresenter(currentView: WelcomeRecoverContract.View, private val repository: WelcomeRecoverContract.Repository)
    : BasePresenter<WelcomeRecoverContract.View>(currentView),
        WelcomeRecoverContract.Presenter {

    override fun viewIsReady() {
        view?.init()
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
