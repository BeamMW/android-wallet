package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import io.reactivex.subjects.Subject

interface ChangeAddressContract {
    interface View: MvpView {
        fun isFromReceive(): Boolean
        fun init(state: ViewState)
        fun updateList(items: List<SearchItem>)
        fun back(walletAddress: WalletAddress)
        fun showScanQr()
    }

    interface Presenter: MvpPresenter<View> {
        fun onChangeSearchText(text: String)
        fun onItemPressed(walletAddress: WalletAddress)
        fun onScanQrPressed()
        fun onScannedQr(address: String)
    }

    interface Repository: MvpRepository {
        fun getTxStatus(): Subject<OnTxStatusData>
        fun getAddresses(): Subject<OnAddressesData>
        fun getCategoryForAddress(address: String): Category?
    }

    enum class ViewState {
        Send, Receive
    }
}