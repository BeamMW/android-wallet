package com.mw.beam.beamwallet.main

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/4/18.
 */
class MainPresenter(currentView: MainContract.View, currentRepository: MainContract.Repository)
    : BasePresenter<MainContract.View, MainContract.Repository>(currentView, currentRepository),
        MainContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.configNavDrawer()
    }

    override fun onClose() {
        repository.closeWallet()
    }

    override fun onShowTransactionDetails(item: TxDescription) {
        view?.showTransactionDetails(item)
    }

    override fun onReceive() {
        view?.showReceiveScreen()
    }

    override fun onSend() {
        view?.showSendScreen()
    }

    override fun hasStatus(): Boolean = true

}
