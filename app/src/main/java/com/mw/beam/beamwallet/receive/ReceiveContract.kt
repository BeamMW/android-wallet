package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
interface ReceiveContract {
    interface View : MvpView {
        fun init()
        fun getComment() : String?
        fun showToken(receiveToken : String)
        fun copyToClipboard(receiveToken : String)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCopyTokenPressed()
        fun onShowQrPressed()
        fun onBackPressed()
        fun onExpirePeriodChanged(period : ExpirePeriod)
    }

    interface Repository : MvpRepository {
        fun generateNewAddress() : Subject<WalletAddress>
        fun saveAddress(address: WalletAddress)
    }
}
