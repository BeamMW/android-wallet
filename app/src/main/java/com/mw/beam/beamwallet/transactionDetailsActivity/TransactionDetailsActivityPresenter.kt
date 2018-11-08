package com.mw.beam.beamwallet.transactionDetailsActivity

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsActivityPresenter(currentView: TransactionDetailsActivityContract.View, private val repository: TransactionDetailsActivityContract.Repository)
    : BasePresenter<TransactionDetailsActivityContract.View>(currentView),
        TransactionDetailsActivityContract.Presenter {

    override fun viewIsReady() {
        view?.init()
        view?.showTransactionDetailsFragment(view?.getTransactionDetails() ?: return)
    }
}
