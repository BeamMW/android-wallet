package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletStatus
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
interface SendContract {

    interface View : MvpView {
        fun getAmount(): Double
        fun getFee(): Long
        fun getToken(): String
        fun getComment(): String?
        fun updateUI(shouldShowParams: Boolean)
        fun hasErrors(availableAmount : Long) : Boolean
        fun clearErrors()
        fun clearToken(clearedToken : String?)
        fun init()
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSend()
        fun onTokenChanged(rawToken : String?)
        fun onAmountChanged()
    }

    interface Repository : MvpRepository {
        fun sendMoney(token: String, comment: String?, amount: Long, fee: Long)
        fun getWalletStatus(): Subject<WalletStatus>
    }
}
