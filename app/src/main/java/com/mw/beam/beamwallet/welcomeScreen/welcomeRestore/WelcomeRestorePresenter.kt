package com.mw.beam.beamwallet.welcomeScreen.welcomeRestore

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestorePresenter(currentView: WelcomeRestoreContract.View, private val repository: WelcomeRestoreContract.Repository, private val state: WelcomeRestoreState)
    : BasePresenter<WelcomeRestoreContract.View>(currentView),
        WelcomeRestoreContract.Presenter {

    override fun onStart() {
        super.onStart()
        view?.init()
        view?.configPhrases(state.phrasesCount)
    }

    override fun onStop() {
        view?.clearWindowState()
        super.onStop()
    }

    override fun onRestorePressed() {
        view?.showPasswordsFragment(view?.getPhrase() ?: return)
    }

    override fun onPhraseChanged() {
        view?.handleRestoreButton()
    }
}
