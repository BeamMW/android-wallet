package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface AppActivityContract {
    interface View: MvpView {
        fun showOpenFragment()
        fun showWalletFragment()
        fun showTransactionDetailsFragment(txId: String)
        fun startNewSnackbar(onUndo: () -> Unit, onDismiss: () -> Unit)
    }

    interface Presenter: MvpPresenter<View> {
        fun onNewIntent(txId: String?)
        fun onPendingSend(info: PendingSendInfo)
    }

    interface Repository: MvpRepository {
        fun sendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long)
    }
}