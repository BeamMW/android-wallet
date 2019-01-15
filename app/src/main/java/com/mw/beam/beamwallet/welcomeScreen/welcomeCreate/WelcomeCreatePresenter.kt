package com.mw.beam.beamwallet.welcomeScreen.welcomeCreate

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WelcomeCreatePresenter(currentView: WelcomeCreateContract.View, currentRepository: WelcomeCreateContract.Repository)
    : BasePresenter<WelcomeCreateContract.View, WelcomeCreateContract.Repository>(currentView, currentRepository),
        WelcomeCreateContract.Presenter {

    override fun onCreateWallet() {
        view?.createWallet()
    }

    override fun onRestoreWallet() {
        view?.restoreWallet()
    }

    override fun hasBackArrow(): Boolean? = false
}
