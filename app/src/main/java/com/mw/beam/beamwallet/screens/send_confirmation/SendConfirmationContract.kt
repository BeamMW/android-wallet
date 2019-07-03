package com.mw.beam.beamwallet.screens.send_confirmation

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import io.reactivex.subjects.Subject

interface SendConfirmationContract {
    interface View : MvpView {
        fun getAddress(): String
        fun getOutgoingAddress(): String
        fun getAmount(): Long
        fun getFee(): Long
        fun getComment(): String?
        fun init(address: String, outgoingAddress: String, amount: Double, fee: Long)
        fun configurateContact(walletAddress: WalletAddress, category: Category?)
        fun configUtxoInfo(usedUtxo: Double, changedUtxo: Double)
        fun showSaveContactDialog()
        fun showSaveAddressFragment(address: String)
        fun delaySend(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long)
        fun showWallet()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSendPressed()
        fun onSaveContactPressed()
        fun onCancelSaveContactPressed()
    }

    interface Repository : MvpRepository {
        fun getCategory(address: String): Category?
        fun getAddresses(): Subject<OnAddressesData>
    }
}