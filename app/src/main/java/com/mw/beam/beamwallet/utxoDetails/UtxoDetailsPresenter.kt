package com.mw.beam.beamwallet.utxoDetails

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsPresenter(currentView: UtxoDetailsContract.View, private val repository: UtxoDetailsContract.Repository)
    : BasePresenter<UtxoDetailsContract.View>(currentView),
        UtxoDetailsContract.Presenter {

    override fun onCreate() {
        super.onCreate()
        repository.utxo = view?.getUtxoDetails()
        repository.relatedTransactions = view?.getRelatedTransactions()
    }

    override fun onStart() {
        super.onStart()
        view?.init(repository.utxo ?: return, repository.relatedTransactions ?: return)
    }
}
