package com.mw.beam.beamwallet.transactionDetails

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/17/18.
 */
class TransactionDetailsPresenter(currentView: TransactionDetailsContract.View, private val repository: TransactionDetailsContract.Repository)
    : BasePresenter<TransactionDetailsContract.View>(currentView),
        TransactionDetailsContract.Presenter {

    override fun viewIsReady() {
        view?.init(view?.getTransactionDetails() ?: return)
    }
}
