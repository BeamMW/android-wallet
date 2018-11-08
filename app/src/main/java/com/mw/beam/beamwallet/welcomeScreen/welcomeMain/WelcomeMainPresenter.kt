package com.mw.beam.beamwallet.welcomeScreen.welcomeMain

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeMainPresenter(currentView: WelcomeMainContract.View, private val repository: WelcomeMainContract.Repository)
    : BasePresenter<WelcomeMainContract.View>(currentView),
        WelcomeMainContract.Presenter {

    override fun viewIsReady() {
        view?.configScreen(repository.isWalletInitialized())
    }

    override fun onCreateWallet() {
        view?.createWallet()
    }

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
        view?.configScreen(false)
    }

    override fun onRestoreWallet() {
        view?.restoreWallet()
    }
}
