package com.mw.beam.beamwallet.transactionDetails

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsPresenter(currentView: TransactionDetailsContract.View, currentRepository: TransactionDetailsContract.Repository)
    : BasePresenter<TransactionDetailsContract.View, TransactionDetailsContract.Repository>(currentView, currentRepository),
        TransactionDetailsContract.Presenter {

    override fun onCreate() {
        super.onCreate()
        repository.txDescription = view?.getTransactionDetails()
    }

    override fun onStart() {
        super.onStart()
        view?.init(repository.txDescription ?: return)
    }
}
