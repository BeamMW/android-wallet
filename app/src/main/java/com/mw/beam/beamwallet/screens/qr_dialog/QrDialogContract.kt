package com.mw.beam.beamwallet.screens.qr_dialog

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress

interface QrDialogContract {
    interface View: MvpView {
        fun getWalletAddress(): WalletAddress
        fun getAmount(): Long
        fun init(walletAddress: WalletAddress, amount: Long)
        fun shareAddress(walletId: String)
    }

    interface Presenter: MvpPresenter<View> {
        fun onSharePressed()
    }

    interface Repository: MvpRepository
}