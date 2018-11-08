package com.mw.beam.beamwallet.transactionDetailsActivity

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/18/18.
 */
interface TransactionDetailsActivityContract {
    interface View : MvpView {
        fun showTransactionDetailsFragment(txDescription : TxDescription)
        fun getTransactionDetails() : TxDescription
        fun init()
    }

    interface Presenter : MvpPresenter<View>
    interface Repository : MvpRepository
}
