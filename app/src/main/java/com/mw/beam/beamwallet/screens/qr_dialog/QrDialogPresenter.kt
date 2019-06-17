package com.mw.beam.beamwallet.screens.qr_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter

class QrDialogPresenter(view: QrDialogContract.View?, repository: QrDialogContract.Repository, private val state: QrDialogState)
    : BasePresenter<QrDialogContract.View, QrDialogContract.Repository>(view, repository), QrDialogContract.Presenter {


    override fun onViewCreated() {
        super.onViewCreated()
        state.walletAddress = view?.getWalletAddress()
        state.amount = view?.getAmount() ?: 0

        state.walletAddress?.let {
            view?.init(it, state.amount)
        }
    }

    override fun onSharePressed() {
        view?.shareAddress(state.walletAddress?.walletID ?: "")
    }

}