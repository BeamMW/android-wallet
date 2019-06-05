package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface AppActivityContract {
    interface View: MvpView {
        fun showOpenFragment()
        fun showWalletFragment()
        fun showTransactionDetailsFragment(txId: String)
        fun cancelSnackbar()
        fun startNewSnackbar()
        fun updateSnackbar(time: Int)

    }

    interface Presenter: MvpPresenter<View> {
        fun onNewIntent(txId: String?)
        fun onPendingSend(info: PendingSendInfo)
        fun onUndoSend()
    }

    interface Repository: MvpRepository {
        fun sendMoney(token: String, comment: String?, amount: Long, fee: Long)
    }
}