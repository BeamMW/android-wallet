package com.mw.beam.beamwallet.main

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/4/18.
 */
interface MainContract {
    interface View : MvpView {
        fun showTransactionDetails(item: TxDescription)
        fun configNavDrawer()
    }

    interface Presenter : MvpPresenter<View> {
        fun onShowTransactionDetails(item: TxDescription)
        fun onClose()
    }

    interface Repository : MvpRepository {
        fun closeWallet()
    }
}
