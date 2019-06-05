package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.DelayedTask


class AppActivityPresenter(view: AppActivityContract.View?, repository: AppActivityContract.Repository) : BasePresenter<AppActivityContract.View, AppActivityContract.Repository>(view, repository), AppActivityContract.Presenter {
    private val duration = 5
    private var currentDelayedTask: DelayedTask? = null
    private var currentPendingInfo: PendingSendInfo? = null

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
            view?.cancelSnackbar()
        }
    }

    override fun onPendingSend(info: PendingSendInfo) {
        view?.cancelSnackbar()
        view?.startNewSnackbar()
        currentPendingInfo = info
        currentDelayedTask = DelayedTask.startNew(
                duration,
                {
                    repository.sendMoney(info.token, info.comment, info.amount, info.fee)
                    if (info.id == currentPendingInfo?.id) {
                        view?.cancelSnackbar()
                    }
                },
                { view?.updateSnackbar(it) },
                { if (info.id == currentPendingInfo?.id) view?.cancelSnackbar() }
        )
    }

    override fun onUndoSend() {
        currentDelayedTask?.cancel(true)
        view?.cancelSnackbar()
    }
}