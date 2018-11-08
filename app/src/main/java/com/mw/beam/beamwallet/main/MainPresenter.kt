package com.mw.beam.beamwallet.main

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/4/18.
 */
class MainPresenter(currentView: MainContract.View, private val repository: MainContract.Repository)
    : BasePresenter<MainContract.View>(currentView),
        MainContract.Presenter {

    override fun viewIsReady() {
        view?.configNavDrawer()
    }

    override fun onClose() {
        repository.closeWallet()
    }

    override fun onShowTransactionDetails(item: TxDescription) {
        view?.showTransactionDetails(item)
    }
}
