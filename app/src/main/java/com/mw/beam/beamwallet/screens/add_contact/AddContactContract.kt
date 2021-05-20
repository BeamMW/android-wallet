package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress


interface AddContactContract {
    interface View : MvpView {
        fun getAddress(): String
        fun getName(): String
        fun showTokenError(address:WalletAddress?)
        fun hideTokenError()
        fun close()
        fun navigateToScanQr()
        fun setAddress(address: String)
        fun showErrorNotBeamAddress()
        fun getAddressFromArguments(): String?
    }

    interface Presenter : MvpPresenter<View> {
        fun checkAddress()
        fun onTokenChanged()
        fun onCancelPressed()
        fun onSavePressed()
        fun onScanPressed()
        fun onScannedQR(text: String?)
    }

    interface Repository: MvpRepository {
        fun saveContact(address: String, name: String)
    }
}