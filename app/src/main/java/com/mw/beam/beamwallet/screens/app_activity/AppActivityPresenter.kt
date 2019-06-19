package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App

class AppActivityPresenter(view: AppActivityContract.View?, repository: AppActivityContract.Repository) : BasePresenter<AppActivityContract.View, AppActivityContract.Repository>(view, repository), AppActivityContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        if (repository.isWalletInitialized() && !App.isAuthenticated) {
            view?.showOpenFragment()
        } else if (App.isAuthenticated) {
            view?.showWalletFragment()
        }
    }

    override fun onNewIntent(txId: String?) {
        if (App.isAuthenticated) {
            if (txId == null) {
                view?.showWalletFragment()
            } else {
                view?.showTransactionDetailsFragment(txId)
            }
        } else {
            view?.showOpenFragment()
        }
    }

    override fun onPendingSend(info: PendingSendInfo) {
        view?.startNewSnackbar({}, { repository.sendMoney(info.outgoingAddress, info.token, info.comment, info.amount, info.fee) })
    }
}