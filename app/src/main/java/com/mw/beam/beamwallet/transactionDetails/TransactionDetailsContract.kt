package com.mw.beam.beamwallet.transactionDetails

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/17/18.
 */
class TransactionDetailsContract {
    interface View : MvpView {
        fun getTransactionDetails() : TxDescription?
        fun init(txDescription: TxDescription)
    }

    interface Presenter : MvpPresenter<View>
    interface Repository : MvpRepository
}
