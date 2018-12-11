package com.mw.beam.beamwallet.welcomeScreen.welcomeOpen

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeOpenPresenter(currentView: WelcomeOpenContract.View, private val repository: WelcomeOpenContract.Repository)
    : BasePresenter<WelcomeOpenContract.View>(currentView),
        WelcomeOpenContract.Presenter {

    override fun onOpenWallet() {
        view?.hideKeyboard()

        if (view != null && view!!.hasValidPass()) {
            if (AppConfig.Status.STATUS_OK == repository.openWallet(view?.getPass())) {
                view?.openWallet()
            } else {
                view?.showOpenWalletError()
            }
        }
    }

    override fun onPassChanged() {
        view?.clearError()
    }

    override fun onChangeWallet() {
        view?.clearError()
        view?.showChangeAlert()
    }

    override fun onChangeConfirm() {
        view?.changeWallet()
    }

    override fun onForgotPassword() {
        view?.clearError()
        view?.showForgotAlert()
    }

    override fun onForgotConfirm() {
        view?.restoreWallet()
    }

    override fun hasBackArrow(): Boolean? = false
}
